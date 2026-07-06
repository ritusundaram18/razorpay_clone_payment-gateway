package com.codingshuttle.razorpay.processor.strategy;

import com.codingshuttle.razorpay.processor.PaymentProcessor;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorRequest;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorResponse;

public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
     //Call third party
        return null;
    }
}
