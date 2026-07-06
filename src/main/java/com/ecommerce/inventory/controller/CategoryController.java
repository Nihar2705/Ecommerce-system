package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.category.CategoryRequest;
import com.ecommerce.inventory.dto.category.CategoryResponse;
import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ADMIN and USER
    // Modified in Version 3: supports pagination via ?page=&size= (defaults: page=0, size=5)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<PaginatedResponse<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    // ADMIN and USER
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                             @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully with id: " + id);
    }
}
