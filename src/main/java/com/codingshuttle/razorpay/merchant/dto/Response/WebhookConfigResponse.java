package com.codingshuttle.razorpay.merchant.dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebhookConfigResponse(
        UUID id,
        String targetUrl,
        String webhookSecret,
        boolean enabled,
        String eventTypes
) {}

