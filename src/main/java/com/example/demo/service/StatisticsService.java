package com.example.demo.service;

import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Tổng số user
        stats.put("totalUsers", userRepository.count());
        
        // Tổng số sản phẩm
        stats.put("totalProducts", productRepository.count());
        
        // Tổng số đơn hàng
        stats.put("totalOrders", orderRepository.count());
        
        // Tổng doanh thu
        List<com.example.demo.entity.Order> orders = orderRepository.findAll();
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() != com.example.demo.entity.Order.OrderStatus.CANCELLED)
                .map(com.example.demo.entity.Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        // Đơn hàng theo trạng thái
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (com.example.demo.entity.Order.OrderStatus status : 
             com.example.demo.entity.Order.OrderStatus.values()) {
            long count = orderRepository.findByStatus(status).size();
            ordersByStatus.put(status.name(), count);
        }
        stats.put("ordersByStatus", ordersByStatus);
        
        // Doanh thu 7 ngày gần nhất
        List<Map<String, Object>> revenueByDay = getRevenueByDays(7);
        stats.put("revenueByDay", revenueByDay);
        
        return stats;
    }
    
    private List<Map<String, Object>> getRevenueByDays(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<com.example.demo.entity.Order> orders = orderRepository.findAll();
        
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        for (com.example.demo.entity.Order order : orders) {
            if (order.getOrderDate().isAfter(startDate) && 
                order.getStatus() != com.example.demo.entity.Order.OrderStatus.CANCELLED) {
                String date = order.getOrderDate().toLocalDate().toString();
                revenueMap.merge(date, order.getTotalAmount(), BigDecimal::add);
            }
        }
        
        for (Map.Entry<String, BigDecimal> entry : revenueMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("revenue", entry.getValue());
            result.add(item);
        }
        
        return result;
    }
}