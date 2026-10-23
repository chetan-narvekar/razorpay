package com.razorpay.app.payment.dto.request;

import com.razorpay.app.common.entity.Money;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateOrderRequest(
        Money amount,
        String receipt,
        Map<String, Object> notes,
        LocalDateTime expiresAt
) {
}
