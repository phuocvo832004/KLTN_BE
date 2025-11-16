package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findById(Long id);
    List<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.userId = :userId AND c.status = :status")
    Optional<Cart> findByUserIdAndStatusWithItems(@Param("userId") Long userId, @Param("status") String status);
}

