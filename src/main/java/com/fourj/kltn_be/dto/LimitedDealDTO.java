package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitedDealDTO {
    private String productId;
    private String title;
    private String description;
    private BigDecimal originalPrice;
    private String imurl;
    private String imgUrl;
    private Double averageRating;
    private String[] categories;
    
    private Long saleId;
    private BigDecimal salePrice;
    private BigDecimal discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long branchId;
    private String branchName;
    
    private BigDecimal savedAmount;
    private Long hoursRemaining;
}

