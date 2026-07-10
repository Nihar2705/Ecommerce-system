package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.category.CategoryRequest;
import com.ecommerce.inventory.dto.category.CategoryResponse;
import com.ecommerce.inventory.dto.common.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    // Modified in Version 3: now returns a paginated result instead of a full List
    PaginatedResponse<CategoryResponse> getAllCategories(Pageable pageable);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
