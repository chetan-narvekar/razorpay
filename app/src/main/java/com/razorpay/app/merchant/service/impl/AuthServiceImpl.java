package com.razorpay.app.merchant.service.impl;

import com.razorpay.app.common.enums.MerchantStatus;
import com.razorpay.app.common.enums.UserRole;
import com.razorpay.app.common.exceptions.DuplicateResourceException;
import com.razorpay.app.common.exceptions.ResourceNotFoundException;
import com.razorpay.app.merchant.dto.request.LoginRequest;
import com.razorpay.app.merchant.dto.request.MerchantSignupRequest;
import com.razorpay.app.merchant.dto.response.LoginResponse;
import com.razorpay.app.merchant.dto.response.MerchantResponse;
import com.razorpay.app.merchant.entity.AppUser;
import com.razorpay.app.merchant.entity.Merchant;
import com.razorpay.app.merchant.mapper.MerchantMapper;
import com.razorpay.app.merchant.repository.AppUserRepository;
import com.razorpay.app.merchant.repository.MerchantRepository;
import com.razorpay.app.merchant.security.JwtUtil;
import com.razorpay.app.merchant.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signUp(MerchantSignupRequest request) {

        if(merchantRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL", "Merchant already exists");
        }

        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();
        appUserRepository.save(appUser);

        return merchantMapper.toResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AppUser appUser = appUserRepository.findByEmail(request.email()).orElseThrow(()-> new ResourceNotFoundException("User", request.email()));
        String token = jwtUtil.generateAccessToken(request.email(), appUser.getMerchant().getId(), appUser.getRole().name());
        return new LoginResponse(token);
    }

}
