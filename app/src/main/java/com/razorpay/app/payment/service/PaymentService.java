package com.razorpay.app.payment.service;

import com.razorpay.app.payment.dto.request.PaymentInitRequest;
import com.razorpay.app.payment.dto.response.PaymentResponse;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse initiate(UUID merchantId, PaymentInitRequest requestDto);

    PaymentResponse capture(UUID merchantId, UUID paymentId);
}
