package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.PageResponse;
import com.fourj.kltn_be.dto.ProductDTO;
import com.fourj.kltn_be.dto.ProductSpecDTO;
import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.ProductCategory;
import com.fourj.kltn_be.entity.ProductSpec;
import com.fourj.kltn_be.repository.ProductRepository;
import com.fourj.kltn_be.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

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
        return productRepository.findById(id)
                .map(this::convertToDTO);
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
}

