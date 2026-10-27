package com.razorpay.app.payment.gateway.adapter;

import com.razorpay.app.payment.gateway.PaymentAdapter;
import com.razorpay.app.payment.gateway.dto.PaymentRequest;
import com.razorpay.app.payment.gateway.dto.PaymentResult;
import com.razorpay.app.payment.processor.dto.PaymentProcessorResponse;
import com.razorpay.app.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        String token =(String) request.methodDetails().get("token");
        PaymentProcessorResponse response = vaultService.charge(request.paymentId(), token, request.amount(), request.methodDetails());

        return switch (response){
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorResponse());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return null;
    }
}
