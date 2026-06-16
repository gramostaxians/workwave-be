package com.hr.workwave.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "security_audit_logs", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_ip", columnList = "ip_address"),
        @Index(name = "idx_audit_user", columnList = "user_email"),
        @Index(name = "idx_audit_event_type", columnList = "event_type")
})
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(length = 1024)
    private String uri;

    @Column(name = "query_string", length = 2048)
    private String queryString;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * REQUEST, UNAUTHORIZED, BRUTE_FORCE, SUSPICIOUS
     */
    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    /**
     * Only populated for SUSPICIOUS events — the matched pattern
     */
    @Column(name = "threat_detail", length = 512)
    private String threatDetail;
}

