package com.nexus.user.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexus.common.events.OrderShippedEvent;
import com.nexus.user.application.usecase.RecordRatableTransactionUseCase;
import com.nexus.user.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CommerceEventsListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final RecordRatableTransactionUseCase recordUseCase = mock(RecordRatableTransactionUseCase.class);
    private final CommerceEventsListener listener = new CommerceEventsListener(objectMapper, recordUseCase);

    private String json(OrderShippedEvent event) throws Exception {
        return objectMapper.writeValueAsString(event);
    }

    @Test
    void onMessage_aDirectOrderShipped_isRecordedAsOneOrderTransaction() throws Exception {
        Instant shippedAt = Instant.parse("2026-10-23T10:00:00Z");

        listener.onMessage(json(new OrderShippedEvent("order-1", null, "buyer-1", "seller-1", shippedAt)));

        verify(recordUseCase).record("order-1", TransactionType.ORDER, "buyer-1", "seller-1", shippedAt);
        verify(recordUseCase, times(1)).record(any(), any(), any(), any(), any());
    }

    @Test
    void onMessage_anAuctionOrderShipped_isRecordedAsBothTheOrderAndTheAuction() throws Exception {
        Instant shippedAt = Instant.parse("2026-10-23T10:00:00Z");

        listener.onMessage(json(new OrderShippedEvent("order-2", "auction-9", "buyer-1", "seller-1", shippedAt)));

        verify(recordUseCase).record("order-2", TransactionType.ORDER, "buyer-1", "seller-1", shippedAt);
        verify(recordUseCase).record("auction-9", TransactionType.AUCTION, "buyer-1", "seller-1", shippedAt);
    }

    @Test
    void onMessage_otherCommerceEvents_areIgnored() throws Exception {
        listener.onMessage("{\"eventId\":\"e\",\"eventType\":\"OrderPaid\",\"orderId\":\"o\"}");

        verify(recordUseCase, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void onMessage_malformedPayload_propagatesSoItGoesToTheDeadLetterTopic() {
        assertThatThrownBy(() -> listener.onMessage("{not valid json")).isInstanceOf(JsonProcessingException.class);
    }
}
