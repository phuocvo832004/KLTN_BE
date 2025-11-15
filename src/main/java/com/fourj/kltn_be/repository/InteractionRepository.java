package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Integer> {
    List<Interaction> findByUserId(Long userId);
    List<Interaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}

