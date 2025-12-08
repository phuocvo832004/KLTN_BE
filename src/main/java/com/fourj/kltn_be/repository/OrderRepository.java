package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);
    List<Order> findByUser_UserId(Long userId);
    Page<Order> findByUser_UserId(Long userId, Pageable pageable);
    List<Order> findByUser_UserIdAndStatus(Long userId, String status);
    Optional<Order> findByPaymentCode(String paymentCode);
}

