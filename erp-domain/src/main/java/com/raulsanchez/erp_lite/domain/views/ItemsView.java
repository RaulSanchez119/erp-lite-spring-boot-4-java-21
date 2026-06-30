package com.raulsanchez.erp_lite.domain.views;

/**
 * Read model representation of a Items.
 * Used for queries (CQRS read side).
 * This is a simplified view optimized for display.
 */
public record ItemsView(
        String code,
        String value,
        String description,
        Integer displayOrder
) {
}