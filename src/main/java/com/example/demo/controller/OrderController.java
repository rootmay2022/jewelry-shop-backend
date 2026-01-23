    package com.example.demo.controller;

    import java.util.List;

    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.PutMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import com.example.demo.dto.request.OrderRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

    @RestController
    @RequestMapping("/api/orders")
    @RequiredArgsConstructor
    public class OrderController {
        
        private final OrderService orderService;
    @PostMapping
        public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
                @Valid @RequestBody OrderRequest request,
                Authentication authentication) {
            Long userId = getUserIdFromAuth(authentication);
    return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                    .success(true)
                    .message("Đặt hàng thành công")
                    .data(orderService.createOrder(userId, request))
                    .build());
    }
        
        @GetMapping
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(Authentication authentication) {
            Long userId = getUserIdFromAuth(authentication);
    return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                    .success(true)
                    .data(orderService.getUserOrders(userId))
                    .build());
    }
        
        @GetMapping("/{orderId}")
        public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
                @PathVariable Long orderId,
                Authentication authentication) {
            Long userId = getUserIdFromAuth(authentication);
    return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                    .success(true)
                    .data(orderService.getOrderById(orderId, userId))
                    .build());
    }
        
        @PutMapping("/{orderId}/cancel")
        public ResponseEntity<ApiResponse<Void>> cancelOrder(
                @PathVariable Long orderId,
                Authentication authentication) {
            Long userId = getUserIdFromAuth(authentication);
    orderService.cancelOrder(orderId, userId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Hủy đơn hàng thành công")
                    .build());
    }
        
        private Long getUserIdFromAuth(Authentication authentication) {
            com.example.demo.entity.User user = (com.example.demo.entity.User) authentication.getPrincipal();
    return user.getId();
        }
    }