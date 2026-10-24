package com.razorpay.app.merchant.mapper;

import com.razorpay.app.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.app.merchant.dto.response.MerchantResponse;
import com.razorpay.app.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {
    Merchant toEntityFromSignUpRequest(MerchantSignupRequest merchantSignupRequest);

    MerchantResponse toResponse(Merchant merchant);
}
