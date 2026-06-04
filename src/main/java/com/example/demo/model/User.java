package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the `users` table.
 *
 * Implements Spring Security's UserDetails so this object can be used
 * directly in the security filter chain without any adapter layer.
 *
 * COLUMN NOTES:
 *  - role/designation: free-text company designation e.g. "Engineering Manager"
 *    NOT a system role enum — any team title the user types at signup.
 *  - password_hash: BCrypt hash, never the raw password.
 *  - isActive: soft-disable flag; false = account blocked from login.
 */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt-hashed password. Stored in column "password_hash".
     * The field is named passwordHash here; Spring Security reads it
     * via getPassword() which we override below.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Company designation / job title, e.g. "Product Manager", "CTO".
     * This is NOT a system-level access role — it's purely informational.
     */
    @Column(name = "designation", length = 150)
    private String designation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // =========================================================
    // Spring Security — UserDetails interface implementation
    // =========================================================

    /**
     * Returns the BCrypt hash as the "password" that Spring Security
     * will compare against using PasswordEncoder.matches().
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Spring Security identifies users by a unique string — we use email.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * All users get the ROLE_USER authority.
     * Admins can be given ROLE_ADMIN in a future iteration.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return isActive; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }
}
