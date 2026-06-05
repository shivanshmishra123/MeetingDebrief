package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @CreationTimestamp
    @Column(name = "meeting_date", updatable = false)
    private LocalDateTime meetingDate;

    @Column(columnDefinition = "TEXT")
    private String transcriptRaw;

    /**
     * The user who created/uploaded this meeting.
     * FETCH = LAZY: we don't join-load the user on every meeting query.
     * JsonIgnore: prevents infinite recursion when serializing to JSON
     * (User → List<Meeting> → User → ...).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Meeting parent;

    public UUID getParentId() {
        return parent != null ? parent.getId() : null;
    }
}