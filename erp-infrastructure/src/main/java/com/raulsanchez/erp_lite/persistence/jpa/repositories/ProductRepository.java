package com.raulsanchez.erp_lite.persistence.jpa.repositories;

import com.raulsanchez.erp_lite.persistence.jpa.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Optional<ProductEntity> findBySku(String sku);
}
