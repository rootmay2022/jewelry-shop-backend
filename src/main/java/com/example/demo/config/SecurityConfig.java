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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // Tắt CSRF là bắt buộc cho REST API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không dùng Session
            .authorizeHttpRequests(auth -> auth
                // 1. Luôn cho phép các request kiểm tra (Pre-flight) từ trình duyệt
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 2. Mở cửa các API Public (Cho cả trường hợp Frontend gọi thiếu /api)
                .requestMatchers("/auth/**", "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/products/**", "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/categories/**", "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/reviews/**", "/api/reviews/**").permitAll()
                .requestMatchers("/uploads/**", "/images/**", "/static/**").permitAll()
                
                // 3. Các API yêu cầu quyền Admin
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                
                // 4. Tất cả các request còn lại phải được xác thực (có Token mới cho vào)
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cấu hình các nguồn được phép (Add thêm link Vercel của bạn)
        configuration.setAllowedOrigins(Arrays.asList(
            "https://jewelry-shop-frontend.vercel.app",
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:3000"
        ));
        
        // Cho phép tất cả các phương thức
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Quan trọng: Cho phép các Headers cần thiết để Token JWT hoạt động
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        
        // Cho phép gửi credentials (token, cookies)
        configuration.setAllowCredentials(true);
        
        // Tiết lộ Header Authorization để Frontend có thể lấy Token sau khi Login
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        
        // Giữ cấu hình CORS trong 1 giờ để giảm bớt các request OPTIONS thừa
        configuration.setMaxAge(3600L);

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