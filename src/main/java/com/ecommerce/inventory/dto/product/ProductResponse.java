package com.ecommerce.inventory.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outbound representation of a Product returned by the API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private Long categoryId;
    private String categoryName;

    // Added in Version 6. imageName is the stored filename only (no file system path);
    // imageUrl is the API endpoint to fetch the actual image bytes. Both are null when
    // the product has no image.
    private String imageName;
    private String imageUrl;
}
