package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findById(String id);
    
    List<Product> findByTitleContainingIgnoreCase(String title);
    
    Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // Option 1: Use ProductCategory relationship (recommended if data exists in product_categories table)
    @Query("SELECT DISTINCT p FROM Product p JOIN p.productCategories pc WHERE pc.category = :category")
    List<Product> findByCategory(@Param("category") String category);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.productCategories pc WHERE pc.category = :category")
    Page<Product> findByCategory(@Param("category") String category, Pageable pageable);
    
    // Option 2: Use native query for PostgreSQL array (use if categories are stored in array column)
    @Query(value = "SELECT * FROM products WHERE categories IS NOT NULL AND :category = ANY(categories)", nativeQuery = true)
    List<Product> findByCategoryFromArray(@Param("category") String category);
    
    @Query(value = "SELECT * FROM products WHERE categories IS NOT NULL AND :category = ANY(categories)", nativeQuery = true, countQuery = "SELECT COUNT(*) FROM products WHERE categories IS NOT NULL AND :category = ANY(categories)")
    Page<Product> findByCategoryFromArray(@Param("category") String category, Pageable pageable);
    
    List<Product> findByAverageRatingGreaterThanEqual(Double minRating);
}

