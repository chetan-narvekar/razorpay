package com.razorpay.app.merchant.repository;


import com.razorpay.app.merchant.dto.response.ApiKeyResponse;
import com.razorpay.app.merchant.entity.ApiKey;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByMerchant_Id(UUID merchantId);
}
