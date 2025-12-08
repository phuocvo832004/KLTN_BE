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
    private long orderCode;
    private Long amount;
    private String description;
    private String returnUrl;
    private String cancelUrl;
}

