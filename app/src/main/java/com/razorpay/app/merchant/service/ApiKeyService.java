package com.razorpay.app.merchant.service;

import com.razorpay.app.merchant.dto.request.CreateApiKeyRequest;
import com.razorpay.app.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.app.merchant.dto.response.ApiKeyResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse create(UUID merchantId, @Valid CreateApiKeyRequest request);

    List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotateKey(UUID merchantId, UUID keyId);
}
