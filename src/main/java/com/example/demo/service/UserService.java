package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

import com.example.demo.dto.request.AdminUserUpdateRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.request.ResetPasswordRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    // FIX 1: Xóa @Autowired thừa (Constructor duy nhất không cần annotation này)
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

    // FIX 2: Sửa kiểu trả về từ UserDetailsService thành UserDetails
    // Đây là lỗi khiến Railway của ní bị "đỏ lòm" khi build (incompatible types)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

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

    @Transactional
    public void forgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("LỖI: Backend nhận email bị trống!");
        }
        
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        String otp = String.format("%06d", new Random().nextInt(1000000));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // In ra log để ní test cho dễ
        System.out.println("-----------------------------------------");
        System.out.println("MÃ OTP CỦA NÍ ĐÂY: " + otp);
        System.out.println("-----------------------------------------");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new RuntimeException("Thông tin không hợp lệ"));

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không chính xác");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUserByAdmin(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
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