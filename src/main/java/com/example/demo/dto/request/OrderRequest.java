package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;
@NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;
}