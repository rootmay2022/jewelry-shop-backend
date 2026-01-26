package com.example.demo.service;

import com.example.demo.dto.request.AdminUserUpdateRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.request.ResetPasswordRequest; // THÊM MỚI
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository,
                       @Lazy PasswordEncoder passwordEncoder, 
                       JwtUtil jwtUtil,
                       @Lazy AuthenticationManager authenticationManager,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetailsService loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // --- CÁC HÀM CŨ CỦA NÍ GIỮ NGUYÊN ---
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new RuntimeException("Username đã tồn tại");
        if (userRepository.existsByEmail(request.getEmail())) throw new RuntimeException("Email đã tồn tại");
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .deviceId(request.getDevice_id())
                .role(User.Role.USER)
                .build();
        
        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token).id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).role(user.getRole().name())
                .fullName(user.getFullName()).phone(user.getPhone()).address(user.getAddress())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Thông tin người dùng không tồn tại"));

        if (request.getDevice_id() != null && !request.getDevice_id().isEmpty()) {
            user.setDeviceId(request.getDevice_id());
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token).id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).role(user.getRole().name())
                .fullName(user.getFullName()).phone(user.getPhone()).address(user.getAddress())
                .build();
    }

    // ============================================================
    // --- PHẦN THÊM MỚI: XỬ LÝ QUÊN MẬT KHẨU ---
    // ============================================================

    @Transactional
   public void forgotPassword(String email) {
    // 1. Thêm dòng này để check Log Railway
    System.out.println("===> DEBUG: Email nhan tu Controller la: [" + email + "]");

    if (email == null || email.isEmpty()) {
        throw new RuntimeException("Backend không nhận được email từ Frontend!");
    }

    // 2. Xử lý dấu cách thừa (Trường hợp ní copy-paste bị dư khoảng trắng)
    String cleanEmail = email.trim();

    // 3. Tìm user (Nên dùng findByEmailIgnoreCase nếu ní có thể sửa Repository)
    User user = userRepository.findByEmail(cleanEmail)
            .orElseThrow(() -> new RuntimeException("Email '" + cleanEmail + "' không tồn tại trên hệ thống"));

    // ... phần còn lại giữ nguyên
    String otp = String.format("%06d", new Random().nextInt(1000000));
    user.setOtp(otp);
    user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
    userRepository.save(user);

    System.out.println("-----------------------------------------");
    System.out.println("MÃ OTP KHÔI PHỤC CỦA NÍ LÀ: " + otp);
    System.out.println("-----------------------------------------");
}

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Tìm user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Thông tin không hợp lệ"));

        // 2. Kiểm tra OTP
        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không chính xác");
        }

        // 3. Kiểm tra hết hạn
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        // 4. Cập nhật mật khẩu và xóa OTP
        user.setPassword(passwordEncoder.encode(request.getN    ewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    // --- CÁC HÀM QUẢN LÝ USER CỦA NÍ ---
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUserByAdmin(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException("Không tìm thấy user");
        userRepository.deleteById(id);
    }
}