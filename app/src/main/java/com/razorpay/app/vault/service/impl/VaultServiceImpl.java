package com.razorpay.app.vault.service.impl;

import com.razorpay.app.common.entity.Money;
import com.razorpay.app.common.enums.CardBrand;
import com.razorpay.app.common.exceptions.ResourceNotFoundException;
import com.razorpay.app.common.util.RandomizerUtil;
import com.razorpay.app.payment.processor.PaymentProcessor;
import com.razorpay.app.payment.processor.PaymentProcessorRouter;
import com.razorpay.app.payment.processor.dto.PaymentProcessorRequest;
import com.razorpay.app.payment.processor.dto.PaymentProcessorResponse;
import com.razorpay.app.vault.config.VaultEncryptionConfig;
import com.razorpay.app.vault.dto.request.TokenizeRequest;
import com.razorpay.app.vault.dto.response.TokenizeResponse;
import com.razorpay.app.vault.entity.CardToken;
import com.razorpay.app.vault.entity.VaultCard;
import com.razorpay.app.vault.repository.CardTokenRepository;
import com.razorpay.app.vault.repository.VaultCardRepository;
import com.razorpay.app.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {
        String lastFour = request.pan().substring(request.pan().length()-4);
        String bin = request.pan().substring(0, 4);
        CardBrand cardBrand = detectBrand(request.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncrypter(dek).encrypt(request.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = vaultCardRepository.save(VaultCard.builder()
                .brand(cardBrand)
                .expiryYear(request.expiryYear().toString())
                .expiryMonth(request.expiryMonth().toString())
                .bin(bin)
                .lastFour(lastFour)
                .encryptedDek(encryptedDek)
                .encryptedPan(encryptedPan)
                .cardHolderName(request.cardHolderName())
                .build());

        String token = "tok_" + RandomizerUtil.randomBase64(32);

        cardTokenRepository.save(CardToken.builder()
                .vaultCard(vaultCard)
                .token(token)
                .customer(request.customerId())
                .merchant(merchantId)
                .build());
        return new TokenizeResponse(token, lastFour, cardBrand, request.expiryMonth(), request.expiryYear());
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token).orElseThrow(()-> new ResourceNotFoundException("CardToken", token));
        VaultCard vaultCard = cardToken.getVaultCard();
        byte[] panBytes = null;
        try {
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());
            panBytes = VaultEncryptionConfig.panEncrypter(dek).decrypt(vaultCard.getEncryptedPan());
            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.card(paymentId, pan, expiry, amount, methodDetails);
            return paymentProcessorRouter.charge(paymentProcessorRequest);
        }catch (Exception e){
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        }finally{
            if(panBytes!=null)Arrays.fill(panBytes, (byte) 0);
        }
    }


    private CardBrand detectBrand(String pan) {
        if (pan.startsWith("4")) return CardBrand.VISA;
        if (pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if (pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
