package com.codingshuttle.razorpay.payment.config;

import com.codingshuttle.razorpay.common.enums.PaymentMethod;
import com.codingshuttle.razorpay.payment.gateway.PaymentAdapter;
import com.codingshuttle.razorpay.payment.gateway.adapter.CardPaymentAdapter;
import com.codingshuttle.razorpay.payment.gateway.adapter.UpiPaymentAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentAdapterConfig {
    @Bean
    public Map<PaymentMethod , PaymentAdapter> paymentAdapterMap
            () {
        return Map.of(
                PaymentMethod.CARD, new CardPaymentAdapter(),
                PaymentMethod.UPI, new UpiPaymentAdapter(),
                PaymentMethod.NETBANKING, new UpiPaymentAdapter()
        );
    }
}
