package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {
    private Integer code;
    private String desc;
    private PaymentLinkData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentLinkData {
        private String bin;
        private String accountNumber;
        private String accountName;
        private BigDecimal amount;
        private String description;
        private Long orderCode;
        private String currency;
        private String paymentLinkId;
        private String status;
        private Long expiredAt;
        private String qrCode;
        private String checkoutUrl;
    }
}

