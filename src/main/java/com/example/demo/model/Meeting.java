package com.example.demo.model;
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
}