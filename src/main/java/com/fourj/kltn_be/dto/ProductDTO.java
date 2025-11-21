package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imurl;
    private String[] categories;
    private String specs;
    private Double averageRating;
    private Double rating; // Alias for averageRating for API compatibility
    private String[] relatedProducts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imgUrl;
    private List<String> categoryList;
    private List<ProductSpecDTO> specList;
    private List<ReviewDTO> reviews;
}

