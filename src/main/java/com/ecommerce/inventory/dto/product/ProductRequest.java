package com.ecommerce.inventory.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound representation of a Product, used for create/update requests.
 * categoryId references an existing Category by id.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private Long categoryId;
}
