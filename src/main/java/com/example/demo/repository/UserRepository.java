package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);

    // Tìm kiếm không phân biệt hoa thường
    Optional<User> findByEmailIgnoreCase(String email);

    // --- FIX LỖI DẤU CÁCH ẨN (LENGTH 29) ---
    // Câu lệnh này sẽ TRIM cả dữ liệu trong DB và dữ liệu người dùng nhập
    @Query("SELECT u FROM User u WHERE TRIM(LOWER(u.email)) = TRIM(LOWER(:email))")
    Optional<User> findByEmailSmart(@Param("email") String email);

    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}