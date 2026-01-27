package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // THÊM ĐOẠN NÀY ĐỂ FIX LỖI XÓA
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId")
    void deleteAllByCartId(Long cartId);
}