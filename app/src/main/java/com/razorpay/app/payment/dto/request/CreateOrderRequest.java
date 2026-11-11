package com.razorpay.app.payment.dto.request;

import com.razorpay.app.common.entity.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateOrderRequest(
        Money amount,
        String receipt,
        Map<String, Object> notes,
        LocalDateTime expiresAt,
        @Valid
        CustomerDetails customerDetails
) {
    public record CustomerDetails(
            @Size(max = 200)
            String name,

            @Email
            @Size(max=200)
            String email,

            @Size(max = 30)
            String phone

    ){}
}
