package com.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Bao gồm cả Getter, Setter, toString, equals và hashCode
@NoArgsConstructor // Cực kỳ quan trọng để Spring Boot có thể tạo object từ JSON
@AllArgsConstructor // Khởi tạo có đủ tham số
public class ForgotPasswordRequest {
    private String email;
}