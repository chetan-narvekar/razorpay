package com.razorpay.app.vault.service;

import com.razorpay.app.common.entity.Money;
import com.razorpay.app.payment.processor.dto.PaymentProcessorResponse;
import com.razorpay.app.vault.dto.request.TokenizeRequest;
import com.razorpay.app.vault.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId,String token, Money amount, Map<String, Object> methodDetails);
}
