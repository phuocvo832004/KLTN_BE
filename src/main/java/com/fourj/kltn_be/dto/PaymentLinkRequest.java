package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkRequest {
    private Long orderId;
    private String description;
    private BigDecimal amount;
    private List<PaymentItem> items;
    private String returnUrl;
    private String cancelUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentItem {
        private String name;
        private Integer quantity;
        private BigDecimal price;
    }
}

