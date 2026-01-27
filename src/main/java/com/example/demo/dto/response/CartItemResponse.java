package com.example.demo.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
private BigDecimal price;
    private Integer quantity;
    private Integer stockQuantity;
    private BigDecimal subtotal;
}