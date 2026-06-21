package com.codingshuttle.razorpay.merchant.dto.Response;

import com.codingshuttle.razorpay.common.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        MerchantStatus merchantStatus
) {
}
