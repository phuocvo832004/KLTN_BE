package com.fourj.kltn_be.controller;

import com.fourj.kltn_be.dto.HomepageDataDTO;
import com.fourj.kltn_be.dto.LimitedDealDTO;
import com.fourj.kltn_be.dto.PageResponse;
import com.fourj.kltn_be.dto.ProductDTO;
import com.fourj.kltn_be.dto.WishlistDTO;
import com.fourj.kltn_be.dto.WishlistRequest;
import com.fourj.kltn_be.service.ProductService;
import com.fourj.kltn_be.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.getAllProducts());
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductDTO> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "-1") int reviewPage,
            @RequestParam(required = false, defaultValue = "10") int reviewSize,
            @RequestParam(required = false, defaultValue = "reviewDate") String reviewSortBy,
            @RequestParam(required = false, defaultValue = "desc") String reviewSortDir) {
        return productService.getProductById(id, reviewPage, reviewSize, reviewSortBy, reviewSortDir)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.searchProducts(title));
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductDTO> response = productService.searchProducts(title, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.getProductsByCategory(category));
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductDTO> response = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            ProductDTO created = productService.createProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable String id, @RequestBody ProductDTO productDTO) {
        return productService.updateProduct(id, productDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/homepage")
    public ResponseEntity<HomepageDataDTO> getHomepageData(
            @RequestParam(required = false, defaultValue = "8") int specialOffersLimit,
            @RequestParam(required = false, defaultValue = "8") int newArrivalsLimit,
            @RequestParam(required = false, defaultValue = "8") int popularLimit,
            @RequestParam(required = false, defaultValue = "8") int limitedDealsLimit) {
        
        HomepageDataDTO homepageData = productService.getHomepageData(
                specialOffersLimit, newArrivalsLimit, popularLimit, limitedDealsLimit);
        return ResponseEntity.ok(homepageData);
    }

    @GetMapping("/special-offers")
    public ResponseEntity<?> getSpecialOffers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.getSpecialOffers());
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductDTO> response = productService.getSpecialOffers(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<?> getNewArrivals(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.getNewArrivals());
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductDTO> response = productService.getNewArrivals(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/season/{type}")
    public ResponseEntity<?> getProductsBySeasonType(
            @PathVariable Integer type,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.getProductsBySeasonType(type));
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductDTO> response = productService.getProductsBySeasonType(type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularProducts(
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "averageRating") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (page < 0 || size <= 0) {
            if (minRating != null) {
                return ResponseEntity.ok(productService.getPopularProducts(minRating));
            }
            return ResponseEntity.ok(productService.getPopularProducts());
        }
        
        if (minRating != null) {
            PageResponse<ProductDTO> response = productService.getPopularProducts(minRating, pageable);
            return ResponseEntity.ok(response);
        }
        
        PageResponse<ProductDTO> response = productService.getPopularProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/limited-deals")
    public ResponseEntity<?> getLimitedDeals(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "discountPercentage") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.getLimitedDeals());
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<LimitedDealDTO> response = productService.getLimitedDeals(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products-with-deals")
    public ResponseEntity<?> getProductsWithDeals(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "24") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        
        if (page < 0 || size <= 0) {
            return ResponseEntity.ok(productService.getProductsWithDeals());
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductDTO> response = productService.getProductsWithDeals(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<ProductDTO>> getProductsByIds(@RequestParam List<String> ids) {
        List<ProductDTO> products = productService.getProductsByIds(ids);
        return ResponseEntity.ok(products);
    }

    // Wishlist endpoints
    @PostMapping("/wishlist")
    public ResponseEntity<?> addToWishlist(@RequestBody WishlistRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            WishlistDTO wishlist = productService.addToWishlist(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(wishlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add to wishlist");
        }
    }

    @DeleteMapping("/wishlist")
    public ResponseEntity<Void> removeFromWishlist(@RequestParam String productId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            productService.removeFromWishlist(userId, productId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<WishlistDTO>> getUserWishlist() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<WishlistDTO> wishlist = productService.getUserWishlist(userId);
            return ResponseEntity.ok(wishlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/wishlist/products")
    public ResponseEntity<List<ProductDTO>> getUserWishlistProducts() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            List<ProductDTO> products = productService.getUserWishlistProducts(userId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/wishlist/check")
    public ResponseEntity<Boolean> isInWishlist(@RequestParam String productId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            boolean isInWishlist = productService.isInWishlist(userId, productId);
            return ResponseEntity.ok(isInWishlist);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/wishlist/count")
    public ResponseEntity<Long> getWishlistCount() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            long count = productService.getWishlistCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.ok(0L);
        }
    }
}

