package com.fourj.kltn_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stockings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "branch_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stocking {
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

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0; // Số lượng tồn kho tại chi nhánh

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0; // Số lượng đã được đặt trước (chưa xuất kho)

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0; // Số lượng có sẵn = quantity - reservedQuantity

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 0; // Mức tồn kho tối thiểu để cảnh báo

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt; // Lần cuối nhập hàng

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Method để tính available quantity
    @PreUpdate
    @PrePersist
    private void calculateAvailableQuantity() {
        this.availableQuantity = Math.max(0, this.quantity - this.reservedQuantity);
    }

    // Method để kiểm tra còn hàng không
    public Boolean isInStock() {
        return this.availableQuantity > 0;
    }

    // Method để kiểm tra sắp hết hàng
    public Boolean isLowStock() {
        return this.availableQuantity <= this.minStockLevel;
    }
}

