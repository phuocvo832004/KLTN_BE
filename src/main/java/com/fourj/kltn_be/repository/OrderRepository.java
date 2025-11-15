package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdAndStatus(Long userId, String status);
}

