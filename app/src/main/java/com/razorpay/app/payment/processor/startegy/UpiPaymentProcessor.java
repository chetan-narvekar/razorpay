package com.razorpay.app.payment.processor.startegy;

import com.razorpay.app.common.util.RandomizerUtil;
import com.razorpay.app.payment.processor.PaymentProcessor;
import com.razorpay.app.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.app.payment.processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";
        String bankCode = request.methodDetails() != null ? request.methodDetails().get("BANK").toString() : null;
        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED", "Bank rejected the transaction registration");
        }
        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);
        String redirectRef = "BANK_REF"+processorRef;
        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
