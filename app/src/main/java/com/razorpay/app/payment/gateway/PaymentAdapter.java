package com.razorpay.app.payment.gateway;

import com.razorpay.app.payment.gateway.dto.PaymentRequest;
import com.razorpay.app.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    PaymentResult initiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}
