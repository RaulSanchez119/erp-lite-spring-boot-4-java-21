package com.raulsanchez.erp_lite.persistence.mongo.repositories;

import com.raulsanchez.erp_lite.enums.CatalogType;
import com.raulsanchez.erp_lite.persistence.mongo.documents.CatalogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CatalogRepository extends MongoRepository<CatalogDocument, String> {

    Optional<CatalogDocument> findByCatalogType(CatalogType type);
}
