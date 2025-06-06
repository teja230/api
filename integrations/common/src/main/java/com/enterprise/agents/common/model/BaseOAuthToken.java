package com.enterprise.agents.common.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseOAuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String enterpriseId;

    @Column(nullable = false)
    private String accessToken;

    @Column
    private String refreshToken;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private String scope;

    @Column
    private String tokenType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 