package com.razorpay.app.payment.service.impl;

import com.razorpay.app.common.enums.EventAggregateType;
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
import com.razorpay.app.payment.outbox.OutboxEventPublisher;
import com.razorpay.app.payment.repository.OrderRepository;
import com.razorpay.app.payment.repository.PaymentRepository;
import com.razorpay.app.payment.service.PaymentService;
import com.razorpay.app.payment.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
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
    private final OutboxEventPublisher outboxEventPublisher;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest requestDto) {

//        OrderRecord order = orderRepository.findByIdAndMerchantId(requestDto.orderId(), merchantId).orElseThrow(()-> new ResourceNotFoundException("Order", requestDto.orderId()));
        OrderRecord order = orderRepository.findByIdAndMerchantIdForUpdate(requestDto.orderId(), merchantId).orElseThrow(()-> new ResourceNotFoundException("Order", requestDto.orderId()));

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
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
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
        outboxEventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_CREATED",
                Map.of("orderId", order.getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", merchantId.toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
//        Payment payment =  paymentRepository.findByIdAndMerchantId(paymentId, merchantId).orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        Payment payment =  paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId).orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

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
        outboxEventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", merchantId.toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve,
                                     String bankRef, String errorCode, String errorDescription) {
//        Payment payment = paymentRepository.findById(paymentId).orElseThrow(()-> new ResourceNotFoundException("Payment", paymentId));
        Payment payment = paymentRepository.findByIdForUpdate(paymentId).orElseThrow(()-> new ResourceNotFoundException("Payment", paymentId));

        if(payment.getStatus() != PaymentStatus.AUTHORIZING){
            return;
        }

        OrderRecord orderRecord =payment.getOrder();

        if(approve){
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);
            if (captureResult instanceof PaymentResult.Success success){
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);
            }else if(captureResult instanceof PaymentResult.Failure failure){
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorCodeDescription());
            }
        }else{
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        paymentRepository.save(payment);
        orderRepository.save(orderRecord);
        outboxEventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_CREATED",
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()));
    }


}
