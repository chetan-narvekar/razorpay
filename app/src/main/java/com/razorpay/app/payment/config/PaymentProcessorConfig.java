package com.razorpay.app.payment.config;

import com.razorpay.app.common.enums.PaymentMethod;
import com.razorpay.app.payment.processor.PaymentProcessor;
import com.razorpay.app.payment.processor.startegy.CardPaymentProcessor;
import com.razorpay.app.payment.processor.startegy.NetBankingPaymentProcessor;
import com.razorpay.app.payment.processor.startegy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;
    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.NETBANKING,netBankingPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor
        );
    }
}
