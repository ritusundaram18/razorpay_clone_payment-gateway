package com.codingshuttle.razorpay.payment.dto.request;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequestDto(
       @NotNull(message = "orderId is required")
       UUID orderId,
       @NotNull(message = "PaymentMethod is required")
       PaymentMethod paymentMethod,
       Map<String, Object> paymentMethodDetails
                                    ) {
}
