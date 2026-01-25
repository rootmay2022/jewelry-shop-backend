package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank; // THÊM IMPORT NÀY
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


    @com.fasterxml.jackson.annotation.JsonProperty("device_id")
    private String device_id; 
}