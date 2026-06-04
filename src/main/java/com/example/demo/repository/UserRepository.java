package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Used by AuthService to look up users by email at login time.
     * Returns Optional so we can elegantly handle "user not found" cases.
     */
    Optional<User> findByEmail(String email);

    /**
     * Quick existence check at registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);
}
