package com.razorpay.app.payment.repository;

import com.razorpay.app.payment.entity.OrderRecord;
import com.razorpay.app.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrder_Id(OrderRecord order);
}
