package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.dto.product.ProductRequest;
import com.ecommerce.inventory.dto.product.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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

    // ---------- Added in Version 6: Product Image Upload ----------

    // Uploads (or replaces, deleting the old file) the image for a product.
    ProductResponse uploadProductImage(Long productId, MultipartFile file);

    // Loads the raw bytes + content type of a product's image.
    ProductImageData getProductImage(Long productId);

    // Deletes a product's image from disk and clears the reference on the product.
    void deleteProductImage(Long productId);
}
