package com.example.demo.dto.request;

import jakarta.validation.constraints.Email; // THÊM IMPORT NÀY
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
        
    private String phone;
    private String address;

   @com.fasterxml.jackson.annotation.JsonProperty("device_id")
    private String device_id;
    public String getDevice_id() {
    return device_id;
}

public void setDevice_id(String device_id) {
    this.device_id = device_id;
}
}