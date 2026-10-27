package com.razorpay.app.payment.config;

import com.razorpay.app.common.enums.PaymentMethod;
import com.razorpay.app.payment.gateway.PaymentAdapter;
import com.razorpay.app.payment.gateway.adapter.CardPaymentAdapter;
import com.razorpay.app.payment.gateway.adapter.NetBankingAdapter;
import com.razorpay.app.payment.gateway.adapter.UpiPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {

    private final CardPaymentAdapter cardPaymentAdapter;
    private final NetBankingAdapter netBankingAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.NETBANKING, netBankingAdapter,
                PaymentMethod.UPI, upiPaymentAdapter
        );
    }

}
