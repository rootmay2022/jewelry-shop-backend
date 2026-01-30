package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("JEWELRY SHOP <support@jewelry.com>"); // Tên người gửi ảo
            message.setTo(toEmail);
            message.setSubject("MÃ XÁC NHẬN ĐẶT LẠI MẬT KHẨU");
            message.setText("Xin chào,\n\n"
                    + "Bạn đã yêu cầu đặt lại mật khẩu. Đây là mã xác thực (OTP) của bạn:\n\n"
                    + "👉 " + otp + " 👈\n\n"
                    + "Mã này sẽ hết hạn sau 5 phút.\n"
                    + "Nếu bạn không yêu cầu, vui lòng bỏ qua email này.");

            mailSender.send(message);
            System.out.println("Đã gửi email thành công đến: " + toEmail);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể gửi email, vui lòng kiểm tra cấu hình mạng hoặc Gmail.");
        }
    }
}