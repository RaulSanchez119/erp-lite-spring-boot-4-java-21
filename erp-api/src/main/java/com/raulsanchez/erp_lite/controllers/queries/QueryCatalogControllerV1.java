package com.raulsanchez.erp_lite.controllers.queries;

import com.raulsanchez.erp_lite.domain.views.CatalogView;
import com.raulsanchez.erp_lite.domain.views.ItemsView;
import com.raulsanchez.erp_lite.dtos.BaseResponseWrapper;
import com.raulsanchez.erp_lite.enums.CatalogType;
import com.raulsanchez.erp_lite.exceptions.QueryException;
import com.raulsanchez.erp_lite.paths.ApiPaths;
import com.raulsanchez.erp_lite.queries.FindCatalogByTypeQuery;
import com.raulsanchez.erp_lite.queries.FindCatalogItemByCodeQuery;
import com.raulsanchez.erp_lite.queries.FindCatalogItemsByTypeQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = ApiPaths.QUERIES_CATALOGS)
@RequiredArgsConstructor
public class QueryCatalogControllerV1 {

    private final FindCatalogByTypeQuery findCatalogByTypeQuery;
    private final FindCatalogItemsByTypeQuery findCatalogItemsByTypeQuery;
    private final FindCatalogItemByCodeQuery findCatalogItemByCodeQuery;

    @GetMapping(path = "/{type}")
    public ResponseEntity<BaseResponseWrapper<CatalogView>> getByType(@PathVariable String type) {
        log.info("GET catalog by type: {}", type);

        CatalogType catalogType = CatalogType.valueOf(type.toUpperCase());

        CatalogView response = this.findCatalogByTypeQuery.execute(catalogType)
                .orElseThrow(() -> new QueryException("Catalog with type " + type + " not found"));

        return ResponseEntity.ok(BaseResponseWrapper.of(response));
    }

    @GetMapping(path = "/{type}/items")
    public ResponseEntity<BaseResponseWrapper<List<ItemsView>>> getItemsByType( @PathVariable String type) {
        log.info("GET catalog items by type: {}", type);

        CatalogType catalogType = CatalogType.valueOf(type.toUpperCase());

        List<ItemsView> response = this.findCatalogItemsByTypeQuery.execute(catalogType);

        return ResponseEntity.ok(BaseResponseWrapper.of(response));
    }


    @GetMapping(path = "/{type}/items", params = "code")
    public ResponseEntity<BaseResponseWrapper<ItemsView>> getItemByTypeAndCode( @PathVariable String type, @RequestParam String code) {
        log.info("GET catalog item by type: {} and code: {}", type, code);

        CatalogType catalogType = CatalogType.valueOf(type.toUpperCase());

        ItemsView response = this.findCatalogItemByCodeQuery.execute(catalogType, code)
                .orElseThrow(() -> new QueryException("Item with code " + code + " not found for type " + type));

        return ResponseEntity.ok(BaseResponseWrapper.of(response));
    }
}