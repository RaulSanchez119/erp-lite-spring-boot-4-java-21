package com.raulsanchez.erp_lite.domain.order.events;

import com.raulsanchez.erp_lite.domain.common.DomainEvent;
import com.raulsanchez.erp_lite.domain.order.OrderId;

import java.time.Instant;

/**
 * Emitted when order transitions SHIPPED -> DELIVERED.
 * Final state.
 *
 * @param orderId   the order identifier
 * @param timestamp the event timestamp
 */
public record OrderDelivered(
        OrderId orderId,
        Instant timestamp
) implements DomainEvent {
}
