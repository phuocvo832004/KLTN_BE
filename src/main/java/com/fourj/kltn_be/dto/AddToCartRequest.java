package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private String productId;
    private Integer quantity; // Optional, mặc định 1
    private BigDecimal unitPrice; // Optional, sẽ lấy từ product (ưu tiên sale, không thì price)
}

