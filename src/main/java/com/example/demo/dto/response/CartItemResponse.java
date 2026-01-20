package com.example.demo.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}