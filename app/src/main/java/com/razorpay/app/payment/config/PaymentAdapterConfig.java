package com.razorpay.app.payment.config;

import com.razorpay.app.common.enums.PaymentMethod;
import com.razorpay.app.payment.gateway.PaymentAdapter;
import com.razorpay.app.payment.gateway.adapter.CardPaymentAdapter;
import com.razorpay.app.payment.gateway.adapter.NetBankingAdapter;
import com.razorpay.app.payment.gateway.adapter.UpiPaymentAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentAdapterConfig {

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, new CardPaymentAdapter(),
                PaymentMethod.NETBANKING, new NetBankingAdapter(),
                PaymentMethod.UPI, new UpiPaymentAdapter()
        );
    }

}
