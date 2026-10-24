package com.razorpay.app.merchant.mapper;

import com.razorpay.app.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.app.merchant.dto.response.ApiKeyResponse;
import com.razorpay.app.merchant.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    ApiKeyCreateResponse toResponse(ApiKey apiKey);

    List<ApiKeyResponse> toResponse(List<ApiKey> apiKey);

}
