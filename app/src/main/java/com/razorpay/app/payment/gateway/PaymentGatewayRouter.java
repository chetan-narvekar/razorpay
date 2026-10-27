package com.razorpay.app.payment.gateway;

import com.razorpay.app.common.enums.PaymentMethod;
import com.razorpay.app.payment.gateway.dto.PaymentRequest;
import com.razorpay.app.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;

    public PaymentResult initiate(PaymentRequest request){
        PaymentAdapter adapter = paymentAdapters.get(request.method());
        if(adapter == null){
            throw new IllegalArgumentException(("No Payment adapter registered for method "+ request.method()));
        }
        return adapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapters.get(method);
        if( paymentAdapter == null ){
            throw new IllegalArgumentException("No Payment registered for method");
        }
        return paymentAdapter.capture(paymentId);
    }
}
