package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service; // ĐỔI THÀNH JAKARTA
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

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager; 

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

        entityManager.refresh(product); // Ép bốc số 40 từ DB

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Sản phẩm không đủ số lượng trong kho");
        }
        
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho");
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
        
        entityManager.refresh(item.getProduct());
        
        if (quantity > 0 && quantity > item.getProduct().getStockQuantity()) {
            throw new RuntimeException("Chỉ còn " + item.getProduct().getStockQuantity() + " sản phẩm");
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
        cartItemRepository.deleteAllByCartId(cart.getId());
        if (cart.getItems() != null) cart.getItems().clear();
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
    
    private CartResponse buildCartResponse(Cart cart) {
        if (cart.getItems() == null) {
            return CartResponse.builder().id(cart.getId()).items(new ArrayList<>()).totalAmount(BigDecimal.ZERO).build();
        }

        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    Product p = item.getProduct();
                    entityManager.refresh(p); 
                    return CartItemResponse.builder()
                            .id(item.getId())
                            .productId(p.getId())
                            .productName(p.getName())
                            .productImage(p.getImageUrl())
                            .price(p.getPrice())
                            .quantity(item.getQuantity())
                            .stockQuantity(p.getStockQuantity()) 
                            .subtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder().id(cart.getId()).items(items).totalAmount(total).build();
    }
}