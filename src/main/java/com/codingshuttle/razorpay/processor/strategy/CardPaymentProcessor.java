package com.codingshuttle.razorpay.processor.strategy;

import com.codingshuttle.razorpay.common.utl.RandomizerUtil;
import com.codingshuttle.razorpay.processor.PaymentProcessor;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorRequest;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000069";

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        String pan = request.pan();
        if (PAN_CARD_DECLINED.equals(pan)) {
            log.warn("cad declined");

            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card was declined by the bank");
        }
        if (PAN_CARD_EXPIRED.equals(pan)) {
            log.warn(" pan card expired");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has expired");
        }
        String processorRef = "CARD_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef
        );
    }
}
