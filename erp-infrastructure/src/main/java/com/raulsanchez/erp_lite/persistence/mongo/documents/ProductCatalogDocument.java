package com.raulsanchez.erp_lite.persistence.mongo.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document(collection = "product_documents")
@NoArgsConstructor
@AllArgsConstructor
public class ProductCatalogDocument {

    @Id
    private String id;

    private Boolean active;

    private String categoryId;

    private String categoryName;

    private String sku;

    private String name;

    private String description;

    private String imageUrl;

    private BigDecimal price;

    private String currency;

    private Integer stock;

    private ProductSpecifications specifications;

    private List<String> tags;

    private Instant createdAt;

    private Instant updatedAt;
}