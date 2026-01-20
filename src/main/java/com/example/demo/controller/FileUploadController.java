package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String fileUrl = fileUploadService.uploadFile(file);
        
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Upload thành công")
                .data(fileUrl)
                .build());
    }
}