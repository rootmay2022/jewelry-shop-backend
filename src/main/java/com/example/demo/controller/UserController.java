package com.example.demo.controller;

import com.example.demo.dto.request.AdminUserUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // Khớp với api/users/18 của Frontend
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        
        // Sử dụng lại logic update đã có trong userService
        UserResponse response = userService.updateUserByAdmin(id, request);
        
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Cập nhật thông tin cá nhân thành công")
                .data(response)
                .build());
    }
}