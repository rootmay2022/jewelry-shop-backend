package com.example.demo.service;

import com.example.demo.dto.request.AdminUserUpdateRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
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
import java.util.List;
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
        // Chỉnh lại kiểu trả về cho đúng chuẩn interface
        return (UserDetails) userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        
        // --- CHÈN THÊM deviceId VÀO BUILDER ---
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .deviceId(request.getDevice_id()) // Thêm dòng này để lưu mã thiết bị khi đăng ký
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

    // HÀM LOGIN ĐÃ ĐƯỢC "ĐỘ" LẠI ĐỂ FIX LỖI VÀ CẬP NHẬT DEVICE ID
    @Transactional // Thêm Transactional để đảm bảo việc save(user) mượt mà
    public AuthResponse login(LoginRequest request) {
        try {
            // 1. Xác thực qua AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Bắt lỗi sai mật khẩu cụ thể để tránh lỗi 502/CORS
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác");
        } catch (Exception e) {
            // Các lỗi hệ thống khác
            throw new RuntimeException("Lỗi xác thực hệ thống: " + e.getMessage());
        }

        // 2. Lấy thông tin User sau khi đã authenticate thành công
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Thông tin người dùng không tồn tại"));

        // --- CẬP NHẬT DEVICE ID MỚI NHẤT MỖI KHI ĐĂNG NHẬP ---
        if (request.getDevice_id() != null && !request.getDevice_id().isEmpty()) {
            user.setDeviceId(request.getDevice_id());
            userRepository.save(user); // Lưu lại thiết bị cuối cùng sử dụng
        }
        // ---------------------------------------------------

        // 3. Tạo Token JWT
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

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