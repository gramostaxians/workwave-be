package com.hr.workwave.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_contract_files", indexes = {
        @Index(name = "idx_user_contract_files_user_id", columnList = "user_id"),
        @Index(name = "idx_user_contract_files_user_id_created_at", columnList = "user_id, created_at")
})
public class UserContractFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String filename;

    /**
     * Base64-encoded AES-256 key used to encrypt this file.
     * Null for legacy files uploaded before encryption was introduced.
     */
    @Column(name = "encryption_key", length = 64)
    private String encryptionKey;

    /**
     * Base64-encoded 96-bit GCM IV used during encryption.
     * Null for legacy files uploaded before encryption was introduced.
     */
    @Column(name = "encryption_iv", length = 32)
    private String encryptionIv;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

