package com.ecommerce.inventory.util;

import com.ecommerce.inventory.exception.InvalidSortFieldException;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Added in Version 4. Shared helper used by CategoryController and ProductController
 * to turn the ?sortBy=&direction= query parameters into a validated Spring Data Sort.
 *
 * allowedSortFields maps the field name accepted from the client to the actual JPA
 * property path used internally (e.g. "category" -> "category.name"), so callers can
 * expose friendly sort keys without leaking entity internals.
 */
public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static Sort buildSort(String sortBy, String direction,
                                  Map<String, String> allowedSortFields, String defaultField) {

        String jpaProperty;

        if (sortBy == null || sortBy.isBlank()) {
            jpaProperty = allowedSortFields.get(defaultField);
        } else if (allowedSortFields.containsKey(sortBy)) {
            jpaProperty = allowedSortFields.get(sortBy);
        } else {
            throw new InvalidSortFieldException(
                    "Invalid sortBy field: '" + sortBy + "'. Allowed values: " + allowedSortFields.keySet());
        }

        Sort.Direction sortDirection = (direction != null && direction.equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(sortDirection, jpaProperty);
    }
}
