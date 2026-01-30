package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.CartItemRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CartResponse;
import com.example.demo.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .data(cartService.getCartByUserId(userId))
                .build());
    }
    
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Thêm vào giỏ hàng thành công")
                .data(cartService.addItemToCart(userId, request))
                .build());
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cập nhật giỏ hàng thành công")
                .data(cartService.updateCartItem(userId, itemId, quantity))
                .build());
    }
    
    // --- ĐÃ SỬA: Trả về CartResponse ---
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        // Nhận CartResponse mới nhất từ Service
        CartResponse updatedCart = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Xóa sản phẩm thành công")
                .data(updatedCart) // Trả về data mới
                .build());
    }
    
    // --- ĐÃ SỬA: Trả về CartResponse ---
    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        CartResponse emptyCart = cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Đã xóa giỏ hàng")
                .data(emptyCart)
                .build());
    }
    
    private Long getUserIdFromAuth(Authentication authentication) {
        com.example.demo.entity.User user = (com.example.demo.entity.User) authentication.getPrincipal();
        return user.getId();
    }
}