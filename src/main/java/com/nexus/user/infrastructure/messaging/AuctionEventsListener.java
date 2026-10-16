package com.nexus.user.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.user.application.usecase.ApplyAuctionPaymentTimeoutPenaltyUseCase;
import com.nexus.common.events.AuctionPaymentTimeoutEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Makes the SRS's reputation Payment Timeout Penalty actually happen: auction-service already
// detects a winner who failed to pay within the deadline and emits AuctionPaymentTimeout, but
// until now nothing consumed it to apply a real penalty -- notification-service only recorded
// it for audit.
@Component
public class AuctionEventsListener {


    private final ObjectMapper objectMapper;
    private final ApplyAuctionPaymentTimeoutPenaltyUseCase applyAuctionPaymentTimeoutPenaltyUseCase;

    public AuctionEventsListener(ObjectMapper objectMapper,
                                  ApplyAuctionPaymentTimeoutPenaltyUseCase applyAuctionPaymentTimeoutPenaltyUseCase) {
        this.objectMapper = objectMapper;
        this.applyAuctionPaymentTimeoutPenaltyUseCase = applyAuctionPaymentTimeoutPenaltyUseCase;
    }

    // Exceptions are NOT caught here on purpose: KafkaErrorHandlingConfig retries them, then
    // moves the record to the dead-letter topic instead of losing it.
    @KafkaListener(topics = "auction-events")
    public void onMessage(String rawPayload) throws JsonProcessingException {
        String eventType = objectMapper.readTree(rawPayload).path("eventType").asText();
        if (!"AuctionPaymentTimeout".equals(eventType)) {
            return;
        }
        AuctionPaymentTimeoutEvent event = objectMapper.readValue(rawPayload, AuctionPaymentTimeoutEvent.class);
        applyAuctionPaymentTimeoutPenaltyUseCase.apply(event.getWinnerId(), event.getAuctionId());
    }
}
