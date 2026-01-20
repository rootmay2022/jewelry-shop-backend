package com.example.demo.service;

import com.example.demo.dto.request.OrderRequest;
import com.example.demo.dto.response.OrderItemResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
@Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng trống"));
if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
}
        
        // Kiểm tra tồn kho
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng");
}
        }
        
        // Tạo order
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .build();
orderRepository.save(order);
        
        // Tạo order items và trừ tồn kho
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                 
   .build();
            
            order.getItems().add(orderItem);
            orderItemRepository.save(orderItem);
            
            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
productRepository.save(product);
        }
        
        // Xóa giỏ hàng
        cart.getItems().clear();
cartRepository.save(cart);
        
        return buildOrderResponse(order);
    }
    
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findUserOrdersOrderByDateDesc(userId).stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
}
    
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền truy cập đơn hàng này");
}
        
        return buildOrderResponse(order);
}
    
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        orderRepository.save(order);
        
        return buildOrderResponse(order);
    }
    
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền hủy đơn hàng này");
}
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Không thể hủy đơn hàng đã xác nhận");
}
        
        // Hoàn lại tồn kho
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
orderRepository.save(order);
    }
    
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
}
    
    private OrderResponse buildOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
           
             .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
               
 .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
 
               .paymentMethod(order.getPaymentMethod())
                .items(items)
                .build();
}
}