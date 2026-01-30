package com.example.demo.controller;

import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Sửa lỗi 404: Đón PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) { // Dùng DTO mới
        
        // Bạn cần viết thêm hàm này trong UserService hoặc tận dụng hàm cũ
        UserResponse response = userService.updateUserByAdmin(id, castToAdminRequest(request));
        
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Cập nhật thông tin thành công")
                .data(response)
                .build());
    }

    // Hàm phụ để map DTO (nếu bạn không muốn sửa UserService)
    private com.example.demo.dto.request.AdminUserUpdateRequest castToAdminRequest(UserUpdateRequest req) {
        com.example.demo.dto.request.AdminUserUpdateRequest adminReq = new com.example.demo.dto.request.AdminUserUpdateRequest();
        adminReq.setFullName(req.getFullName());
        adminReq.setPhone(req.getPhone());
        adminReq.setAddress(req.getAddress());
        return adminReq;
    }
}