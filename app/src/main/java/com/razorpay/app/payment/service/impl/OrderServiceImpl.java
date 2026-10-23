package com.razorpay.app.payment.service.impl;

import com.razorpay.app.common.enums.OrderStatus;
import com.razorpay.app.common.exceptions.DuplicateResourceException;
import com.razorpay.app.payment.dto.request.CreateOrderRequest;
import com.razorpay.app.payment.dto.response.OrderResponse;
import com.razorpay.app.payment.entity.OrderRecord;
import com.razorpay.app.payment.repository.OrderRepository;
import com.razorpay.app.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if (request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists: " + request.receipt());
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())

                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();
        order = orderRepository.save(order);
        return new OrderResponse(order.getId(), order.getMerchantId(), order.getReceipt(), order.getAmount(), order.getOrderStatus(), order.getAttempts(),order.getNotes(), order.getExpiresAt(), null);
    }
}
