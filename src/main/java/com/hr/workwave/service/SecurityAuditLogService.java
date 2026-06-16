package com.hr.workwave.service;

import com.hr.workwave.model.SecurityAuditLog;
import com.hr.workwave.repo.SecurityAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SecurityAuditLogService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditLogService.class);

    private final SecurityAuditLogRepository repository;

    @Async
    public void logRequest(String method, String uri, String queryString,
                           int status, String userEmail, String userName,
                           String ipAddress, String userAgent, long durationMs) {
        save(SecurityAuditLog.builder()
                .timestamp(LocalDateTime.now())
                .httpMethod(method)
                .uri(uri)
                .queryString(queryString)
                .statusCode(status)
                .userEmail(userEmail)
                .userName(userName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .durationMs(durationMs)
                .eventType("REQUEST")
                .build());
    }

    @Async
    public void logUnauthorized(String method, String uri, int status,
                                String userEmail, String userName, String ipAddress,
                                String userAgent, int failedAttempts) {
        String eventType = failedAttempts >= 5 ? "BRUTE_FORCE" : "UNAUTHORIZED";
        save(SecurityAuditLog.builder()
                .timestamp(LocalDateTime.now())
                .httpMethod(method)
                .uri(uri)
                .statusCode(status)
                .userEmail(userEmail)
                .userName(userName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .eventType(eventType)
                .threatDetail("Failed attempts from this IP: " + failedAttempts)
                .build());
    }

    @Async
    public void logSuspicious(String method, String uri, String queryString,
                              String ipAddress, String userAgent, String matchedPattern) {
        save(SecurityAuditLog.builder()
                .timestamp(LocalDateTime.now())
                .httpMethod(method)
                .uri(uri)
                .queryString(queryString)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .eventType("SUSPICIOUS")
                .threatDetail("Matched pattern: " + matchedPattern)
                .build());
    }

    @Async
    public void logContractDownload(String adminEmail, String adminName,
                                    BigInteger targetUserId, Long contractId, String filename) {
        save(SecurityAuditLog.builder()
                .timestamp(LocalDateTime.now())
                .userEmail(adminEmail)
                .userName(adminName)
                .eventType("CONTRACT_DOWNLOAD")
                .threatDetail("contractId=" + contractId
                        + " targetUserId=" + targetUserId
                        + " filename=" + filename)
                .build());
    }

    private void save(SecurityAuditLog entry) {
        try {
            repository.save(entry);
        } catch (Exception e) {
            log.error("Failed to persist security audit log: {}", e.getMessage());
        }
    }
}
