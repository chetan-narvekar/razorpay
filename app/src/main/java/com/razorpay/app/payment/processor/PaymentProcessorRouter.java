package com.razorpay.app.payment.processor;

import com.razorpay.app.common.enums.PaymentMethod;
import com.razorpay.app.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.app.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request){
        PaymentProcessor processor = paymentProcessorMap.get(request.method());
        if(processor == null){
            throw new IllegalArgumentException(("No Payment processor registered for method:"+ request.method()));
        }
        return processor.charge(request);
    }
}
