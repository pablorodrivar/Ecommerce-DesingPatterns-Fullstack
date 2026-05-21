package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.entity.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private ProductStatus status;
    private LocalDateTime createdAt;
}