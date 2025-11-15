package com.fourj.kltn_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer reviewId;
    private String productId;
    private Integer rating;
    private String comment;
    private String userId;
    private LocalDateTime createdAt;
    private String reviewDate;
}

