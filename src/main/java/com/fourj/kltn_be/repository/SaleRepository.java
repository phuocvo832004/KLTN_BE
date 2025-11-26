package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    // Tìm sale của một sản phẩm tại một chi nhánh
    Optional<Sale> findByProductIdAndBranchId(String productId, Long branchId);
    
    // Tìm tất cả sale của một sản phẩm
    List<Sale> findByProductId(String productId);
    
    // Tìm tất cả sale của một chi nhánh
    List<Sale> findByBranchId(Long branchId);
    
    // Tìm các sale đang hoạt động (isActive = true và trong khoảng thời gian)
    @Query("SELECT s FROM Sale s WHERE s.isActive = true " +
           "AND s.product.id = :productId " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now)")
    List<Sale> findActiveSalesByProduct(@Param("productId") String productId, 
                                        @Param("now") LocalDateTime now);
    
    // Tìm các sale đang hoạt động tại một chi nhánh
    @Query("SELECT s FROM Sale s WHERE s.isActive = true " +
           "AND s.branch.id = :branchId " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now)")
    List<Sale> findActiveSalesByBranch(@Param("branchId") Long branchId, 
                                       @Param("now") LocalDateTime now);
    
    // Tìm các chi nhánh đang có sale cho một sản phẩm
    @Query("SELECT s.branch FROM Sale s WHERE s.isActive = true " +
           "AND s.product.id = :productId " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now)")
    List<com.fourj.kltn_be.entity.Branch> findBranchesWithActiveSale(@Param("productId") String productId, 
                                                                      @Param("now") LocalDateTime now);
    
    // Limited Deals: Tìm tất cả sale đang hoạt động
    @Query("SELECT s FROM Sale s WHERE s.isActive = true " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now)")
    List<Sale> findAllActiveSales(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Sale s WHERE s.isActive = true " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now)")
    Page<Sale> findAllActiveSales(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Limited Deals: Tìm các sản phẩm đang có sale (distinct products)
    @Query("SELECT DISTINCT s.product FROM Sale s WHERE s.isActive = true " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now)")
    List<Product> findProductsWithActiveSales(@Param("now") LocalDateTime now);
    
    @Query("SELECT DISTINCT s.product FROM Sale s WHERE s.isActive = true " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now)")
    Page<Product> findProductsWithActiveSales(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Limited Deals: Tìm sale có discount cao nhất (sorted by discount percentage)
    @Query("SELECT s FROM Sale s WHERE s.isActive = true " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now) " +
           "ORDER BY s.discountPercentage DESC")
    List<Sale> findActiveSalesOrderByDiscount(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Sale s WHERE s.isActive = true " +
           "AND (s.startDate IS NULL OR s.startDate <= :now) " +
           "AND (s.endDate IS NULL OR s.endDate >= :now) " +
           "ORDER BY s.discountPercentage DESC")
    Page<Sale> findActiveSalesOrderByDiscount(@Param("now") LocalDateTime now, Pageable pageable);
}

