package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Tự động tạo Getter, Setter, toString, equals, hashCode
@Builder // Hỗ trợ tạo object theo pattern Builder
@AllArgsConstructor // Tạo constructor có đầy đủ tham số
@NoArgsConstructor // CỰC KỲ QUAN TRỌNG: Tạo constructor không tham số để Jackson hoạt động
public class LoginRequest {

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;
}