package com.raulsanchez.erp_lite.domain.entities.product.events;

import com.raulsanchez.erp_lite.domain.common.DomainEvent;
import com.raulsanchez.erp_lite.domain.entities.product.*;
import com.raulsanchez.erp_lite.domain.shared.Money;

import java.time.Instant;

/**
 * Emitted when a new product is created.
 * TRIGGERS sync to MongoDB (CQRS).
 *
 * @param productId the product identifier
 * @param sku       the product SKU
 * @param name      the product name
 * @param price     the product price
 * @param timestamp the event timestamp
 */
public record ProductCreated(
        ProductId productId,
        SKU sku,
        ProductName name,
        Money price,
        Instant timestamp,
        String description,
        Stock stock,
        CategoryReference category,
        ProductImage image,
        boolean active
) implements DomainEvent {
}
