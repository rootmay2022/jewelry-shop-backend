package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;

    // 1. Phải có Constructor không tham số (Bắt buộc để Jackson hoạt động)
    public LoginRequest() {
    }

    // 2. Constructor có tham số
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // 3. Viết Getter và Setter thủ công (Quan trọng nhất)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}