package com.razorpay.app.merchant.service.impl;

import com.razorpay.app.common.exceptions.ResourceNotFoundException;
import com.razorpay.app.common.util.RandomizerUtil;
import com.razorpay.app.merchant.dto.request.CreateApiKeyRequest;
import com.razorpay.app.merchant.dto.response.ApiKeyCreateResponse;
import com.razorpay.app.merchant.dto.response.ApiKeyResponse;
import com.razorpay.app.merchant.entity.ApiKey;
import com.razorpay.app.merchant.entity.Merchant;
import com.razorpay.app.merchant.mapper.ApiKeyMapper;
import com.razorpay.app.merchant.repository.ApiKeyRepository;
import com.razorpay.app.merchant.repository.MerchantRepository;
import com.razorpay.app.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final ApiKeyMapper apiKeyMapper;
    @Override
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(()-> new ResourceNotFoundException("Merchant", merchantId));
        String keyId = "rzp_"+request.environment().name().toUpperCase()+"_"+RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40);
        ApiKey apiKey = ApiKey.builder().merchant(merchant).keyId(keyId).keySecretHash(rawSecret).environment(request.environment()).build();
        apiKey = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(apiKey.getId(), keyId, rawSecret, request.environment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        return apiKeyMapper.toResponse(apiKeyRepository.findByMerchant_Id(merchantId));
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId).filter(k->k.getMerchant().getId().equals(merchantId)).orElseThrow(()-> new ResourceNotFoundException("APIKEY", keyId));
        apiKey.setEnabled(false);
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse rotateKey(UUID merchantId, UUID keyId) {

        ApiKey key = apiKeyRepository.findById(keyId)
                .filter(k->k.getMerchant().getId().equals(merchantId))
                .orElseThrow(()-> new ResourceNotFoundException("APIKEY", keyId));
        if(!key.isEnabled()) throw new RuntimeException(("Cannot rotate a disabled key"));
        String rawSecret = RandomizerUtil.randomBase64(40);
        key.setPreviousKeySecretHash(key.getKeySecretHash());
        key.setKeySecretHash(rawSecret);
        key.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));
        key.setRotatedAt(LocalDateTime.now());

        return new ApiKeyCreateResponse(
                key.getId(), key.getKeyId(), rawSecret, key.getEnvironment());
    }


}
