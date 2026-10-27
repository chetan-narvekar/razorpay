package com.razorpay.app.vault.controller;


import com.razorpay.app.vault.dto.request.TokenizeRequest;
import com.razorpay.app.vault.dto.response.TokenizeResponse;
import com.razorpay.app.vault.service.VaultService;
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
public class VaultController {
    UUID merchantId = UUID.fromString("9a7fcc42-1126-4b7f-aa3f-7eec256cf8d0");
    private final VaultService vaultService;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody TokenizeRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(vaultService.tokenize(request, merchantId));
    }
}
