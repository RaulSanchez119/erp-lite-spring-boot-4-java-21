package com.raulsanchez.erp_lite.persistence.mongo.adapters;

import com.raulsanchez.erp_lite.domain.ports.repositories.ProductCatalogRepositoryPort;
import com.raulsanchez.erp_lite.domain.views.ProductView;
import com.raulsanchez.erp_lite.persistence.mongo.mappers.ProductCatalogMapper;
import com.raulsanchez.erp_lite.persistence.mongo.repositories.ProductInCatalogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static com.raulsanchez.erp_lite.constants.CacheConstants.*;

@Repository
@Slf4j
@AllArgsConstructor
public class ProductCatalogRepositoryAdapter implements ProductCatalogRepositoryPort {

    private final ProductInCatalogRepository productInCatalogRepository;
    private final ProductCatalogMapper productCatalogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductView> findById(String id) {
        Object raw = this.redisTemplate.opsForValue().get(CACHE_PRODUCTS_BY_ID + id);

        if (raw != null) {
            log.debug("Found product with id in cache {}", id);
            return Optional.of(this.objectMapper.convertValue(raw, ProductView.class));
        }

        log.debug("Finding product with id in mongo {}", id);
        return this.productInCatalogRepository.findById(id)
                .map(productCatalogMapper::toView);
    }

    @Override
    public Optional<ProductView> findBySku(String sku) {
        Object raw = this.redisTemplate.opsForValue().get(CACHE_PRODUCTS_BY_SKU + sku);

        if (raw != null) {
            log.debug("Found product with sku in cache {}", sku);
            return Optional.of(this.objectMapper.convertValue(raw, ProductView.class));
        }

        log.debug("Finding product with sku in mongo {}", sku);

        return this.productInCatalogRepository.findBySku(sku)
                .map(productCatalogMapper::toView);
    }

    @Override
    public List<ProductView> findByText(String text) {
        log.info("Find product by text: {}", text);

        return this.productInCatalogRepository.findByTextAndActive(text)
                .stream().map(productCatalogMapper::toView)
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductView> findByCategory(String category) {
        Object raw = this.redisTemplate.opsForList().range(CACHE_PRODUCTS_BY_CATEGORY + category, 0, -1);

        if (raw != null) {
            log.debug("Found product with category in cache {}", category);
            return this.objectMapper.convertValue(raw,
                    this.objectMapper.getTypeFactory().constructCollectionType(List.class, ProductView.class));
        }

        log.debug("Finding product with category in mongo {}", category);

        return this.productInCatalogRepository.findByCategoryIdAndActiveTrue(category)
                .stream().map(productCatalogMapper::toView)
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductView> findActive() {
        Object raw = this.redisTemplate.opsForList().range(CACHE_PRODUCTS_ACTIVE, 0, -1);

        if (raw != null) {
            log.debug("Found product active");
            return this.objectMapper.convertValue(raw,
                    this.objectMapper.getTypeFactory().constructCollectionType(List.class, ProductView.class));
        }

        log.debug("Finding products active");

        return this.productInCatalogRepository.findByActiveTrueOrderByIdAsc()
                .stream().map(productCatalogMapper::toView)
                .toList();
    }
}