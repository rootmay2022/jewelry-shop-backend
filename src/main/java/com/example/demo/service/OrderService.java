package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.OrderRequest;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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

        // Bước 1: Chỉ KIỂM TRA số lượng, chưa trừ kho
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng trong kho");
            }
        }

        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Bước 2: Tạo đơn hàng với trạng thái PENDING
        Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .build();
        orderRepository.save(order);

        // Bước 3: Lưu chi tiết đơn hàng (OrderDetail)
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProduct().getPrice())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Bước 4: Xóa giỏ hàng sau khi đặt thành công
        cart.getItems().clear();
        cartRepository.save(cart);

        return buildOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        Order.OrderStatus oldStatus = order.getStatus();

        // CHỈ TRỪ KHO KHI CHUYỂN SANG DELIVERED
        if (newStatus == Order.OrderStatus.DELIVERED && oldStatus != Order.OrderStatus.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Kho không đủ hàng cho sản phẩm: " + product.getName());
                }
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }
        
        // HOÀN LẠI KHO NẾU ĐƠN ĐÃ GIAO NHƯNG LẠI BỊ HỦY (Tùy chọn)
        if (newStatus == Order.OrderStatus.CANCELLED && oldStatus == Order.OrderStatus.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        return buildOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền hủy đơn hàng này");
        }

        // Nếu trạng thái đã là giao hàng hoặc đã hủy thì không được hủy tiếp
        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái này");
        }

        // Vì logic mới là chỉ trừ kho khi DELIVERED, 
        // nên nếu hủy ở trạng thái PENDING/CONFIRMED thì KHÔNG cần cộng lại kho.
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // --- Các hàm hỗ trợ giữ nguyên ---

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getOrderStatsByStatus() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    order -> order.getStatus() != null ? order.getStatus().name() : "UNKNOWN",
                    Collectors.counting()
                ));
    }
    // Thêm hàm này vào OrderService.java
public OrderResponse getOrderById(Long orderId, Long userId) {
    // 1. Tìm đơn hàng theo ID
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
    
    // 2. Bảo mật: Kiểm tra xem đơn hàng này có phải của chính User đang yêu cầu không
    if (!order.getUser().getId().equals(userId)) {
        throw new RuntimeException("Bạn không có quyền truy cập vào đơn hàng của người khác");
    }
    
    // 3. Chuyển đổi Entity sang DTO để trả về
    return buildOrderResponse(order);
}

    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findUserOrdersOrderByDateDesc(userId).stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse buildOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .build();
    }
}