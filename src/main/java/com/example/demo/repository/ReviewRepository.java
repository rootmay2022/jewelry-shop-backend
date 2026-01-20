package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Phương thức này được dùng trong ReviewService [cite: 380]
    List<Review> findByProductId(Long productId);
}