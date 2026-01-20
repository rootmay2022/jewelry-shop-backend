package com.example.demo.controller;

// 1. THÊM CÁC IMPORT NÀY
import com.example.demo.dto.request.AdminUserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
// ---

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final UserService userService; // <-- 2. THÊM TRƯỜNG NÀY

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .data(orderService.getAllOrders())
                .build());
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(orderService.updateOrderStatus(orderId, status))
                .build());
    }

    // ========== 3. THÊM CÁC ENDPOINT QUẢN LÝ USER ==========

    /**
     * Lấy tất cả người dùng (Admin only)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .data(userService.getAllUsers())
                .build());
    }

    /**
     * Cập nhật thông tin người dùng (Admin only)
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Cập nhật người dùng thành công")
                .data(userService.updateUserByAdmin(id, request))
                .build());
    }

    /**
     * Xóa người dùng (Admin only)
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa người dùng thành công")
                .build());
    }
}