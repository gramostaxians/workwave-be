package com.hr.workwave.config;

import com.hr.workwave.repo.UsersRepository;
import com.hr.workwave.service.SecurityAuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class SecurityAuditFilter extends OncePerRequestFilter {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("SECURITY_AUDIT");
    private static final Logger THREAT_LOG = LoggerFactory.getLogger("SECURITY_THREAT");

    private final SecurityAuditLogService auditLogService;
    private final UsersRepository usersRepository;

    // Cache email -> name to avoid a DB hit on every request
    private final Map<String, String> userNameCache = new ConcurrentHashMap<>();

    // Track failed attempts per IP: IP -> count
    private final Map<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();

    private static final int FAILED_ATTEMPTS_THRESHOLD = 5;

    private static final List<String> SUSPICIOUS_PATTERNS = List.of(
            "../", "..%2F", "%2e%2e", "..\\",          // Path traversal
            "' OR ", "' or ", "UNION SELECT", "union select",  // SQL injection
            "DROP TABLE", "drop table", "INSERT INTO", "--",   // SQL injection
            "<script", "javascript:", "onerror=", "onload=",   // XSS
            "/etc/passwd", "/etc/shadow", "/.env",              // Sensitive file probing
            "/wp-admin", "/wp-login", "/phpinfo", "/.git",      // CMS/framework probing
            "/actuator", "cmd=", "exec(", "system(",            // RCE probing
            "base64_decode", "%00", "null byte"                 // Null byte / encoding attacks
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String fullPath = query != null ? uri + "?" + query : uri;

        // Check for suspicious patterns BEFORE processing
        String matchedPattern = findSuspiciousPattern(fullPath);
        if (matchedPattern != null) {
            THREAT_LOG.error("SUSPICIOUS_REQUEST ip={} method={} path=\"{}\" matchedPattern=\"{}\" userAgent=\"{}\"",
                    clientIp, method, fullPath, matchedPattern, userAgent);
            auditLogService.logSuspicious(method, uri, query, clientIp, userAgent, matchedPattern);
        }

        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = responseWrapper.getStatus();
            String userEmail = extractUserEmail();
            String userName = resolveUserName(userEmail);

            // Log every request to file
            AUDIT_LOG.info("method={} uri={} status={} user=\"{}\" email={} ip={} duration={}ms userAgent=\"{}\"",
                    method, uri, status, userName, userEmail, clientIp, duration, userAgent);

            // Persist every request to DB
            auditLogService.logRequest(method, uri, query, status, userEmail, userName, clientIp, userAgent, duration);

            // Track failed auth attempts
            if (status == 401 || status == 403) {
                int count = failedAttempts
                        .computeIfAbsent(clientIp, k -> new AtomicInteger(0))
                        .incrementAndGet();

                THREAT_LOG.warn("UNAUTHORIZED_ACCESS method={} uri={} status={} user=\"{}\" email={} ip={} failedAttempts={}",
                        method, uri, status, userName, userEmail, clientIp, count);

                if (count >= FAILED_ATTEMPTS_THRESHOLD) {
                    THREAT_LOG.error("BRUTE_FORCE_SUSPECTED ip={} failedAttempts={} lastUri={} userAgent=\"{}\"",
                            clientIp, count, uri, userAgent);
                }

                // Persist unauthorized / brute force event to DB
                auditLogService.logUnauthorized(method, uri, status, userEmail, userName, clientIp, userAgent, count);
            } else {
                // Reset counter on successful auth
                failedAttempts.remove(clientIp);
            }

            responseWrapper.copyBodyToResponse();
        }
    }

    private String resolveUserName(String email) {
        if (email == null || email.equals("anonymous")) return "anonymous";
        return userNameCache.computeIfAbsent(email, e -> {
            try {
                var user = usersRepository.findByEmail(e);
                return (user != null && user.getName() != null) ? user.getName() : e;
            } catch (Exception ex) {
                return e; // fallback to email if DB lookup fails
            }
        });
    }

    private String findSuspiciousPattern(String fullPath) {
        String lowerPath = fullPath.toLowerCase();
        for (String pattern : SUSPICIOUS_PATTERNS) {
            if (lowerPath.contains(pattern.toLowerCase())) {
                return pattern;
            }
        }
        return null;
    }

    private String extractUserEmail() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "anonymous";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim(); // First IP in chain is the real client
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
