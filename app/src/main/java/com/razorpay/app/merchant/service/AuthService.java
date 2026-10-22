package com.razorpay.app.merchant.service;

import com.razorpay.app.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.app.merchant.dto.response.MerchantResponse;


public interface AuthService {
    MerchantResponse signUp(MerchantSignupRequest request);
}
