package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.ProductRequest;
import com.example.demo.dto.response.ProductResponse;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return productMapper.toResponse(product);
    }
    
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    // ĐÃ FIX: Chuyển Double sang BigDecimal để khớp với Repository
    public List<ProductResponse> filterByPriceRange(Double minPrice, Double maxPrice) {
        BigDecimal min = (minPrice != null) ? BigDecimal.valueOf(minPrice) : BigDecimal.ZERO;
        BigDecimal max = (maxPrice != null) ? BigDecimal.valueOf(maxPrice) : BigDecimal.valueOf(Double.MAX_VALUE);
        
        return productRepository.findByPriceRange(min, max).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : BigDecimal.ZERO)
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .material(request.getMaterial())
                .weight(request.getWeight() != null ? BigDecimal.valueOf(request.getWeight()) : null)
                .build();

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : BigDecimal.ZERO);
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setMaterial(request.getMaterial());
        product.setWeight(request.getWeight() != null ? BigDecimal.valueOf(request.getWeight()) : null);
        
        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }
        productRepository.deleteById(id);
    }
}