package com.ecommerce.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductIndexRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String category;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer stock;
}