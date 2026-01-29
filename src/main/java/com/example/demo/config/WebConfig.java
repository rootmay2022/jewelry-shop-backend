package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // --- QUAN TRỌNG: ĐÃ TẮT CORS Ở ĐÂY ĐỂ DÙNG BÊN SECURITY CONFIG ---
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**") 
    //             .allowedOrigins(
    //                 "http://localhost:5173", 
    //                 "https://jewelry-shop-frontend.vercel.app"
    //             ) 
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .allowCredentials(true)
    //             .maxAge(3600);
    // }

    // Vẫn giữ cái này để hiển thị ảnh upload nhé
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}