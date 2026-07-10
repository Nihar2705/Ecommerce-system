package com.ecommerce.inventory.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Added in Version 3. Generic wrapper used to return paginated results from any
 * GET-all endpoint while keeping the existing DTO structure (CategoryResponse,
 * ProductResponse, etc.) unchanged - they are simply carried in "content".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /**
     * Builds a PaginatedResponse from a Spring Data Page, e.g.:
     * PaginatedResponse.from(categoryPage.map(this::toResponse))
     */
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
