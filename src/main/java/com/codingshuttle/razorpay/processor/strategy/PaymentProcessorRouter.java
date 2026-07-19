package com.codingshuttle.razorpay.processor.strategy;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import com.codingshuttle.razorpay.processor.PaymentProcessor;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorRequest;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorResponse;

import java.util.Map;

public class PaymentProcessorRouter {

    private Map<PaymentMethod,PaymentProcessor> paymentProcessors;


        public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
            PaymentProcessor processor=paymentProcessors.get(request.method());
            if(processor==null){
                throw new IllegalArgumentException("No payment processor registered for method: "+request.method());
            }
            return processor.charge(request);
        }

    }
