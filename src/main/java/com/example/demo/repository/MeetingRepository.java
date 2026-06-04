package com.example.demo.repository;

import com.example.demo.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    /**
     * Fetch all meetings belonging to a specific user.
     * Used by MeetingService to enforce user-scoped isolation —
     * users only see meetings they uploaded.
     */
    List<Meeting> findByUserId(UUID userId);

    /**
     * Safe getter that also checks ownership — prevents one user
     * from accessing another user's meeting by guessing the UUID.
     */
    Optional<Meeting> findByIdAndUserId(UUID id, UUID userId);
}