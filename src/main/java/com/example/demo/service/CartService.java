package com.example.demo.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Sản phẩm không đủ số lượng trong kho");
}
        
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);
if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
} else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
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
        
        if (quantity <= 0) {
            cartItemRepository.delete(item);
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
cart.getItems().clear();
        cartRepository.save(cart);
    }
    
    private Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
Cart cart = Cart.builder()
                .user(user)
                .build();
return cartRepository.save(cart);
    }
    
    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
         
               .productName(item.getProduct().getName())
                        .productImage(item.getProduct().getImageUrl())
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
             
           .subtotal(item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
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