package com.fourj.kltn_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "branch_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "sale_price", nullable = false, precision = 38, scale = 2)
    private BigDecimal salePrice; // Giá sale tại chi nhánh này

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage; // Phần trăm giảm giá (0-100)

    @Column(name = "start_date")
    private LocalDateTime startDate; // Ngày bắt đầu sale

    @Column(name = "end_date")
    private LocalDateTime endDate; // Ngày kết thúc sale

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Sale có đang hoạt động không

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

