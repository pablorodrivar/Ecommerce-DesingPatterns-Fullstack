package com.ecommerce.search.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.search.document.ProductDocument;
import com.ecommerce.search.dto.ProductIndexRequest;
import com.ecommerce.search.dto.SearchRequest;
import com.ecommerce.search.service.ProductSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductSearchService searchService;

    @PostMapping("/index")
    public ResponseEntity<ApiResponse<ProductDocument>> indexProduct(
            @Valid @RequestBody ProductIndexRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product indexed",
                        searchService.indexProduct(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDocument>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setCategory(category);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);

        return ResponseEntity.ok(ApiResponse.ok(searchService.search(request)));
    }

    @GetMapping("/advanced")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> advancedSearch(
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.advancedSearch(q)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteFromIndex(
            @PathVariable String productId) {
        searchService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.ok("Product removed from index", null));
    }
}