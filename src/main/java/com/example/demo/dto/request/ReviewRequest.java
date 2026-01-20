package com.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "Product ID không được trống")
    private Long productId;

    @Min(value = 1, message = "Rating phải từ 1")
    @Max(value = 5, message = "Rating tối đa là 5")
    private Integer rating;

    private String comment;
}