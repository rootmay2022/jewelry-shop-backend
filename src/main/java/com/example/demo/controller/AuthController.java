package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping; // Ní nhớ tạo DTO này
import org.springframework.web.bind.annotation.RestController;  // Và DTO này nữa

import com.example.demo.dto.request.ForgotPasswordRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.request.ResetPasswordRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder().success(true).message("Đăng ký thành công").data(response).build());
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder().success(true).message("Đăng nhập thành công").data(response).build());
    }

    // --- THÊM PHẦN QUÊN MẬT KHẨU VÀO ĐÂY NÈ ---

    // AuthController.java
@PostMapping("/send-otp") // Đổi từ forgot-password thành send-otp để tránh trùng lặp nếu có
public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
    userService.forgotPassword(request.getEmail()); 
    return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("Mã OTP đã được gửi")
            .build());
}

@PostMapping("/reset-password")
public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
    userService.resetPassword(request);
    return ResponseEntity.ok("Password reset successfully");
}
}