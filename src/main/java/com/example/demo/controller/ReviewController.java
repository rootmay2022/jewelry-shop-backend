package com.example.demo.controller;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // API để user gửi đánh giá (cần đăng nhập)
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        // Lấy userId từ người dùng đã đăng nhập (giống CartController [cite: 188-189])
        com.example.demo.entity.User user = (com.example.demo.entity.User) authentication.getPrincipal();
        Long userId = user.getId();

        ReviewResponse review = reviewService.createReview(userId, request);

        return ResponseEntity.ok(ApiResponse.<ReviewResponse>builder()
                .success(true)
                .message("Gửi đánh giá thành công")
                .data(review)
                .build());
    }

    // API để xem đánh giá của 1 sản phẩm (public)
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId) {

        List<ReviewResponse> reviews = reviewService.getProductReviews(productId);

        return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                .success(true)
                .data(reviews)
                .build());
    }
}