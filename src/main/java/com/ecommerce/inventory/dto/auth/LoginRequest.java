package com.ecommerce.inventory.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/auth/login.
 * usernameOrEmail lets the user log in with either their username or their email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    private String usernameOrEmail;
    private String password;
}
