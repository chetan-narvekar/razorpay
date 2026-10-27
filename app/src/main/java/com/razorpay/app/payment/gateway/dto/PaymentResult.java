package com.razorpay.app.payment.gateway.dto;

public sealed interface PaymentResult permits PaymentResult.Failure, PaymentResult.Pending, PaymentResult.Success {

    record Pending(String registrationRef) implements PaymentResult{};

    record Failure(String errorCode , String errorCodeDescription) implements PaymentResult{};

    record Success(String bankRererence) implements PaymentResult{}
}