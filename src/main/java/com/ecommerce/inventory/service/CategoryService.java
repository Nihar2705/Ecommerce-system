package com.ecommerce.inventory.service;

import com.ecommerce.inventory.entity.Category;

import java.util.List;

public interface CategoryService {

    Category createCategory(Category category);

    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    Category updateCategory(Long id, Category categoryDetails);

    void deleteCategory(Long id);
}
