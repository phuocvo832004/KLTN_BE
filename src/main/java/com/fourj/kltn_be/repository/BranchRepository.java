package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByCode(String code);
    
    List<Branch> findByIsActiveTrue();
    
    @Query("SELECT b FROM Branch b WHERE b.isActive = true ORDER BY b.name")
    List<Branch> findAllActiveBranches();
}

