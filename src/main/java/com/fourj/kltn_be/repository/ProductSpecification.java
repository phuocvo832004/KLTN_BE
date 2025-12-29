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

            // Keyword search in title or description
            if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), keyword
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), keyword
                );
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
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


