package com.fourj.kltn_be.controller;

import com.fourj.kltn_be.dto.PageResponse;
import com.fourj.kltn_be.dto.ReviewDTO;
import com.fourj.kltn_be.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductReviews(
            @PathVariable String productId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "reviewDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        if (page < 0 || size <= 0) {
            // Return non-paginated response for backward compatibility
            return ResponseEntity.ok(reviewService.getProductReviews(productId));
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ReviewDTO> response = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewDTO reviewDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(reviewDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

