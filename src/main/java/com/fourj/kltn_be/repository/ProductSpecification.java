package com.fourj.kltn_be.repository;

import com.fourj.kltn_be.dto.ProductFilterRequest;
import com.fourj.kltn_be.entity.Product;
import com.fourj.kltn_be.entity.ProductCategory;
import com.fourj.kltn_be.entity.SeasonCategory;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(ProductFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Enhanced keyword search with fuzzy matching
            if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                String keyword = filter.getKeyword().trim();
                List<Predicate> keywordPredicates = new ArrayList<>();
                
                // Strategy 1: Exact match (highest priority)
                String exactKeyword = "%" + keyword.toLowerCase() + "%";
                Predicate exactTitleMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), exactKeyword
                );
                Predicate exactDescMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), exactKeyword
                );
                keywordPredicates.add(criteriaBuilder.or(exactTitleMatch, exactDescMatch));
                
                // Strategy 2: Split keywords and match each word
                String[] keywords = keyword.toLowerCase().split("\\s+");
                if (keywords.length > 1) {
                    List<Predicate> wordPredicates = new ArrayList<>();
                    for (String word : keywords) {
                        if (word.length() > 2) { // Only search words with 3+ characters
                            String wordPattern = "%" + word + "%";
                            Predicate wordInTitle = criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")), wordPattern
                            );
                            Predicate wordInDesc = criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")), wordPattern
                            );
                            wordPredicates.add(criteriaBuilder.or(wordInTitle, wordInDesc));
                        }
                    }
                    if (!wordPredicates.isEmpty()) {
                        // Match if ANY word is found
                        keywordPredicates.add(criteriaBuilder.or(wordPredicates.toArray(new Predicate[0])));
                    }
                }
                
                // Strategy 3: Fuzzy match with character variations
                if (keyword.length() >= 4) {
                    // Remove spaces for continuous matching
                    String noSpaceKeyword = "%" + keyword.replaceAll("\\s+", "") + "%";
                    Predicate noSpaceTitle = criteriaBuilder.like(
                        criteriaBuilder.function("regexp_replace", String.class,
                            criteriaBuilder.lower(root.get("title")),
                            criteriaBuilder.literal("\\s+"),
                            criteriaBuilder.literal(""),
                            criteriaBuilder.literal("g")
                        ),
                        noSpaceKeyword
                    );
                    keywordPredicates.add(noSpaceTitle);
                }
                
                // Combine all keyword strategies with OR
                if (!keywordPredicates.isEmpty()) {
                    predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
                }
            }

            // Categories filter
            if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
                Join<Product, ProductCategory> categoryJoin = root.join("productCategories", JoinType.LEFT);
                predicates.add(categoryJoin.get("category").in(filter.getCategories()));
            }

            // Price range filter
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), filter.getMinPrice()
                ));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), filter.getMaxPrice()
                ));
            }

            // Rating filter
            if (filter.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("averageRating"), filter.getMinRating()
                ));
            }

            // Season type filter
            if (filter.getSeasonType() != null) {
                Join<Product, SeasonCategory> seasonJoin = root.join("seasonCategories", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(seasonJoin.get("type"), filter.getSeasonType()));
            }

            // Has discount filter
            if (filter.getHasDiscount() != null && filter.getHasDiscount()) {
                predicates.add(criteriaBuilder.isNotNull(root.get("sale")));
                predicates.add(criteriaBuilder.greaterThan(
                    root.get("sale"), BigDecimal.ZERO
                ));
            }

            // Ensure distinct results when joining
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


