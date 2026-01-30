package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.AdminUserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final UserService userService;

    // --- QUẢN LÝ ĐƠN HÀNG ---

    /**
     * Lấy danh sách toàn bộ đơn hàng cho trang OrderManagement.jsx
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .data(orderService.getAllOrders())
                .build());
    }

    /**
     * Cập nhật trạng thái đơn hàng. 
     * Nếu status là 'DELIVERED', logic trừ kho trong OrderService sẽ được kích hoạt.
     */
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

    /**
     * Thống kê Dashboard: Doanh thu và Biểu đồ trạng thái
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        // Gửi toàn bộ đơn hàng để Frontend tự tính toán doanh thu linh hoạt
        stats.put("orders", orderService.getAllOrders()); 
        // Gửi dữ liệu đã nhóm theo trạng thái để vẽ biểu đồ tròn (Pie Chart)
        stats.put("ordersByStatus", orderService.getOrderStatsByStatus()); 
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(stats)
                .build());
    }

    // --- QUẢN LÝ NGƯỜI DÙNG ---

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .data(userService.getAllUsers())
                .build());
    }

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

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa người dùng thành công")
                .build());
    } 
}