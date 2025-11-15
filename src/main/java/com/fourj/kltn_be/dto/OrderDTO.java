package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<OrderItemDTO> items;
}

