package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
@Column(nullable = false, length = 200)
    private String name;
@Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
@Column(name = "stock_quantity")
    private Integer stockQuantity = 0;
    
    @Column(name = "image_url")
    private String imageUrl;
@Column(length = 50)
    private String material;
    
    @Column(precision = 6, scale = 2)
    private BigDecimal weight;
@Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
@Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
}
}