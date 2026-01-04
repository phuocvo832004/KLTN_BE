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
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String keyword = filter.getKeyword().trim().toLowerCase();

                List<Predicate> strongMatches = new ArrayList<>();
                List<Predicate> fuzzyMatches = new ArrayList<>();

                String pattern = "%" + keyword + "%";

                strongMatches.add(cb.like(cb.lower(root.get("title")), pattern));
                strongMatches.add(cb.like(cb.lower(root.get("description")), pattern));

                Expression<String> titleNoSpace = cb.function(
                        "replace",
                        String.class,
                        cb.lower(root.get("title")),
                        cb.literal(" "),
                        cb.literal("")
                );

                fuzzyMatches.add(cb.like(titleNoSpace, "%" + keyword.replace(" ", "") + "%"));

                Expression<Double> titleSimilarity = cb.function(
                        "similarity",
                        Double.class,
                        cb.lower(root.get("title")),
                        cb.literal(keyword)
                );

                Expression<Double> descSimilarity = cb.function(
                        "similarity",
                        Double.class,
                        cb.lower(root.get("description")),
                        cb.literal(keyword)
                );

                fuzzyMatches.add(cb.greaterThanOrEqualTo(titleSimilarity, 0.2));
                fuzzyMatches.add(cb.greaterThanOrEqualTo(descSimilarity, 0.2));

                for (String variation : generateTypoVariations(keyword)) {
                    String varPattern = "%" + variation + "%";
                    fuzzyMatches.add(cb.like(cb.lower(root.get("title")), varPattern));
                    fuzzyMatches.add(cb.like(cb.lower(root.get("description")), varPattern));
                }

                predicates.add(
                        cb.or(
                                cb.or(strongMatches.toArray(new Predicate[0])),
                                cb.or(fuzzyMatches.toArray(new Predicate[0]))
                        )
                );
            }

            if (filter.getCategories() != null && !filter.getCategories().isEmpty()) {
                Join<Product, ProductCategory> categoryJoin =
                        root.join("productCategories", JoinType.LEFT);
                predicates.add(categoryJoin.get("category").in(filter.getCategories()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), filter.getMinRating()));
            }

            if (filter.getSeasonType() != null) {
                Join<Product, SeasonCategory> seasonJoin =
                        root.join("seasonCategories", JoinType.LEFT);
                predicates.add(cb.equal(seasonJoin.get("type"), filter.getSeasonType()));
            }

            if (Boolean.TRUE.equals(filter.getHasDiscount())) {
                predicates.add(cb.isNotNull(root.get("sale")));
                predicates.add(cb.greaterThan(root.get("sale"), BigDecimal.ZERO));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static List<String> generateTypoVariations(String keyword) {
        List<String> variations = new ArrayList<>();

        if (keyword.length() < 4) {
            return variations;
        }

        variations.add(keyword.substring(0, keyword.length() - 1));
        variations.add(keyword.substring(1));

        StringBuilder dedup = new StringBuilder();
        char last = '\0';
        for (char c : keyword.toCharArray()) {
            if (c != last) {
                dedup.append(c);
                last = c;
            }
        }
        if (!dedup.toString().equals(keyword)) {
            variations.add(dedup.toString());
        }

        String normalized = keyword
                .replace("ô", "o")
                .replace("ơ", "o")
                .replace("ă", "a")
                .replace("â", "a")
                .replace("ê", "e")
                .replace("ư", "u")
                .replace("đ", "d");

        if (!normalized.equals(keyword)) {
            variations.add(normalized);
        }

        return variations;
    }
}
