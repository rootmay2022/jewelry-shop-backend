package com.example.demo.mapper;

import com.example.demo.dto.response.ProductResponse;
import com.example.demo.entity.Product;
import org.springframework.stereotype.Component;
@Component
public class ProductMapper {
    
    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .name(product.getName())
            
    .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .material(product.getMaterial())
                .weight(product.getWeight())
                
.createdAt(product.getCreatedAt())
                .build();
}
}