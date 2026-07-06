package com.codingshuttle.razorpay.payment.gateway.dto;

public sealed interface PaymentResult {

    record Pending(String processorReference) implements PaymentResult {
    }
    record Failure(String errorCode, String errorDescription) implements PaymentResult {
    }
}
