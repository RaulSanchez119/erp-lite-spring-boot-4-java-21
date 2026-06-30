package com.raulsanchez.erp_lite.persistence.mongo.mappers;

import com.raulsanchez.erp_lite.persistence.mongo.documents.ProductInCatalogDocument;
import com.raulsanchez.erp_lite.domain.views.ProductView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProductCatalogMapper {

    @Mapping(source = "currency", target = "money")
    ProductView toView(ProductInCatalogDocument document);
}