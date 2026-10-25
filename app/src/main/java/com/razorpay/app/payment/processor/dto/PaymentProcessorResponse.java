package com.razorpay.app.payment.processor.dto;

public sealed interface PaymentProcessorResponse permits PaymentProcessorResponse.Failure, PaymentProcessorResponse.Pending, PaymentProcessorResponse.Success{

    record Pending(
            String processorResponse
    ) implements PaymentProcessorResponse{}

    record Success(
            String processorResponse,
            String bankReference
    )implements PaymentProcessorResponse{}

    record Failure(
            String errorCode,
            String errorDescription
    )implements PaymentProcessorResponse{}

}
