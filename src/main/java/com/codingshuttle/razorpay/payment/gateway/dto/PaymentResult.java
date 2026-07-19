package com.codingshuttle.razorpay.payment.gateway.dto;

public sealed interface PaymentResult {

    record Pending(String registrationRef) implements PaymentResult {
    }
    record Failure(String errorCode, String errorDescription) implements PaymentResult {
    }
    record Success(String bankReference) implements PaymentResult{}

}
