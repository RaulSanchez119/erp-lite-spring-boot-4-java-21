package com.raulsanchez.erp_lite.domain.ports.repositories;

import com.raulsanchez.erp_lite.domain.entities.product.ProductId;
import com.raulsanchez.erp_lite.domain.entities.product.ProductRoot;

import java.util.Optional;


/**
 *  Port for storage or consult Products
 */
public interface ProductRepositoryPort {

    ProductRoot save(ProductRoot product);
    Optional<ProductRoot> findAllById(ProductId id);
    Optional<ProductRoot> findBySku(String sku);
    void delete(ProductRoot product);
}
