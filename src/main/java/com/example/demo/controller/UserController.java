package com.example.demo.controller;

import com.example.demo.dto.request.ChangePasswordRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.request.AdminUserUpdateRequest;
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
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    // API Cập nhật hồ sơ (fullName, phone, address)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        AdminUserUpdateRequest adminReq = new AdminUserUpdateRequest();
        adminReq.setFullName(request.getFullName());
        adminReq.setPhone(request.getPhone());
        adminReq.setAddress(request.getAddress());
        adminReq.setRole(null); // Tránh lỗi NullPointerException toUpperCase()

        UserResponse response = userService.updateUserByAdmin(id, adminReq);
        
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Cập nhật thông tin thành công")
                .data(response)
                .build());
    }

    // API Đổi mật khẩu
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        userService.changePassword(request);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đổi mật khẩu thành công")
                .build());
    }
}