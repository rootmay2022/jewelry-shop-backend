package com.example.demo.entity;

// 1. THÊM IMPORT NÀY
import com.fasterxml.jackson.annotation.JsonIgnore; 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 2. THÊM ANNOTATION NÀY VÀO TRÊN "products"
    @JsonIgnore 
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}