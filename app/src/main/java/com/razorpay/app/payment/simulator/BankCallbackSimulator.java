package com.razorpay.app.payment.simulator;

import com.razorpay.app.common.enums.PaymentStatus;
import com.razorpay.app.payment.entity.Payment;
import com.razorpay.app.payment.repository.PaymentRepository;
import com.razorpay.app.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallback(){
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);
        List<Payment> candidates = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);
        if(candidates.isEmpty()) return;
        for(Payment payment : candidates){
            simulatorCallback(payment);
        }

    }

    private void simulatorCallback(Payment payment) {

    }

}
