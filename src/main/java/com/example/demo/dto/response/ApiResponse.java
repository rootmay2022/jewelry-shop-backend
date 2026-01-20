package com.example.demo.dto.response;

import lombok.*;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
private T data;
}