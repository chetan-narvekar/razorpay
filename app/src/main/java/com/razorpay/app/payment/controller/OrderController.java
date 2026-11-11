package com.razorpay.app.payment.controller;

import com.razorpay.app.merchant.security.MerchantContext;
import com.razorpay.app.payment.dto.request.CreateOrderRequest;
import com.razorpay.app.payment.dto.response.OrderResponse;
import com.razorpay.app.payment.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "apiKeyAuth")
public class OrderController {

    private final OrderService orderService;
    private final MerchantContext merchantContext;

    @Operation(parameters = {
            @Parameter(
                    name = "X-Idempotency-Key",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Unique key to prevent duplicate requests"
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(merchantContext.getMerchantId(), request));
    }
}


