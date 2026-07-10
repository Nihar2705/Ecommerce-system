package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.dto.product.ProductRequest;
import com.ecommerce.inventory.dto.product.ProductResponse;
import com.ecommerce.inventory.service.ProductImageData;
import com.ecommerce.inventory.service.ProductService;
import com.ecommerce.inventory.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    // Client-facing sort keys -> actual JPA property path. "category" maps to the
    // nested Category.name field so products can be sorted by category name too.
    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "name", "name",
            "description", "description",
            "price", "price",
            "quantity", "quantity",
            "category", "category.name"
    );

    @Autowired
    private ProductService productService;

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ADMIN and USER
    // Modified in Version 4: adds sortBy/direction (defaults: id, asc) and optional
    // filters name / category / minPrice / maxPrice, all combinable with pagination.
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        Sort sort = PaginationUtils.buildSort(sortBy, direction, ALLOWED_SORT_FIELDS, "id");
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.getAllProducts(pageable, name, category, minPrice, maxPrice));
    }

    // ADMIN and USER
    // New in Version 5: dedicated search endpoint. Exactly one search mode must be used
    // per call - keyword, category, or price range (minPrice/maxPrice) - not a combination.
    // Note: Spring matches the literal "/search" segment before the "/{id}" path variable,
    // so this does not conflict with getProductById below regardless of declaration order.
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<ProductResponse>> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        Sort sort = PaginationUtils.buildSort(sortBy, direction, ALLOWED_SORT_FIELDS, "id");
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.searchProducts(pageable, keyword, category, minPrice, maxPrice));
    }

    // ADMIN and USER
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                           @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully with id: " + id);
    }

    // ---------- Added in Version 6: Product Image Upload ----------

    // ADMIN only - uploads a new image, or replaces the existing one (old file is deleted)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> uploadProductImage(@PathVariable Long id,
                                                               @RequestParam("file") MultipartFile file) {
        ProductResponse response = productService.uploadProductImage(id, file);
        return ResponseEntity.ok(response);
    }

    // ADMIN and USER - streams the raw image bytes with the correct Content-Type
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        ProductImageData imageData = productService.getProductImage(id);
        MediaType mediaType = MediaType.parseMediaType(imageData.getContentType());
        return ResponseEntity.ok().contentType(mediaType).body(imageData.getData());
    }

    // ADMIN only - deletes the image from disk and clears the reference on the product
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/image")
    public ResponseEntity<String> deleteProductImage(@PathVariable Long id) {
        productService.deleteProductImage(id);
        return ResponseEntity.ok("Product image deleted successfully for product id: " + id);
    }
}
