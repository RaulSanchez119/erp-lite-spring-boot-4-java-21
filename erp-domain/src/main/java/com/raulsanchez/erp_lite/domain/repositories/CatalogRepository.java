package com.raulsanchez.erp_lite.domain.repositories;

import com.raulsanchez.erp_lite.domain.catalog.CatalogItem;
import com.raulsanchez.erp_lite.domain.catalog.CatalogType;

import javax.xml.catalog.Catalog;
import java.util.List;
import java.util.Optional;

/**
* Port read-only for Catalog
 */
public interface CatalogRepository {

    Optional<Catalog> findByType(CatalogType type);

    List<CatalogItem> findItemsByType(CatalogType type);

    Optional<CatalogItem> findItemByTypeAndCode(CatalogType type, String code);
}
