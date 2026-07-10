package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.category.CategoryRequest;
import com.ecommerce.inventory.dto.category.CategoryResponse;
import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return toResponse(savedCategory);
    }

    @Override
    public PaginatedResponse<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        Page<CategoryResponse> responsePage = categoryPage.map(this::toResponse);
        return PaginatedResponse.from(responsePage);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryEntityById(id);
        return toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryEntityById(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return toResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = findCategoryEntityById(id);
        categoryRepository.delete(category);
    }

    // Fetches the managed Category entity by id (ProductServiceImpl has its own copy
    // since it uses CategoryRepository directly to avoid a service-to-service dependency).
    private Category findCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
