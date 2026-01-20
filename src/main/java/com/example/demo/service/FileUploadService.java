package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    public String uploadFile(MultipartFile file) throws IOException {
        // Tạo thư mục nếu chưa tồn tại
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filepath = Paths.get(uploadDir, filename);
        Files.write(filepath, file.getBytes());
        
        // Return URL
        return "/uploads/" + filename;
    }
    
    public void deleteFile(String filename) throws IOException {
        Path filepath = Paths.get(uploadDir, filename);
        Files.deleteIfExists(filepath);
    }
}