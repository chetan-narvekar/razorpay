package com.razorpay.app.payment.outbox;

import com.razorpay.app.common.config.KafkaProperties;
import com.razorpay.app.common.enums.OutboxStatus;
import com.razorpay.app.payment.entity.OutboxEvent;
import com.razorpay.app.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String , Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OutboxResultHandler outboxResultHandler;

    @Scheduled(fixedDelay = 5000)
    public void poll() {

        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                String topic = kafkaProperties.topicFor(outboxEvent.getAggregateType());
                String key = extractMerchantId(outboxEvent.getPayload());
                Map<String, Object> envelope = Map.of(
                        "eventType", outboxEvent.getEventType(),
                        "aggregateType", outboxEvent.getAggregateType().name(),
                        "aggregateId", outboxEvent.getAggregateId().toString(),
                        "data", outboxEvent.getPayload()
                );

                kafkaTemplate.send(topic, key, envelope)
                        .get(5, TimeUnit.SECONDS);

                outboxResultHandler.handleEventPublished(outboxEvent);
            } catch (Exception e) {

                log.error(
                        "Outbox event failed. EventId: {}, Current attempts: {}",
                        outboxEvent.getId(),
                        outboxEvent.getAttempts()
                );

                outboxResultHandler.handleEventFailed(
                        outboxEvent,
                        e.getMessage()
                );
            }
        }
    }

    private String extractMerchantId(Map<String, Object> payload){
        Object value = payload.get("merchantId");
        return value != null ? value.toString() : "unknown";
    }

}
