package com.hr.workwave.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityHelper {

    public Jwt getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }

    public String getCurrentUserId() {
        Jwt jwt = getJwt();
        return (jwt != null) ? jwt.getClaimAsString("upn") : null;
    }

}
