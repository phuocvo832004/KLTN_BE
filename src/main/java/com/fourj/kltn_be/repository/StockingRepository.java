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
    Optional<Stocking> findByProductIdAndBranchId(String productId, Long branchId);
    
    List<Stocking> findByProductId(String productId);
    
    List<Stocking> findByBranchId(Long branchId);
    
    @Query("SELECT s.branch FROM Stocking s WHERE s.product.id = :productId " +
           "AND s.availableQuantity > 0 ORDER BY s.availableQuantity DESC")
    List<com.fourj.kltn_be.entity.Branch> findBranchesWithStock(@Param("productId") String productId);
    
    @Query("SELECT s.branch FROM Stocking s WHERE s.product.id = :productId " +
           "AND s.availableQuantity >= :minQuantity ORDER BY s.availableQuantity DESC")
    List<com.fourj.kltn_be.entity.Branch> findBranchesWithStockMinQuantity(@Param("productId") String productId, 
                                                                           @Param("minQuantity") Integer minQuantity);
    
    @Query("SELECT s FROM Stocking s WHERE s.branch.id = :branchId " +
           "AND s.availableQuantity <= s.minStockLevel")
    List<Stocking> findLowStockItemsByBranch(@Param("branchId") Long branchId);
    
    @Query("SELECT s FROM Stocking s WHERE s.branch.id = :branchId " +
           "AND s.availableQuantity > 0")
    List<Stocking> findInStockItemsByBranch(@Param("branchId") Long branchId);
}

