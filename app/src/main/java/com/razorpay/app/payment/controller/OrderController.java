package com.razorpay.app.payment.controller;

import com.razorpay.app.payment.dto.request.CreateOrderRequest;
import com.razorpay.app.payment.dto.response.OrderResponse;
import com.razorpay.app.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderService orderService;

    UUID merchnatId = UUID.fromString("9a7fcc42-1126-4b7f-aa3f-7eec256cf8d0");

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(merchnatId, request));
    }
}


