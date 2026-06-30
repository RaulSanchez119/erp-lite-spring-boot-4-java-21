package com.raulsanchez.erp_lite.queries;

import com.raulsanchez.erp_lite.domain.ports.repositories.CatalogRepositoryPort;
import com.raulsanchez.erp_lite.domain.views.ItemsView;
import com.raulsanchez.erp_lite.enums.CatalogType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FindCatalogItemByCodeQuery {

    private final CatalogRepositoryPort catalogRepository;

    public Optional<ItemsView> execute(CatalogType catalogType, String code) {
        log.info("Execute FindCatalogItemByCodeQuery");

        return this.catalogRepository.findItemByTypeAndCode(catalogType, code);
    }

}