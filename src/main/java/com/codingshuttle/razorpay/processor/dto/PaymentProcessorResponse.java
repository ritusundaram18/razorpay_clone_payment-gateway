package com.codingshuttle.razorpay.processor.dto;

public sealed interface PaymentProcessorResponse permits PaymentProcessorResponse.Pending,
        PaymentProcessorResponse.Success, PaymentProcessorResponse.Failure {

    //Pending ,successs, Failure -> will create three type of response
//shield CLass-< this class or interface can be extended only to those class
//which are permitted
    record Pending(String processorReference) implements PaymentProcessorResponse {
    }

    record Success(String processorReference, String bankReference) implements PaymentProcessorResponse{
    }

    record Failure(String errorCode, String errorDescription) implements PaymentProcessorResponse{
    }


}