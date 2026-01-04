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
                
                // Strategy 4: Typo handling (PostgreSQL Trigram Similarity)
                // This handles common typos like "iphonen" -> "iphone"
                if (keyword.length() >= 3) {
                    // Using PostgreSQL similarity() function with threshold 0.3
                    // Lower threshold = more forgiving for typos
                    Expression<Double> titleSimilarity = criteriaBuilder.function(
                        "similarity",
                        Double.class,
                        criteriaBuilder.lower(root.get("title")),
                        criteriaBuilder.literal(keyword.toLowerCase())
                    );
                    
                    Expression<Double> descSimilarity = criteriaBuilder.function(
                        "similarity",
                        Double.class,
                        criteriaBuilder.lower(root.get("description")),
                        criteriaBuilder.literal(keyword.toLowerCase())
                    );
                    
                    // Match if similarity >= 0.3 (30% similar)
                    Predicate titleSimilarityMatch = criteriaBuilder.greaterThanOrEqualTo(
                        titleSimilarity, 0.3
                    );
                    Predicate descSimilarityMatch = criteriaBuilder.greaterThanOrEqualTo(
                        descSimilarity, 0.3
                    );
                    
                    keywordPredicates.add(criteriaBuilder.or(titleSimilarityMatch, descSimilarityMatch));
                }
                
                // Strategy 5: Handle common typo patterns
                if (keyword.length() > 3) {
                    List<String> typoVariations = generateTypoVariations(keyword);
                    for (String variation : typoVariations) {
                        String pattern = "%" + variation.toLowerCase() + "%";
                        Predicate varTitleMatch = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")), pattern
                        );
                        Predicate varDescMatch = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")), pattern
                        );
                        keywordPredicates.add(criteriaBuilder.or(varTitleMatch, varDescMatch));
                    }
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
    
    /**
     * Generate common typo variations for a keyword
     * Examples:
     * - "iphonen" -> ["iphone"] (remove extra char at end)
     * - "ipphone" -> ["iphone"] (remove duplicate char)
     * - "ihpone" -> original keyword is kept in main search
     */
    private static List<String> generateTypoVariations(String keyword) {
        List<String> variations = new ArrayList<>();
        
        if (keyword == null || keyword.length() < 4) {
            return variations;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        // Variation 1: Remove last character (handles "iphonen" -> "iphone")
        if (lowerKeyword.length() > 3) {
            variations.add(lowerKeyword.substring(0, lowerKeyword.length() - 1));
        }
        
        // Variation 2: Remove first character (handles "aiphone" -> "iphone")
        if (lowerKeyword.length() > 3) {
            variations.add(lowerKeyword.substring(1));
        }
        
        // Variation 3: Remove duplicate consecutive characters
        StringBuilder deduplicated = new StringBuilder();
        char lastChar = '\0';
        for (char c : lowerKeyword.toCharArray()) {
            if (c != lastChar) {
                deduplicated.append(c);
                lastChar = c;
            }
        }
        String deduplicatedStr = deduplicated.toString();
        if (!deduplicatedStr.equals(lowerKeyword) && deduplicatedStr.length() >= 3) {
            variations.add(deduplicatedStr);
        }
        
        // Variation 4: Handle common character substitutions
        // Vietnamese keyboard typos: o/ô, a/ă/â, e/ê, u/ư, etc.
        String normalized = lowerKeyword
            .replace("ô", "o")
            .replace("ơ", "o")
            .replace("ă", "a")
            .replace("â", "a")
            .replace("ê", "e")
            .replace("ư", "u")
            .replace("đ", "d");
        
        if (!normalized.equals(lowerKeyword)) {
            variations.add(normalized);
        }
        
        return variations;
    }
}


