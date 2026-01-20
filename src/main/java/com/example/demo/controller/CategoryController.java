package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
@GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.<List<Category>>builder()
                .success(true)
                .data(categoryRepository.findAll())
                .build());
}
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
return ResponseEntity.ok(ApiResponse.<Category>builder()
                .success(true)
                .data(category)
                .build());
}
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
return ResponseEntity.ok(ApiResponse.<Category>builder()
                .success(true)
                .message("Tạo danh mục thành công")
                .data(saved)
                .build());
}
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setImageUrl(category.getImageUrl());
        
        Category updated = categoryRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.<Category>builder()
                .success(true)
                .message("Cập nhật danh mục thành công")
                .data(updated)
                .build());
}
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa danh mục thành công")
                .build());
}
}