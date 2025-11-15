package com.fourj.kltn_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_specs")
@IdClass(ProductSpecId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpec {

    @Id
    @Column(name = "product_id", nullable = false, length = 255)
    private String productId;

    @Id
    @Column(name = "spec_key", nullable = false, length = 255)
    private String specKey;

    @Column(name = "spec_value", length = 255)
    private String specValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}
