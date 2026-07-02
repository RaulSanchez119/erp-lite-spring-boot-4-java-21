package com.raulsanchez.erp_lite.persistence.mongo.adapters;

import com.raulsanchez.erp_lite.domain.ports.repositories.CatalogRepositoryPort;
import com.raulsanchez.erp_lite.domain.views.CatalogView;
import com.raulsanchez.erp_lite.domain.views.ItemsView;
import com.raulsanchez.erp_lite.enums.CatalogType;
import com.raulsanchez.erp_lite.persistence.mongo.mappers.CatalogMapper;
import com.raulsanchez.erp_lite.persistence.mongo.repositories.CatalogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static com.raulsanchez.erp_lite.constants.CacheConstants.CACHE_CATALOGS_BY_TYPE;

@Repository
@Slf4j
@AllArgsConstructor
public class CatalogRepositoryAdapter implements CatalogRepositoryPort {

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<CatalogView> findByType(CatalogType type) {
        log.info("Find catalog by type: {}", type);

        Object raw = redisTemplate.opsForValue().get(CACHE_CATALOGS_BY_TYPE + type.name());
        if (raw != null) {
            CatalogView cached = objectMapper.convertValue(raw, CatalogView.class);
            log.info("Found catalog in cache: {}", cached);
            return Optional.of(cached);
        }

        return catalogRepository.findByCatalogType(type)
                .map(catalogMapper::toView);
    }

    @Override
    public List<ItemsView> findItemsByType(CatalogType type) {
        log.info("Find items catalog by type: {}", type);

        Object raw = this.redisTemplate.opsForValue().get(CACHE_CATALOGS_BY_TYPE + type.name());

        if (raw != null) {
            CatalogView cached = objectMapper.convertValue(raw, CatalogView.class);

            log.info("Found catalog items in cache, total: {}", cached.items().size());
            return cached.items();
        }

        return catalogRepository.findByCatalogType(type)
                .map(doc -> doc.getItems()
                        .stream()
                        .map(catalogMapper::toItemView)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public Optional<ItemsView> findItemByTypeAndCode(CatalogType type, String code) {
        log.info("Find items catalog by type: {} & code: {}", type, code);

        return catalogRepository.findByCatalogType(type)
                .flatMap(doc -> doc.getItems()
                        .stream()
                        .filter(item -> item.code().equals(code))
                        .findFirst()
                        .map(catalogMapper::toItemView));
    }
}