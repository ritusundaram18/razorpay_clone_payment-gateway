package com.codingshuttle.razorpay.merchant.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class ApiKey {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="merchant_id",nullable = false)
    private Merchant merchant;

    @Column(nullable = false,length=50)
    private String keyId;

    @Column(nullable = false,length=200)
    private String keySecretHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private String environment;
    @Column(nullable = false) boolean enabled=true;
     private LocalDateTime lastUsedAt;
     private LocalDateTime rotatedAt;
     private LocalDateTime gracePeriodExpiresAt;
}
