package com.ecommerce.inventory.specification;

import com.ecommerce.inventory.entity.Product;
import org.springframework.data.jpa.domain.Specification;

/**
 * Added in Version 4. Builds small, composable Specification<Product> predicates for
 * optional filtering by name, category name, minPrice, and maxPrice. Each method
 * returns null when its filter value isn't supplied, and Spring's Specification.and()
 * safely ignores null predicates - so any combination of filters (or none) works.
 */
public class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategory(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.join("category").get("name")), "%" + categoryName.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasMinPrice(Double minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Product> hasMaxPrice(Double maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    // Added in Version 5: matches products whose name OR description contains the
    // given keyword, case-insensitively. Used by GET /api/products/search?keyword=...
    public static Specification<Product> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Combines all optional filters into a single Specification. Any filter left null
     * is skipped, so passing all nulls returns "no filtering" (all products).
     */
    public static Specification<Product> withFilters(String name, String category,
                                                       Double minPrice, Double maxPrice) {
        return Specification.where(hasName(name))
                .and(hasCategory(category))
                .and(hasMinPrice(minPrice))
                .and(hasMaxPrice(maxPrice));
    }
}
