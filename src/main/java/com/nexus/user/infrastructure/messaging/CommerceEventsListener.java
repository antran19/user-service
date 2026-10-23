package com.nexus.user.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.events.OrderShippedEvent;
import com.nexus.user.application.usecase.RecordRatableTransactionUseCase;
import com.nexus.user.domain.model.TransactionType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// A rating is only accepted for a transaction that really happened between the two people. The
// order service says so when a seller ships: this listener keeps this service's own copy of those
// facts (no call to commerce-service when someone rates). An order paid through an auction is
// recorded twice, as the ORDER and as the AUCTION, because a rating may name either id.
@Component
public class CommerceEventsListener {

    private final ObjectMapper objectMapper;
    private final RecordRatableTransactionUseCase recordRatableTransactionUseCase;

    public CommerceEventsListener(ObjectMapper objectMapper, RecordRatableTransactionUseCase recordRatableTransactionUseCase) {
        this.objectMapper = objectMapper;
        this.recordRatableTransactionUseCase = recordRatableTransactionUseCase;
    }

    // Exceptions are NOT caught here on purpose: NexusKafkaAutoConfiguration (common-kafka) retries them, then
    // moves the record to the dead-letter topic instead of losing it.
    @KafkaListener(topics = "commerce-events")
    public void onMessage(String rawPayload) throws JsonProcessingException {
        String eventType = objectMapper.readTree(rawPayload).path("eventType").asText();
        if (!"OrderShipped".equals(eventType)) {
            return;
        }
        OrderShippedEvent event = objectMapper.readValue(rawPayload, OrderShippedEvent.class);
        recordRatableTransactionUseCase.record(event.getOrderId(), TransactionType.ORDER, event.getBuyerId(),
                event.getSellerId(), event.getShippedAt());
        if (event.getAuctionId() != null) {
            recordRatableTransactionUseCase.record(event.getAuctionId(), TransactionType.AUCTION, event.getBuyerId(),
                    event.getSellerId(), event.getShippedAt());
        }
    }
}
