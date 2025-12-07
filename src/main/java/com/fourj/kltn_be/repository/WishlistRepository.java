package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Wishlist;
import com.fourj.kltn_be.entity.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
    
    List<Wishlist> findByUserId(Long userId);
    
    boolean existsByUserIdAndProductId(Long userId, String productId);
    
    void deleteByUserIdAndProductId(Long userId, String productId);
    
    @Query("SELECT w.productId FROM Wishlist w WHERE w.userId = :userId")
    List<String> findProductIdsByUserId(@Param("userId") Long userId);
    
    long countByUserId(Long userId);
}

