package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Stocking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockingRepository extends JpaRepository<Stocking, Long> {
    // Tìm tồn kho của một sản phẩm tại một chi nhánh
    Optional<Stocking> findByProductIdAndBranchId(String productId, Long branchId);
    
    // Tìm tất cả tồn kho của một sản phẩm
    List<Stocking> findByProductId(String productId);
    
    // Tìm tất cả tồn kho của một chi nhánh
    List<Stocking> findByBranchId(Long branchId);
    
    // Tìm các chi nhánh còn hàng (availableQuantity > 0) cho một sản phẩm
    @Query("SELECT s.branch FROM Stocking s WHERE s.product.id = :productId " +
           "AND s.availableQuantity > 0 ORDER BY s.availableQuantity DESC")
    List<com.fourj.kltn_be.entity.Branch> findBranchesWithStock(@Param("productId") String productId);
    
    // Tìm các chi nhánh còn hàng với số lượng tối thiểu
    @Query("SELECT s.branch FROM Stocking s WHERE s.product.id = :productId " +
           "AND s.availableQuantity >= :minQuantity ORDER BY s.availableQuantity DESC")
    List<com.fourj.kltn_be.entity.Branch> findBranchesWithStockMinQuantity(@Param("productId") String productId, 
                                                                           @Param("minQuantity") Integer minQuantity);
    
    // Tìm các sản phẩm sắp hết hàng tại một chi nhánh (availableQuantity <= minStockLevel)
    @Query("SELECT s FROM Stocking s WHERE s.branch.id = :branchId " +
           "AND s.availableQuantity <= s.minStockLevel")
    List<Stocking> findLowStockItemsByBranch(@Param("branchId") Long branchId);
    
    // Tìm các sản phẩm còn hàng tại một chi nhánh
    @Query("SELECT s FROM Stocking s WHERE s.branch.id = :branchId " +
           "AND s.availableQuantity > 0")
    List<Stocking> findInStockItemsByBranch(@Param("branchId") Long branchId);
}

