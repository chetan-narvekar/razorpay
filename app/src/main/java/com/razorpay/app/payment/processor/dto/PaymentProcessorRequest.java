package com.razorpay.app.payment.processor.dto;

import com.razorpay.app.common.entity.Money;
import com.razorpay.app.common.enums.PaymentMethod;

import java.util.Map;

public record PaymentProcessorRequest(
        PaymentMethod method,
        Money amount,
        String pan,
        String expiry,
        Map<String, Object> methodDetails
) {
}
