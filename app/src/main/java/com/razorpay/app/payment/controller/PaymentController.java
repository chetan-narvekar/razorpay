package com.razorpay.app.payment.controller;

import com.razorpay.app.merchant.security.MerchantContext;
import com.razorpay.app.payment.dto.request.PaymentInitRequest;
import com.razorpay.app.payment.dto.response.PaymentResponse;
import com.razorpay.app.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "apiKeyAuth")
public class PaymentController {

    private final PaymentService paymentService;

   private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestBody @Valid PaymentInitRequest paymentInitRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(merchantContext.getMerchantId(),paymentInitRequest));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId){
        return ResponseEntity.ok(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }
}
