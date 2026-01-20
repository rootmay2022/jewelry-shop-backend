package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    
    @NotNull(message = "Product ID không được null")
    private Long productId;
@Min(value = 1, message = "Số lượng phải >= 1")
    private Integer quantity;
}