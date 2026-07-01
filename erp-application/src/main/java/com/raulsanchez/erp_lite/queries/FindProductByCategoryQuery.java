package com.raulsanchez.erp_lite.queries;

import com.raulsanchez.erp_lite.domain.ports.repositories.ProductCatalogRepositoryPort;
import com.raulsanchez.erp_lite.domain.views.ProductView;
import com.raulsanchez.erp_lite.exceptions.QueryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FindProductByCategoryQuery {

    private final ProductCatalogRepositoryPort productCatalogRepository;


    public List<ProductView> execute(String category) {
        log.info("Execute FindProductBySkuQuery");

        try {
            return productCatalogRepository.findByCategory(category);
        } catch (RuntimeException e) {
            throw new QueryException("Error executing FindProductBySkuQuery");
        }

    }
}