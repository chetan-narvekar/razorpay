package com.razorpay.app.payment.service;

import com.razorpay.app.payment.dto.request.CreateOrderRequest;
import com.razorpay.app.payment.dto.response.OrderResponse;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface OrderService {
    OrderResponse create(UUID merchantId, CreateOrderRequest request);
}
