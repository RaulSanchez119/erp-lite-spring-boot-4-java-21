package com.raulsanchez.erp_lite.domain.ports.repositories;

import com.raulsanchez.erp_lite.domain.views.ProductView;

import java.util.List;
import java.util.Optional;

public interface ProductCatalogRepositoryPort {

    Optional<ProductView> findById(String id);
    Optional<ProductView> findBySku(String sku);
    List<ProductView> findByText(String text);
    List<ProductView> findByCategory(String category);
    List<ProductView> findActive();
}
