package com.razorpay.app.payment.outbox;

import com.razorpay.app.common.enums.EventAggregateType;
import com.razorpay.app.common.enums.OutboxStatus;
import com.razorpay.app.payment.entity.OutboxEvent;
import com.razorpay.app.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void publish(EventAggregateType aggregateType, UUID aggregateId, String eventType, Map<String, Object> payload){
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
