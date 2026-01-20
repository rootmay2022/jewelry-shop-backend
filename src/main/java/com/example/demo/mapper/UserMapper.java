package com.example.demo.mapper;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole().name()) // Chuyển Enum thành String
                .createdAt(user.getCreatedAt())
                .build();
    }
}