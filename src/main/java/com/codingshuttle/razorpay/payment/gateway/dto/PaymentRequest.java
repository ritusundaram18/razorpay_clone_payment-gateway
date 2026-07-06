package com.codingshuttle.razorpay.payment.gateway.dto;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        com.codingshuttle.razorpay.common.entity.Money amount, PaymentMethod paymentMethod,
        Map<String, Object> methodDetails
) {
}
