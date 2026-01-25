package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String role; // Sẽ là "USER" hoặc "ADMIN"
    private LocalDateTime createdAt;
    private String deviceId;
}