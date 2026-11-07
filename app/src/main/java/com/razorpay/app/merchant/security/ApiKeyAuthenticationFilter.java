package com.razorpay.app.merchant.security;

import com.razorpay.app.common.exceptions.RateLimitException;
import com.razorpay.app.common.ratelimit.RateLimitResult;
import com.razorpay.app.common.ratelimit.RateLimiter;
import com.razorpay.app.merchant.cache.ApiKeyCache;
import com.razorpay.app.merchant.cache.ApiKeyCacheEntry;
import com.razorpay.app.merchant.entity.ApiKey;
import com.razorpay.app.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:30}")
    private Integer requestPerMinutes;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if(header == null || !header.startsWith(BASIC_PREFIX)){
                filterChain.doFilter(request, response);
                return;
            }

            String[] credentials = decode(header);
            if( credentials == null ){
                throw  new BadRequestException("Malformed API KEY header");
            }
            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKeyCacheEntry apiKey = apiKeyCache.get(keyId).orElseGet(() -> loadAndCache(keyId));

            if( apiKey == null || !apiKey.enabled() || !secretMatcher(rawSecret, apiKey)){
                throw new BadRequestException("Invalid or missing api key");
            }
            RateLimitResult result = rateLimiter.check("apiKey:"+keyId, requestPerMinutes, 60);
            if(!result.isAllowed()){
                throw new RateLimitException("Too many requests ", result.retryAfterSeconds());
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(requestPerMinutes));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));


            var auth = new UsernamePasswordAuthenticationToken(keyId, null, List.of(new SimpleGrantedAuthority("API_KEY_ROLE")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setMerchantId(apiKey.merchantId());
            merchantContext.setKeyID(apiKey.keyId());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private ApiKeyCacheEntry loadAndCache(String keyId){
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);
        if( apiKey == null) return null;
        ApiKeyCacheEntry apiKeyCacheEntry = new ApiKeyCacheEntry(apiKey.getKeyId(), apiKey.getKeySecretHash(), apiKey.getPreviousKeySecretHash(), apiKey.getGracePeriodExpiresAt(), apiKey.getMerchant().getId(),apiKey.getEnvironment(), apiKey.isEnabled());
        apiKeyCache.put(keyId, apiKeyCacheEntry);
        return apiKeyCacheEntry;
    }

    private String[] decode( String header){
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        int colon = decoded.indexOf(":");
        if( colon < 1) return  null;
        return new String[]{decoded.substring(0, colon), decoded.substring(colon+1 )};
    }

    private boolean secretMatcher(String rawSecret , ApiKeyCacheEntry apiKey){
        if (BCRYPT.matches(rawSecret, apiKey.keySecretHash())){
            return true;
        }
        boolean isInGracePeriod = apiKey.gracePeriodExpiresAt() != null && LocalDateTime.now().isBefore(apiKey.gracePeriodExpiresAt());
        return isInGracePeriod && apiKey.previousKeySecretHash() != null && (BCRYPT.matches(rawSecret, apiKey.previousKeySecretHash()));
    }
}
