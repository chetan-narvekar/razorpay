package com.razorpay.app.merchant.service;

import com.razorpay.app.merchant.dto.request.LoginRequest;
import com.razorpay.app.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.app.merchant.dto.response.LoginResponse;
import com.razorpay.app.merchant.dto.response.MerchantResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;


public interface AuthService {
    MerchantResponse signUp(MerchantSignupRequest request);

    LoginResponse  login(@Valid LoginRequest request);
}
