package com.codingshuttle.razorpay.payment.gateway;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentRequest;
import com.codingshuttle.razorpay.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod,PaymentAdapter> paymentAdapterMap;
    public PaymentResult initiatePayment(PaymentRequest request) {

        PaymentAdapter adapter = paymentAdapterMap.get(request.paymentMethod());
        if(adapter==null){
            throw new IllegalArgumentException("No payment adapter register for method: " + request.paymentMethod());
        }
        return adapter.initiate(request);
    }
    public PaymentResult capture(PaymentMethod method, UUID paymentId){
        PaymentAdapter adapter = paymentAdapterMap.get(method);
        if(adapter==null){
            throw new IllegalArgumentException("No payment adapter register for method: " + method);
        }
        return adapter.capture(paymentId);
    }
}
