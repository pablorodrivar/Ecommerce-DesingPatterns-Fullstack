package com.ecommerce.search.service;

import com.ecommerce.search.document.ProductDocument;
import com.ecommerce.search.dto.ProductIndexRequest;
import com.ecommerce.search.dto.SearchRequest;
import com.ecommerce.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductDocument indexProduct(ProductIndexRequest request) {
        ProductDocument doc = ProductDocument.builder()
                .id(request.getProductId().toString())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .stock(request.getStock())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        ProductDocument saved = searchRepository.save(doc);
        log.info("Product indexed in Elasticsearch: {}", saved.getId());
        return saved;
    }

    public List<ProductDocument> search(SearchRequest request) {
        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            return searchRepository.findByNameContainingIgnoreCase(request.getQuery());
        }

        if (request.getCategory() != null && request.getMinPrice() != null) {
            return searchRepository.findByCategoryAndPriceBetween(
                    request.getCategory(),
                    request.getMinPrice(),
                    request.getMaxPrice() != null ? request.getMaxPrice() : Double.MAX_VALUE
            );
        }

        if (request.getCategory() != null) {
            return searchRepository.findByCategory(request.getCategory());
        }

        if (request.getMinPrice() != null) {
            return searchRepository.findByPriceBetween(
                    request.getMinPrice(),
                    request.getMaxPrice() != null ? request.getMaxPrice() : Double.MAX_VALUE
            );
        }

        return (List<ProductDocument>) searchRepository.findAll();
    }

    public List<ProductDocument> advancedSearch(String query) {
        Criteria criteria = new Criteria("name").contains(query)
                .or(new Criteria("description").contains(query))
                .or(new Criteria("category").is(query));

        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(criteriaQuery, ProductDocument.class);

        return hits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }

    public void deleteProduct(String productId) {
        searchRepository.deleteById(productId);
        log.info("Product removed from index: {}", productId);
    }
}