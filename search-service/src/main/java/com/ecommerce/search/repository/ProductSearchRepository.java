package com.ecommerce.search.repository;

import com.ecommerce.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByNameContainingIgnoreCase(String name);

    List<ProductDocument> findByCategory(String category);

    List<ProductDocument> findByPriceBetween(Double minPrice, Double maxPrice);

    List<ProductDocument> findByCategoryAndPriceBetween(
            String category, Double minPrice, Double maxPrice);
}