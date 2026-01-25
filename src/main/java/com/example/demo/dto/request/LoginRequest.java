package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty; // THÊM IMPORT NÀY

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;

    @JsonProperty("device_id") // ÉP JACKSON ĐỌC ĐÚNG TÊN NÀY
    private String device_id; 
}