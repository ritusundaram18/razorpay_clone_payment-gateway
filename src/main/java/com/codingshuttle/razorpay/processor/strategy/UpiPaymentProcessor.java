package com.codingshuttle.razorpay.processor.strategy;

import com.codingshuttle.razorpay.common.utl.RandomizerUtil;
import com.codingshuttle.razorpay.processor.PaymentProcessor;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorRequest;
import com.codingshuttle.razorpay.processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component

public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String VPA_CODE_FAIL="fail@okaxis";

        String bankCode = request.methodDetails()!=null?
                request.methodDetails().get("vpa").toString():null;
//simulation
        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("UPI_REJECTED","Bank rejected the transaction registration"
            );
        }

        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        String bankRef="BANK_REF_" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorRef
        );
    }
}
