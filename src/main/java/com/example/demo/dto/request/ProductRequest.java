package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotNull(message = "Category ID không được null")
    private Long categoryId;
@NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    
    private String description;
@NotNull(message = "Giá không được null")
    @DecimalMin(value = "0.0", message = "Giá phải >= 0")
    private Double price;
@Min(value = 0, message = "Số lượng phải >= 0")
    private Integer stockQuantity;
    
    private String imageUrl;
private String material;
    private Double weight;
}