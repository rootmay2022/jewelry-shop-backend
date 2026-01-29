package com.example.demo.dto.response;
import lombok.*;

@Data
@Builder
public class AuthResponse {
    private String token;
private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String role;


    private String fullName; 
    private String phone;
    private String address;
}