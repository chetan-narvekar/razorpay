package com.razorpay.app.merchant.dto.response;

import com.razorpay.app.common.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponse(UUID id, String keyId, String keySecret, Environment environment) {
}
