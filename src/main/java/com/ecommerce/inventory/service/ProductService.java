package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.dto.product.ProductRequest;
import com.ecommerce.inventory.dto.product.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    // Modified in Version 3: now returns a paginated result instead of a full List
    PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
