package com.razorpay.app.merchant.service;

import com.razorpay.app.merchant.dto.request.CreateApiKeyRequest;
import com.razorpay.app.merchant.dto.response.ApiKeyCreateResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface ApiKeyService {
    @Nullable ApiKeyCreateResponse create(UUID merchantId, @Valid CreateApiKeyRequest request);
}
