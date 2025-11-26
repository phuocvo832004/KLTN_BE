package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.LimitedDealDTO;
import com.fourj.kltn_be.dto.PageResponse;
import com.fourj.kltn_be.dto.ProductDTO;
import com.fourj.kltn_be.dto.ProductSpecDTO;
import com.fourj.kltn_be.dto.ReviewDTO;
import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.ProductCategory;
import com.fourj.kltn_be.entity.ProductSpec;
import com.fourj.kltn_be.entity.Review;
import com.fourj.kltn_be.entity.Sale;
import com.fourj.kltn_be.repository.ProductRepository;
import com.fourj.kltn_be.repository.ReviewRepository;
import com.fourj.kltn_be.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final SaleRepository saleRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return convertToPageResponse(page);
    }

    public Optional<ProductDTO> getProductById(String id) {
        return getProductById(id, -1, 10, "reviewDate", "desc");
    }

    public Optional<ProductDTO> getProductById(String id, int reviewPage, int reviewSize, String reviewSortBy, String reviewSortDir) {
        return productRepository.findById(id)
                .map(product -> {
                    ProductDTO dto = convertToDTO(product);
                    // Fetch reviews for product detail with pagination
                    if (reviewPage >= 0 && reviewSize > 0) {
                        Sort sort = reviewSortDir.equalsIgnoreCase("desc") 
                                ? Sort.by(reviewSortBy).descending() 
                                : Sort.by(reviewSortBy).ascending();
                        Pageable pageable = PageRequest.of(reviewPage, reviewSize, sort);
                        Page<Review> reviewPageResult = reviewRepository.findByProductId(id, pageable);
                        List<ReviewDTO> reviews = reviewPageResult.getContent().stream()
                                .map(this::convertReviewToDTO)
                                .collect(Collectors.toList());
                        dto.setReviews(reviews);
                    } else {
                        // Return all reviews if pagination not requested (backward compatibility)
                        List<ReviewDTO> reviews = reviewRepository.findByProductId(id).stream()
                                .map(this::convertReviewToDTO)
                                .collect(Collectors.toList());
                        dto.setReviews(reviews);
                    }
                    return dto;
                });
    }

    public List<ProductDTO> searchProducts(String title) {
        return productRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> searchProducts(String title, Pageable pageable) {
        Page<Product> page = productRepository.findByTitleContainingIgnoreCase(title, pageable);
        return convertToPageResponse(page);
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        Page<Product> page = productRepository.findByCategory(category, pageable);
        return convertToPageResponse(page);
    }

    // Get products by season type (0 = special offer, 1 = new arrivals, etc.)
    public List<ProductDTO> getProductsBySeasonType(Integer type) {
        return productRepository.findBySeasonType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getProductsBySeasonType(Integer type, Pageable pageable) {
        Page<Product> page = productRepository.findBySeasonType(type, pageable);
        return convertToPageResponse(page);
    }

    // Convenience methods for special offers (type = 0) and new arrivals (type = 1)
    public List<ProductDTO> getSpecialOffers() {
        return getProductsBySeasonType(0);
    }

    public PageResponse<ProductDTO> getSpecialOffers(Pageable pageable) {
        return getProductsBySeasonType(0, pageable);
    }

    public List<ProductDTO> getNewArrivals() {
        return getProductsBySeasonType(1);
    }

    public PageResponse<ProductDTO> getNewArrivals(Pageable pageable) {
        return getProductsBySeasonType(1, pageable);
    }

    // ==================== POPULAR PRODUCTS ====================
    
    /**
     * Get popular products sorted by average rating (descending)
     */
    public List<ProductDTO> getPopularProducts() {
        return productRepository.findPopularProducts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getPopularProducts(Pageable pageable) {
        Page<Product> page = productRepository.findPopularProducts(pageable);
        return convertToPageResponse(page);
    }

    /**
     * Get popular products with minimum rating threshold
     */
    public List<ProductDTO> getPopularProducts(Double minRating) {
        return productRepository.findPopularProductsWithMinRating(minRating).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getPopularProducts(Double minRating, Pageable pageable) {
        Page<Product> page = productRepository.findPopularProductsWithMinRating(minRating, pageable);
        return convertToPageResponse(page);
    }

    // ==================== LIMITED DEALS ====================
    
    /**
     * Get products with active sales (limited deals)
     */
    public List<LimitedDealDTO> getLimitedDeals() {
        LocalDateTime now = LocalDateTime.now();
        List<Sale> activeSales = saleRepository.findActiveSalesOrderByDiscount(now);
        return activeSales.stream()
                .map(this::convertToLimitedDealDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<LimitedDealDTO> getLimitedDeals(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Sale> page = saleRepository.findActiveSalesOrderByDiscount(now, pageable);
        return convertToLimitedDealPageResponse(page);
    }

    /**
     * Get distinct products that have active sales
     */
    public List<ProductDTO> getProductsWithDeals() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> products = saleRepository.findProductsWithActiveSales(now);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getProductsWithDeals(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Product> page = saleRepository.findProductsWithActiveSales(now, pageable);
        return convertToPageResponse(page);
    }

    private LimitedDealDTO convertToLimitedDealDTO(Sale sale) {
        LimitedDealDTO dto = new LimitedDealDTO();
        Product product = sale.getProduct();
        
        // Product information
        dto.setProductId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getPrice());
        dto.setImurl(product.getImurl());
        dto.setImgUrl(product.getImgUrl());
        dto.setAverageRating(product.getAverageRating());
        dto.setCategories(product.getCategories());
        
        // Sale information
        dto.setSaleId(sale.getId());
        dto.setSalePrice(sale.getSalePrice());
        dto.setDiscountPercentage(sale.getDiscountPercentage());
        dto.setStartDate(sale.getStartDate());
        dto.setEndDate(sale.getEndDate());
        
        if (sale.getBranch() != null) {
            dto.setBranchId(sale.getBranch().getId());
            dto.setBranchName(sale.getBranch().getName());
        }
        
        // Calculated fields
        if (product.getPrice() != null && sale.getSalePrice() != null) {
            dto.setSavedAmount(product.getPrice().subtract(sale.getSalePrice()));
        }
        
        if (sale.getEndDate() != null) {
            Duration duration = Duration.between(LocalDateTime.now(), sale.getEndDate());
            dto.setHoursRemaining(duration.isNegative() ? 0L : duration.toHours());
        }
        
        return dto;
    }

    private PageResponse<LimitedDealDTO> convertToLimitedDealPageResponse(Page<Sale> page) {
        List<LimitedDealDTO> content = page.getContent().stream()
                .map(this::convertToLimitedDealDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product saved = productRepository.save(product);
        updateAverageRating(saved.getId());
        return convertToDTO(saved);
    }

    @Transactional
    public Optional<ProductDTO> updateProduct(String id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(productDTO.getTitle());
                    existing.setDescription(productDTO.getDescription());
                    existing.setPrice(productDTO.getPrice());
                    existing.setImurl(productDTO.getImurl());
                    existing.setImgUrl(productDTO.getImgUrl());
                    existing.setSale(productDTO.getSale());
                    existing.setCategories(productDTO.getCategories());
                    existing.setSpecs(productDTO.getSpecs());
                    existing.setRelatedProducts(productDTO.getRelatedProducts());
                    Product saved = productRepository.save(existing);
                    return convertToDTO(saved);
                });
    }

    @Transactional
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void updateAverageRating(String productId) {
        Double avgRating = reviewRepository.calculateAverageRating(productId);
        productRepository.findById(productId).ifPresent(product -> {
            product.setAverageRating(avgRating != null ? avgRating : 0.0);
            productRepository.save(product);
        });
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImurl(product.getImurl());
        dto.setImgUrl(product.getImgUrl());
        dto.setSale(product.getSale());
        dto.setCategories(product.getCategories());
        dto.setSpecs(product.getSpecs());
        dto.setAverageRating(product.getAverageRating());
        dto.setRating(product.getAverageRating()); // Set rating from averageRating
        dto.setRelatedProducts(product.getRelatedProducts());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        if (product.getProductCategories() != null) {
            dto.setCategoryList(product.getProductCategories().stream()
                    .map(ProductCategory::getCategory)
                    .collect(Collectors.toList()));
        }
        
        if (product.getProductSpecs() != null) {
            dto.setSpecList(product.getProductSpecs().stream()
                    .map(ps -> new ProductSpecDTO(ps.getSpecKey(), ps.getSpecValue()))
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImurl(dto.getImurl());
        product.setImgUrl(dto.getImgUrl());
        product.setSale(dto.getSale());
        product.setCategories(dto.getCategories());
        product.setSpecs(dto.getSpecs());
        product.setAverageRating(dto.getAverageRating());
        product.setRelatedProducts(dto.getRelatedProducts());
        return product;
    }

    private PageResponse<ProductDTO> convertToPageResponse(Page<Product> page) {
        List<ProductDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private ReviewDTO convertReviewToDTO(Review review) {
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

