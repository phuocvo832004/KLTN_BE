package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomepageDataDTO {
    
    private List<ProductDTO> specialOffers;
    
    private List<ProductDTO> newArrivals;
    
    private List<ProductDTO> popularProducts;
    
    private List<LimitedDealDTO> limitedDeals;
}

