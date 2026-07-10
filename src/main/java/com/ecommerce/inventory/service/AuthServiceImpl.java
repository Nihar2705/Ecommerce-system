package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.auth.AuthResponse;
import com.ecommerce.inventory.dto.auth.LoginRequest;
import com.ecommerce.inventory.dto.auth.RefreshTokenRequest;
import com.ecommerce.inventory.dto.auth.RegisterRequest;
import com.ecommerce.inventory.dto.user.UserResponse;
import com.ecommerce.inventory.entity.RefreshToken;
import com.ecommerce.inventory.entity.Role;
import com.ecommerce.inventory.entity.RoleName;
import com.ecommerce.inventory.entity.User;
import com.ecommerce.inventory.exception.InvalidRefreshTokenException;
import com.ecommerce.inventory.exception.RefreshTokenExpiredException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.exception.UserAlreadyExistsException;
import com.ecommerce.inventory.repository.RefreshTokenRepository;
import com.ecommerce.inventory.repository.RoleRepository;
import com.ecommerce.inventory.repository.UserRepository;
import com.ecommerce.inventory.security.CustomUserDetails;
import com.ecommerce.inventory.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered: " + request.getEmail());
        }

        // New self-registrations always get the USER role.
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: USER"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().getName().name());
        String refreshTokenValue = createOrReplaceRefreshToken(user);

        return new AuthResponse(accessToken, refreshTokenValue, toUserResponse(user));
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException("Refresh token has expired, please log in again");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole().getName().name());

        // The same refresh token remains valid until it expires.
        return new AuthResponse(newAccessToken, refreshToken.getToken(), toUserResponse(user));
    }

    private String createOrReplaceRefreshToken(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(jwtUtil.generateRefreshTokenValue());
        newRefreshToken.setUser(user);
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpirationMs()));

        return refreshTokenRepository.save(newRefreshToken).getToken();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName().name()
        );
    }
}
