package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.CartItemRequest;
import com.example.demo.dto.response.CartItemResponse;
import com.example.demo.dto.response.CartResponse;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
        return buildCartResponse(cart);
    }
    
    @Transactional
    public CartResponse addItemToCart(Long userId, CartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        // 1. Chặn ngay từ đầu nếu mua quá kho
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Sản phẩm không đủ số lượng trong kho");
        }
        
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            // 2. Chặn nếu cộng dồn vào giỏ hàng vượt quá 40 món (ví dụ kho có 40)
            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Tổng số lượng trong giỏ hàng vượt quá tồn kho hiện có");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
            
            // Đảm bảo list trong object Cart được cập nhật để buildResponse không sót
            if (cart.getItems() == null) cart.setItems(new ArrayList<>());
            cart.getItems().add(newItem);
        }
        
        return buildCartResponse(cart);
    }
    
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy item"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item không thuộc giỏ hàng này");
        }
        
        // 3. Chặn khi khách nhấn nút (+) trên giao diện React
        if (quantity > 0 && quantity > item.getProduct().getStockQuantity()) {
            throw new RuntimeException("Chỉ còn " + item.getProduct().getStockQuantity() + " sản phẩm trong kho");
        }
        
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        
        return buildCartResponse(cart);
    }

    @Transactional
    public void removeItemFromCart(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy item"));
        
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item không thuộc giỏ hàng này");
        }
        
        cartItemRepository.delete(item);
    }

    
   @Transactional
public void clearCart(Long userId) {
    Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));
    
    // Xóa sạch ở DB
    cartItemRepository.deleteAllByCartId(cart.getId());
    
    // Xóa sạch ở RAM (để React nhận về list rỗng ngay)
    if (cart.getItems() != null) {
        cart.getItems().clear();
    }
}

    private Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        Cart cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
        return cartRepository.save(cart);
    }
    
    // HÀM QUAN TRỌNG NHẤT: Trả số 40 (stockQuantity) về cho React
    private CartResponse buildCartResponse(Cart cart) {
        if (cart.getItems() == null) {
            return CartResponse.builder()
                    .id(cart.getId())
                    .items(new ArrayList<>())
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    Product p = item.getProduct();
                    return CartItemResponse.builder()
                            .id(item.getId())
                            .productId(p.getId())
                            .productName(p.getName())
                            .productImage(p.getImageUrl())
                            .price(p.getPrice())
                            .quantity(item.getQuantity())
                            // ĐẨY SỐ TỒN KHO VỀ ĐÂY NÈ NÍ
                            .stockQuantity(p.getStockQuantity()) 
                            .subtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalAmount(total)
                .build();
    }
}