package com.nexus.user.application.port.out;

import com.nexus.common.events.DomainEvent;

public interface EventPublisherPort {
    void publish(DomainEvent event);
}
