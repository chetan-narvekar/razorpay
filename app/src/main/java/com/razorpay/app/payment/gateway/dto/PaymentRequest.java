package com.razorpay.app.payment.gateway.dto;

import com.razorpay.app.common.entity.Money;
import com.razorpay.app.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;
public record PaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
