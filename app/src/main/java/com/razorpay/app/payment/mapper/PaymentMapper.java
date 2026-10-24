package com.razorpay.app.payment.mapper;

import com.razorpay.app.payment.dto.response.PaymentResponse;
import com.razorpay.app.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "orderId" , source = "order.id")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "orderId" , source = "order.id")
    List<PaymentResponse> toResponseList(List<Payment> paymentList);
}
