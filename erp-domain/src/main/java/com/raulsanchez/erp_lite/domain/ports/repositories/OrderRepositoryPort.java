package com.raulsanchez.erp_lite.domain.ports.repositories;

import com.raulsanchez.erp_lite.domain.entities.order.OrderId;
import com.raulsanchez.erp_lite.domain.entities.order.OrderRoot;
import com.raulsanchez.erp_lite.domain.entities.order.OrderNumber;
import com.raulsanchez.erp_lite.domain.shared.CustomerId;

import java.util.List;
import java.util.Optional;

/**
 *  Port for storage or consult Order
 */
public interface OrderRepositoryPort {

    OrderRoot save(OrderRoot order);
    Optional<OrderRoot> findById(OrderId id);
    Optional<OrderRoot> findAllByOrderNumber(OrderNumber orderNumber);
    List<OrderRoot> findByCustomerId(CustomerId customerId);
    void delete(OrderRoot order);
}
