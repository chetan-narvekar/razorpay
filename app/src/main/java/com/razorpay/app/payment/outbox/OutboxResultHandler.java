package com.razorpay.app.payment.outbox;

import com.razorpay.app.common.enums.OutboxStatus;
import com.razorpay.app.payment.entity.OutboxEvent;
import com.razorpay.app.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxResultHandler {

    private static final int MAX_ATTEMPTS = 3;

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void handleEventPublished(OutboxEvent event) {

        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());

        outboxEventRepository.save(event);
    }

    @Transactional
    public void handleEventFailed(OutboxEvent event, String message) {

        int currentAttempts = event.getAttempts() == null
                ? 0
                : event.getAttempts();

        event.setAttempts(currentAttempts + 1);

        String errorMessage = message == null
                ? "Unknown Kafka publishing error"
                : message;

        event.setLastError(
                errorMessage.length() > 1000
                        ? errorMessage.substring(0, 1000)
                        : errorMessage
        );

        if (event.getAttempts() >= MAX_ATTEMPTS) {
            event.setStatus(OutboxStatus.FAILED);
        }

        outboxEventRepository.save(event);

        log.info(
                "Outbox event failure recorded. EventId: {}, Attempts: {}, Status: {}",
                event.getId(),
                event.getAttempts(),
                event.getStatus()
        );
    }
}