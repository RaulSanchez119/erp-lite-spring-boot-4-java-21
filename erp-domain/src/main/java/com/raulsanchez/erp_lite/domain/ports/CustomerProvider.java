package com.raulsanchez.erp_lite.domain.ports;

import com.raulsanchez.erp_lite.domain.customer.CustomerInfo;

import java.util.Optional;

/**
 *  Port for external service for JSONPlaceholder
 */
public interface CustomerProvider {

    Optional<CustomerInfo> findById(Long id);
    boolean existsById(Long id);
}
