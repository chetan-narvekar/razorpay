package com.razorpay.app.vault.controller;


import com.razorpay.app.merchant.security.MerchantContext;
import com.razorpay.app.vault.dto.request.TokenizeRequest;
import com.razorpay.app.vault.dto.response.TokenizeResponse;
import com.razorpay.app.vault.service.VaultService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
@SecurityRequirement(name = "apiKeyAuth")

public class VaultController {
    private final MerchantContext merchantContext;
    private final VaultService vaultService;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody TokenizeRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(vaultService.tokenize(request, merchantContext.getMerchantId()));
    }
}
