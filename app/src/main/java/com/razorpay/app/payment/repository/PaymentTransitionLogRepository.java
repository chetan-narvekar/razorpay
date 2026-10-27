package com.razorpay.app.payment.repository;

import com.razorpay.app.payment.entity.PaymentTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentTransitionLogRepository extends JpaRepository<PaymentTransitionLog, UUID> {
}
