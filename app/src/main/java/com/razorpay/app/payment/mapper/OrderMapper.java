package com.razorpay.app.payment.mapper;

import com.razorpay.app.payment.dto.response.OrderResponse;
import com.razorpay.app.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    OrderResponse toResponse(OrderRecord orderRecord);
}
