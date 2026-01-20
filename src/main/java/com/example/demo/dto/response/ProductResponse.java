package com.example.demo.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String material;
    private BigDecimal weight;
private LocalDateTime createdAt;
}