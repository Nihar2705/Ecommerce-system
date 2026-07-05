package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Added in Version 2. Represents an application user stored in the "users" table.
 * Each user has exactly one role. Passwords are always stored BCrypt-encoded.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // A user can have exactly one role
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
