package com.razorpay.app.merchant.dto.response;


import com.razorpay.app.common.enums.BusinessType;
import com.razorpay.app.common.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
