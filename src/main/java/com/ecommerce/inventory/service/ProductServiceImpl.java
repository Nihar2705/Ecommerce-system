package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.common.PaginatedResponse;
import com.ecommerce.inventory.dto.product.ProductRequest;
import com.ecommerce.inventory.dto.product.ProductResponse;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.repository.CategoryRepository;
import com.ecommerce.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
    public PaginatedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
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
        productRepository.delete(product);
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
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}
