package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.dto.product.ProductRequest;
import com.ecommerce.inventory.dto.product.ProductResponse;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.exception.ImageNotFoundException;
import com.ecommerce.inventory.exception.InvalidPriceRangeException;
import com.ecommerce.inventory.exception.InvalidSearchParameterException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.repository.CategoryRepository;
import com.ecommerce.inventory.repository.ProductRepository;
import com.ecommerce.inventory.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Category category = findCategoryEntityById(request.getCategoryId());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    @Override
    public PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable,
                                                               String name,
                                                               String category,
                                                               Double minPrice,
                                                               Double maxPrice) {

        Specification<Product> spec = ProductSpecification.withFilters(name, category, minPrice, maxPrice);

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        Page<ProductResponse> responsePage = productPage.map(this::toResponse);
        return PaginatedResponse.from(responsePage);
    }

    @Override
    public PaginatedResponse<ProductResponse> searchProducts(Pageable pageable,
                                                               String keyword,
                                                               String category,
                                                               Double minPrice,
                                                               Double maxPrice) {

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasPriceRange = minPrice != null || maxPrice != null;

        int criteriaGroupsSupplied = (hasKeyword ? 1 : 0) + (hasCategory ? 1 : 0) + (hasPriceRange ? 1 : 0);

        if (criteriaGroupsSupplied == 0) {
            throw new InvalidSearchParameterException(
                    "Provide exactly one search parameter: keyword, category, or minPrice/maxPrice");
        }
        if (criteriaGroupsSupplied > 1) {
            throw new InvalidSearchParameterException(
                    "Only one search criterion is supported per request: keyword, category, or price range - not a combination");
        }
        if (hasPriceRange && minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new InvalidPriceRangeException("minPrice cannot be greater than maxPrice");
        }

        Specification<Product> spec;
        if (hasKeyword) {
            spec = ProductSpecification.containsKeyword(keyword);
        } else if (hasCategory) {
            spec = ProductSpecification.hasCategory(category);
        } else {
            spec = Specification.where(ProductSpecification.hasMinPrice(minPrice))
                    .and(ProductSpecification.hasMaxPrice(maxPrice));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        Page<ProductResponse> responsePage = productPage.map(this::toResponse);
        return PaginatedResponse.from(responsePage);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = findProductEntityById(id);
        return toResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductEntityById(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        if (request.getCategoryId() != null) {
            Category category = findCategoryEntityById(request.getCategoryId());
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        return toResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = findProductEntityById(id);
        // Clean up any stored image before removing the product row itself.
        if (product.getImageName() != null && !product.getImageName().isBlank()) {
            fileStorageService.deleteFile(product.getImageName());
        }
        productRepository.delete(product);
    }

    // ---------- Added in Version 6: Product Image Upload ----------

    @Override
    public ProductResponse uploadProductImage(Long productId, MultipartFile file) {
        Product product = findProductEntityById(productId);

        // Replace: delete the old image file (if any) before storing the new one.
        if (product.getImageName() != null && !product.getImageName().isBlank()) {
            fileStorageService.deleteFile(product.getImageName());
        }

        String storedFilename = fileStorageService.storeFile(file);
        product.setImageName(storedFilename);

        Product updatedProduct = productRepository.save(product);
        return toResponse(updatedProduct);
    }

    @Override
    public ProductImageData getProductImage(Long productId) {
        Product product = findProductEntityById(productId);

        if (product.getImageName() == null || product.getImageName().isBlank()) {
            throw new ImageNotFoundException("Product with id " + productId + " has no image");
        }

        return fileStorageService.loadFile(product.getImageName());
    }

    @Override
    public void deleteProductImage(Long productId) {
        Product product = findProductEntityById(productId);

        if (product.getImageName() == null || product.getImageName().isBlank()) {
            throw new ImageNotFoundException("Product with id " + productId + " has no image to delete");
        }

        fileStorageService.deleteFile(product.getImageName());
        product.setImageName(null);
        productRepository.save(product);
    }

    private Product findProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Category findCategoryEntityById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private ProductResponse toResponse(Product product) {
        String imageName = product.getImageName();
        // Only expose the API endpoint to fetch the image, never the file system path.
        String imageUrl = (imageName != null && !imageName.isBlank())
                ? "/api/products/" + product.getId() + "/image"
                : null;

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                imageName,
                imageUrl
        );
    }
}
