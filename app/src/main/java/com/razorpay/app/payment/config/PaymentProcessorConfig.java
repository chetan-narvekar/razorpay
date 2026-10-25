package com.razorpay.app.payment.config;

import com.razorpay.app.common.enums.PaymentMethod;
import com.razorpay.app.payment.processor.PaymentProcessor;
import com.razorpay.app.payment.processor.startegy.CardPaymentProcessor;
import com.razorpay.app.payment.processor.startegy.NetBankingPaymentProcessor;
import com.razorpay.app.payment.processor.startegy.UpiPaymentProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentProcessorConfig {
    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.NETBANKING, new NetBankingPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor()
        );
    }
}
