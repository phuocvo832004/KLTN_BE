package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {
    private Long userId;
    private String productId;
    private ProductDTO product;
    private LocalDateTime createdAt;
}

