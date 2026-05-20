package com.raulsanchez.erp_lite.persistence.mongo.repositories;

import com.raulsanchez.erp_lite.persistence.mongo.documents.ProductCatalogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductCatalogRepository extends MongoRepository<ProductCatalogDocument, String> {
}
