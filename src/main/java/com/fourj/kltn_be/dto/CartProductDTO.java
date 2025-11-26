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
public class CartProductDTO {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imurl;
    private String[] categories;
    private String specs;
    private Double averageRating;
    private Double rating;
    private String[] relatedProducts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imgUrl;
    private BigDecimal sale;
    private List<String> categoryList;
    private List<ProductSpecDTO> specList;
    private List<ReviewDTO> reviews;
    
    // Cart-specific fields
    private Long cartItemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String productId;
    
    public static CartProductDTO fromProductDTO(ProductDTO productDTO, Integer quantity, BigDecimal unitPrice, Long cartItemId) {
        CartProductDTO dto = new CartProductDTO();
        dto.setId(productDTO.getId());
        dto.setTitle(productDTO.getTitle());
        dto.setDescription(productDTO.getDescription());
        dto.setPrice(productDTO.getPrice());
        dto.setImurl(productDTO.getImurl());
        dto.setCategories(productDTO.getCategories());
        dto.setSpecs(productDTO.getSpecs());
        dto.setAverageRating(productDTO.getAverageRating());
        dto.setRating(productDTO.getRating());
        dto.setRelatedProducts(productDTO.getRelatedProducts());
        dto.setCreatedAt(productDTO.getCreatedAt());
        dto.setUpdatedAt(productDTO.getUpdatedAt());
        dto.setImgUrl(productDTO.getImgUrl());
        dto.setSale(productDTO.getSale());
        dto.setCategoryList(productDTO.getCategoryList());
        dto.setSpecList(productDTO.getSpecList());
        dto.setReviews(productDTO.getReviews());
        dto.setQuantity(quantity);
        dto.setUnitPrice(unitPrice);
        dto.setProductId(productDTO.getId());
        dto.setCartItemId(cartItemId);
        return dto;
    }
}

