package com.raulsanchez.erp_lite.controllers.queries;

import com.raulsanchez.erp_lite.domain.views.ProductView;
import com.raulsanchez.erp_lite.dtos.BaseResponseWrapper;
import com.raulsanchez.erp_lite.exceptions.QueryException;
import com.raulsanchez.erp_lite.paths.ApiPaths;
import com.raulsanchez.erp_lite.queries.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiPaths.QUERIES_PRODUCTS)
@RequiredArgsConstructor
public class QueryProductsControllersV1 {

    private final FindProductByCategoryQuery findProductByCategoryQuery;
    private final FindProductByIdQuery findProductByIdQuery;
    private final FindProductActiveQuery findProductActiveQuery;
    private final FindProductByTextQuery findProductByTextQuery;
    private final FindProductBySkuQuery findProductBySkuQuery;

    @GetMapping(path = "/{id}")
    public ResponseEntity<BaseResponseWrapper<ProductView>> getById(@PathVariable String id) {
        log.info("GET product by id {}", id);

        ProductView response = this.findProductByIdQuery.execute(id)
                .orElseThrow(() -> new QueryException("Product with id " + id + " not found"));

        return ResponseEntity.ok(BaseResponseWrapper.of(response));
    }

    @GetMapping(params = "sku")
    public ResponseEntity<BaseResponseWrapper<ProductView>> getBySku(@RequestParam String sku) {
        log.info("GET product by sku {}", sku);

        ProductView response = this.findProductBySkuQuery.execute(sku)
                .orElseThrow(() -> new QueryException("Product with sku " + sku + " not found"));

        return ResponseEntity.ok(BaseResponseWrapper.of(response));
    }

    @GetMapping(path = "/active")
    public ResponseEntity<BaseResponseWrapper<List<ProductView>>> getActive() {
        log.info("GET active products");

        List<ProductView> response = this.findProductActiveQuery.execute();

        if (response.isEmpty()) {
            log.error("No active products found");
            return  ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(BaseResponseWrapper.of(response));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponseWrapper<List<ProductView>>> search(@RequestParam String text) {
        log.info("GET search: {}", text);

        List<ProductView> products = this.findProductByTextQuery.execute(text);

        return ResponseEntity.ok(BaseResponseWrapper.of(products));
    }

    @GetMapping(params = "category")
    public ResponseEntity<BaseResponseWrapper<List<ProductView>>> findByCategory(@RequestParam String category) {
        log.info("GET findByCategory: {}", category);

        List<ProductView> products = this.findProductByCategoryQuery.execute(category);

        return ResponseEntity.ok(BaseResponseWrapper.of(products));
    }

}
