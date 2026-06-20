package com.codingshuttle.razorpay.operations.entity;

import com.codingshuttle.razorpay.common.enums.WebhookEventStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class WebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;
    @Column(nullable = false,length = 100)


    private String eventType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(nullable = false)
    private String targetUrl;

    @Column(nullable = false)
    private String signature;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WebhookEventStatus status;

    @Column(nullable = false)
    private Integer attempts = 0;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime lastResponseCode;
    private LocalDateTime lastResponseBody;
    private LocalDateTime deliveredAt;

}
