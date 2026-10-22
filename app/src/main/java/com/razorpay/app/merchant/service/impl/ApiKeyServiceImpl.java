package com.razorpay.app.merchant.service.impl;

import com.razorpay.app.common.exceptions.ResourceNotFoundException;
import com.razorpay.app.merchant.dto.request.CreateApiKeyRequest;
import com.razorpay.app.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.app.merchant.entity.ApiKey;
import com.razorpay.app.merchant.entity.Merchant;
import com.razorpay.app.merchant.repository.ApiKeyRepository;
import com.razorpay.app.merchant.repository.MerchantRepository;
import com.razorpay.app.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;

    @Override
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(()-> new ResourceNotFoundException("Merchant", merchantId));
        String keyId = "rzp_"+request.environment().name().toUpperCase()+"big_random";
        String rawSecret = "big_random_secret";
        ApiKey apiKey = ApiKey.builder().merchant(merchant).keyId(keyId).keySecretHash(rawSecret).environment(request.environment()).build();
        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(apiKey.getId(), keyId, rawSecret, request.environment());
    }
}
