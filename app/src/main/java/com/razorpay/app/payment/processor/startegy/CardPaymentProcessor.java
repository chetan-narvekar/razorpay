package com.razorpay.app.payment.processor.startegy;

import com.razorpay.app.common.util.RandomizerUtil;
import com.razorpay.app.payment.processor.PaymentProcessor;
import com.razorpay.app.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.app.payment.processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor implements PaymentProcessor {

    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000003";

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        String pan = request.pan();
        if(PAN_CARD_DECLINED.equals(pan)){
           return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card declined");
        }
        if(PAN_CARD_EXPIRED.equals(pan)){
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card expired");
        }

        String processor_ref = "CARD_PROCESSOR_" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processor_ref);
    }

}
