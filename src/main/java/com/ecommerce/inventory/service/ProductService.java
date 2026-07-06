package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.dto.product.ProductRequest;
import com.ecommerce.inventory.dto.product.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    // Modified in Version 4: adds optional filtering (name, category, minPrice, maxPrice)
    // on top of the existing pagination/sorting support carried by Pageable.
    PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable,
                                                        String name,
                                                        String category,
                                                        Double minPrice,
                                                        Double maxPrice);

    // Added in Version 5: exactly one of keyword / category / (minPrice and/or maxPrice)
    // must be supplied per call - see ProductServiceImpl for validation.
    PaginatedResponse<ProductResponse> searchProducts(Pageable pageable,
                                                        String keyword,
                                                        String category,
                                                        Double minPrice,
                                                        Double maxPrice);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
