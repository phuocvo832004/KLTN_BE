package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.HomepageDataDTO;
import com.fourj.kltn_be.dto.LimitedDealDTO;
import com.fourj.kltn_be.dto.PageResponse;
import com.fourj.kltn_be.dto.ProductDTO;
import com.fourj.kltn_be.dto.ProductFilterRequest;
import com.fourj.kltn_be.dto.ProductSpecDTO;
import com.fourj.kltn_be.dto.ReviewDTO;
import com.fourj.kltn_be.dto.WishlistDTO;
import com.fourj.kltn_be.dto.WishlistRequest;
import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.ProductCategory;
import com.fourj.kltn_be.entity.ProductSpec;
import com.fourj.kltn_be.entity.Review;
import com.fourj.kltn_be.entity.Sale;
import com.fourj.kltn_be.entity.Wishlist;
import com.fourj.kltn_be.repository.ProductRepository;
import com.fourj.kltn_be.repository.ProductSpecification;
import com.fourj.kltn_be.repository.ReviewRepository;
import com.fourj.kltn_be.repository.SaleRepository;
import com.fourj.kltn_be.repository.WishlistRepository;
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
    private final WishlistRepository wishlistRepository;

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
                        List<ReviewDTO> reviews = reviewRepository.findByProductId(id).stream()
                                .map(this::convertReviewToDTO)
                                .collect(Collectors.toList());
                        dto.setReviews(reviews);
                    }
                    return dto;
                });
    }

    public List<ProductDTO> searchProducts(String title) {
        // Use enhanced search with ProductSpecification
        ProductFilterRequest filterRequest = new ProductFilterRequest();
        filterRequest.setKeyword(title);
        return productRepository.findAll(ProductSpecification.filterProducts(filterRequest)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> searchProducts(String title, Pageable pageable) {
        // Use enhanced search with ProductSpecification
        ProductFilterRequest filterRequest = new ProductFilterRequest();
        filterRequest.setKeyword(title);
        Page<Product> page = productRepository.findAll(ProductSpecification.filterProducts(filterRequest), pageable);
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

    public List<ProductDTO> getProductsBySeasonType(Integer type) {
        return productRepository.findBySeasonType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getProductsBySeasonType(Integer type, Pageable pageable) {
        Page<Product> page = productRepository.findBySeasonType(type, pageable);
        return convertToPageResponse(page);
    }

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

    public List<ProductDTO> getPopularProducts() {
        return productRepository.findPopularProducts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getPopularProducts(Pageable pageable) {
        Page<Product> page = productRepository.findPopularProducts(pageable);
        return convertToPageResponse(page);
    }

    public List<ProductDTO> getPopularProducts(Double minRating) {
        return productRepository.findPopularProductsWithMinRating(minRating).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> getPopularProducts(Double minRating, Pageable pageable) {
        Page<Product> page = productRepository.findPopularProductsWithMinRating(minRating, pageable);
        return convertToPageResponse(page);
    }

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

    public List<ProductDTO> getProductsWithDeals() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> products = saleRepository.findProductsWithActiveSales(now);
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productRepository.findByIdIn(ids).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> filterProducts(ProductFilterRequest filterRequest) {
        return productRepository.findAll(ProductSpecification.filterProducts(filterRequest)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<ProductDTO> filterProducts(ProductFilterRequest filterRequest, Pageable pageable) {
        Page<Product> page = productRepository.findAll(ProductSpecification.filterProducts(filterRequest), pageable);
        return convertToPageResponse(page);
    }

    public PageResponse<ProductDTO> getProductsWithDeals(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Product> page = saleRepository.findProductsWithActiveSales(now, pageable);
        return convertToPageResponse(page);
    }

    public HomepageDataDTO getHomepageData(int specialOffersLimit, int newArrivalsLimit, 
                                           int popularLimit, int limitedDealsLimit) {
        Pageable specialOffersPageable = PageRequest.of(0, specialOffersLimit);
        List<ProductDTO> specialOffers = getProductsBySeasonType(0, specialOffersPageable).getContent();
        
        Pageable newArrivalsPageable = PageRequest.of(0, newArrivalsLimit, 
                Sort.by("createdAt").descending());
        List<ProductDTO> newArrivals = getProductsBySeasonType(1, newArrivalsPageable).getContent();
        
        Pageable popularPageable = PageRequest.of(0, popularLimit, 
                Sort.by("averageRating").descending());
        List<ProductDTO> popularProducts = getPopularProducts(popularPageable).getContent();
        
        Pageable limitedDealsPageable = PageRequest.of(0, limitedDealsLimit, 
                Sort.by("discountPercentage").descending());
        List<LimitedDealDTO> limitedDeals = getLimitedDeals(limitedDealsPageable).getContent();
        
        return new HomepageDataDTO(specialOffers, newArrivals, popularProducts, limitedDeals);
    }

    private LimitedDealDTO convertToLimitedDealDTO(Sale sale) {
        LimitedDealDTO dto = new LimitedDealDTO();
        Product product = sale.getProduct();
        
        dto.setProductId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getPrice());
        dto.setImurl(product.getImurl());
        dto.setImgUrl(product.getImgUrl());
        dto.setAverageRating(product.getAverageRating());
        dto.setCategories(product.getCategories());
        
        dto.setSaleId(sale.getId());
        dto.setSalePrice(sale.getSalePrice());
        dto.setDiscountPercentage(sale.getDiscountPercentage());
        dto.setStartDate(sale.getStartDate());
        dto.setEndDate(sale.getEndDate());
        
        if (sale.getBranch() != null) {
            dto.setBranchId(sale.getBranch().getId());
            dto.setBranchName(sale.getBranch().getName());
        }
        
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
        dto.setRating(product.getAverageRating());
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

    // Wishlist methods
    @Transactional
    public WishlistDTO addToWishlist(Long userId, WishlistRequest request) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new IllegalArgumentException("Product already in wishlist");
        }
        
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setProductId(request.getProductId());
        
        Wishlist saved = wishlistRepository.save(wishlist);
        return convertWishlistToDTO(saved);
    }

    @Transactional
    public void removeFromWishlist(Long userId, String productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public List<WishlistDTO> getUserWishlist(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        return wishlists.stream()
                .map(this::convertWishlistToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getUserWishlistProducts(Long userId) {
        List<String> productIds = wishlistRepository.findProductIdsByUserId(userId);
        return getProductsByIds(productIds);
    }

    public boolean isInWishlist(Long userId, String productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    public long getWishlistCount(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }

    private WishlistDTO convertWishlistToDTO(Wishlist wishlist) {
        WishlistDTO dto = new WishlistDTO();
        dto.setUserId(wishlist.getUserId());
        dto.setProductId(wishlist.getProductId());
        dto.setCreatedAt(wishlist.getCreatedAt());
        
        // Optionally include product details
        if (wishlist.getProduct() != null) {
            dto.setProduct(convertToDTO(wishlist.getProduct()));
        }
        
        return dto;
    }
}

