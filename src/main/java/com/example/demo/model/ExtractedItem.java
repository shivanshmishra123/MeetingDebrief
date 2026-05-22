package com.example.demo.model;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "extracted_items")
public class ExtractedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_drifted")
    private boolean isDrifted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "superseded_by_id")
    private ExtractedItem supersededBy;
}