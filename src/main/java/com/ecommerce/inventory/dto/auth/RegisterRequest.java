package com.ecommerce.inventory.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/auth/register.
 * New accounts are always created with the USER role; ADMIN accounts are
 * provisioned separately (see DataInitializer) to prevent self-promotion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
}
