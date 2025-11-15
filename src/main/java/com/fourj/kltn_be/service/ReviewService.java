package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.ReviewDTO;
import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.Review;
import com.fourj.kltn_be.repository.ProductRepository;
import com.fourj.kltn_be.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public List<ReviewDTO> getProductReviews(String productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Review review = new Review();
        review.setProduct(product);
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setUserId(reviewDTO.getUserId());
        review.setReviewDate(reviewDTO.getReviewDate());
        
        Review saved = reviewRepository.save(review);
        
        // Update product average rating
        productService.updateAverageRating(product.getId());
        
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        String productId = review.getProduct().getId();
        reviewRepository.deleteById(reviewId);
        productService.updateAverageRating(productId);
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setProductId(review.getProduct().getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setUserId(review.getUserId());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setReviewDate(review.getReviewDate());
        return dto;
    }
}

