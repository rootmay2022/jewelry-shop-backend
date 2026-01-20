package com.example.demo.controller;

import com.example.demo.dto.request.CartItemRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CartResponse;
import com.example.demo.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                .build());
}
    
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã xóa giỏ hàng")
                .build());
}
    
    private Long getUserIdFromAuth(Authentication authentication) {
        com.example.demo.entity.User user = (com.example.demo.entity.User) authentication.getPrincipal();
return user.getId();
    }
}