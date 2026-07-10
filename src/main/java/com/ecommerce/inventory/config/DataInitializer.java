package com.ecommerce.inventory.config;

import com.ecommerce.inventory.entity.Role;
import com.ecommerce.inventory.entity.RoleName;
import com.ecommerce.inventory.entity.User;
import com.ecommerce.inventory.repository.RoleRepository;
import com.ecommerce.inventory.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once on startup to make sure the ADMIN/USER roles exist and that at least one
 * ADMIN account is available. Self-registration always assigns the USER role
 * (see AuthServiceImpl.register), so an ADMIN account must be provisioned this way.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.USER)));

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(adminRole);
            userRepository.save(admin);
        }
    }
}
