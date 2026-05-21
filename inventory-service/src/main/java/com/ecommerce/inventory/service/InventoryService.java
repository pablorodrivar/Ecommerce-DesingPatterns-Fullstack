package com.ecommerce.inventory.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.entity.ProductStatus;
import com.ecommerce.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {} (id={})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse updateStock(Long id, StockUpdateRequest request) {
        Product product = productRepository.findByIdWithLock(id)
                .orElseThrow(() -> BusinessException.notFound("Product", id));

        product.setStock(request.getQuantity());
        updateStatusByStock(product);

        log.info("Stock updated for product {}: new stock={}", id, request.getQuantity());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> BusinessException.notFound("Product", productId));

        if (!product.hasStock(quantity)) {
            throw BusinessException.conflict(
                "Insufficient stock for product: " + product.getName()
                + ". Available: " + product.getStock() + ", requested: " + quantity);
        }

        product.decreaseStock(quantity);
        updateStatusByStock(product);
        productRepository.save(product);

        log.info("Stock decreased for product {}: -{}", productId, quantity);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(this::toResponse)
                .toList();
    }

    private void updateStatusByStock(Product product) {
        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Product", id));
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse response = new ProductResponse();
        response.setId(p.getId());
        response.setName(p.getName());
        response.setDescription(p.getDescription());
        response.setPrice(p.getPrice());
        response.setStock(p.getStock());
        response.setCategory(p.getCategory());
        response.setStatus(p.getStatus());
        response.setCreatedAt(p.getCreatedAt());
        return response;
    }
}