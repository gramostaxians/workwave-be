package com.hr.workwave.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "ms_graph_tokens")
public class MsGraphToken {
    @Id
    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
    private String refreshToken;

    @Column(name = "expires_at")
    private long expiresAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "user_Id")
    private BigInteger userId;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

}