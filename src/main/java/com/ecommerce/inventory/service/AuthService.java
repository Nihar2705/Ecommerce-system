package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.auth.AuthResponse;
import com.ecommerce.inventory.dto.auth.LoginRequest;
import com.ecommerce.inventory.dto.auth.RefreshTokenRequest;
import com.ecommerce.inventory.dto.auth.RegisterRequest;
import com.ecommerce.inventory.dto.user.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}
