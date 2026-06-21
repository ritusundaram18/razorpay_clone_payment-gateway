package com.codingshuttle.razorpay.merchant.dto.Response;

import com.codingshuttle.razorpay.common.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String keyId,
        String keySecret,
        Environment environment


) {
}
