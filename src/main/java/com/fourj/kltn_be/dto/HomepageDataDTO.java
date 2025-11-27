package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for homepage data - combines multiple product lists into one response
 * to reduce API calls from frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomepageDataDTO {
    
    // Special Offers (season type = 0)
    private List<ProductDTO> specialOffers;
    
    // New Arrivals (season type = 1)
    private List<ProductDTO> newArrivals;
    
    // Popular products sorted by average rating
    private List<ProductDTO> popularProducts;
    
    // Limited deals with active sales
    private List<LimitedDealDTO> limitedDeals;
}

