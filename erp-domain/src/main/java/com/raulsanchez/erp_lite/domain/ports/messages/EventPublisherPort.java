package com.raulsanchez.erp_lite.domain.ports.messages;

import com.raulsanchez.erp_lite.domain.common.DomainEvent;

public interface EventPublisherPort {

    void publish(DomainEvent event);
}