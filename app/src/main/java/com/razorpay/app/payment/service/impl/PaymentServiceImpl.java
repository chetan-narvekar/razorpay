package com.razorpay.app.payment.service.impl;

import com.razorpay.app.common.enums.OrderStatus;
import com.razorpay.app.common.enums.PaymentEvent;
import com.razorpay.app.common.enums.PaymentStatus;
import com.razorpay.app.common.exceptions.BusinessRuleViolationException;
import com.razorpay.app.common.exceptions.ResourceNotFoundException;
import com.razorpay.app.payment.dto.request.PaymentInitRequest;
import com.razorpay.app.payment.dto.response.PaymentResponse;
import com.razorpay.app.payment.entity.OrderRecord;
import com.razorpay.app.payment.entity.Payment;
import com.razorpay.app.payment.gateway.PaymentGatewayRouter;
import com.razorpay.app.payment.gateway.dto.PaymentRequest;
import com.razorpay.app.payment.gateway.dto.PaymentResult;
import com.razorpay.app.payment.mapper.PaymentMapper;
import com.razorpay.app.payment.repository.OrderRepository;
import com.razorpay.app.payment.repository.PaymentRepository;
import com.razorpay.app.payment.service.PaymentService;
import com.razorpay.app.payment.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest requestDto) {

        OrderRecord order = orderRepository.findByIdAndMerchantId(requestDto.orderId(), merchantId).orElseThrow(()-> new ResourceNotFoundException("Order", requestDto.orderId()));
        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE", "Order cannot accept in status: "+ order.getOrderStatus());
        }
        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);
        Payment payment = Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(requestDto.method())
                .idempotencyKey(UUID.randomUUID().toString()) //TODO: idempotency
                .methodDetails(requestDto.methodDetails())
                .build();

        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(payment.getId(),
                requestDto.orderId(), merchantId, order.getAmount(), requestDto.method(),
                requestDto.methodDetails());

        PaymentResult result = paymentGatewayRouter.initiate(paymentRequest);

        switch(result){
            case PaymentResult.Failure failure-> {
//                payment.setStatus(PaymentStatus.FAILED);
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorCodeDescription());
            }
            case PaymentResult.Pending pending->{
                payment.setProcessorReference(pending.registrationRef());
            }
            case PaymentResult.Success success -> {
                log.warn("Invalid state");
                return null;
            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        Payment payment =  paymentRepository.findByIdAndMerchantId(paymentId, merchantId).orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);
        if(paymentResult instanceof PaymentResult.Success success){
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
        } else if (paymentResult instanceof  PaymentResult.Failure failure) {
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorCodeDescription());
        }

        payment = paymentRepository.save(payment);
        return paymentMapper.toResponse(payment);
    }
}
