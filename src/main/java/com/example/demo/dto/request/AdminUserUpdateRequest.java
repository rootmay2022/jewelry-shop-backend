package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    // Admin có thể không cần cập nhật email/username

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String phone;
    private String address;

    @NotBlank(message = "Vai trò không được để trống")
    private String role; // Sẽ là "USER" hoặc "ADMIN"
}