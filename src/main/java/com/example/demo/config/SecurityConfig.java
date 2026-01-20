package com.example.demo.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { 
        http
            // 1. Cấu hình CORS trước tiên
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 2. Tắt CSRF để cho phép POST từ domain khác
            .csrf(csrf -> csrf.disable())
            // 3. Phân quyền truy cập
            .authorizeHttpRequests(auth -> auth
                // === CHO PHÉP MỌI REQUEST OPTIONS (Pre-flight) ===
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // === KHU VỰC PUBLIC (Không cần đăng nhập) ===
                // Cho phép Auth (cả có /api và không có)
                .requestMatchers("/api/auth/**", "/auth/**").permitAll()
                
                // Cho phép xem Sản phẩm (cả có /api và không có)
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/products/**").permitAll()
                
                // Cho phép xem Danh mục (cả có /api và không có)
                .requestMatchers(HttpMethod.GET, "/api/categories/**", "/categories/**").permitAll()
                
                // Cho phép xem Đánh giá
                .requestMatchers(HttpMethod.GET, "/api/reviews/**", "/reviews/**").permitAll()
                
                // Cho phép truy cập ảnh/file tĩnh
                .requestMatchers("/uploads/**", "/images/**").permitAll()
                
                // === KHU VỰC ADMIN ===
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                
                // === CÒN LẠI PHẢI LOGIN ===
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Danh sách các domain được phép gọi API
        configuration.setAllowedOrigins(Arrays.asList(
            "https://jewelry-shop-frontend.vercel.app", // Link Production
            "http://localhost:5173",                    // Link Dev React
            "http://127.0.0.1:5173",
            "http://localhost:3000"
        ));
        
        // Cho phép tất cả các method (GET, POST, PUT, DELETE...)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        
        // Cho phép tất cả các header (để tránh lỗi thiếu header Authorization/Content-Type)
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        
        // Cho phép gửi credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cho phép Frontend đọc được header trả về
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}