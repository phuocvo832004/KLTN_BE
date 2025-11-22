package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.SeasonCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonCategoryRepository extends JpaRepository<SeasonCategory, Long> {
    List<SeasonCategory> findByType(Integer type);
    
    List<SeasonCategory> findByProductId(String productId);
    
    void deleteByProductId(String productId);
}

