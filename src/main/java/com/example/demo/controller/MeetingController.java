package com.example.demo.controller;

import com.example.demo.dto.MeetingUploadRequest;
import com.example.demo.model.ExtractedItem;
import com.example.demo.model.Meeting;
import com.example.demo.model.User;
import com.example.demo.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for meeting CRUD operations.
 *
 * All endpoints are user-scoped — the authenticated principal is extracted
 * from the SecurityContext via @AuthenticationPrincipal and passed down
 * to the service layer to enforce data isolation.
 */
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * Upload a meeting with a raw text transcript (no audio).
     * @AuthenticationPrincipal injects the User object that JwtAuthFilter
     * placed in the SecurityContext.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadMeeting(
            @RequestBody MeetingUploadRequest request,
            @AuthenticationPrincipal User currentUser) {

        Meeting savedMeeting = meetingService.processAndSaveMeeting(request, currentUser);
        return ResponseEntity.status(201).body("Meeting processed successfully. ID: " + savedMeeting.getId());
    }

    /**
     * Returns only meetings belonging to the currently authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<Meeting>> getAllMeetings(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(meetingService.getMeetingsForUser(currentUser.getId()));
    }

    /**
     * Returns a single meeting — validates ownership before returning.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getMeetingById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(meetingService.getMeetingByIdForUser(id, currentUser.getId()));
    }

    @GetMapping("/{id}/extracted-items")
    public ResponseEntity<List<ExtractedItem>> getExtractedItemsByMeetingId(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User currentUser) {

        // Verify the meeting belongs to this user before exposing extracted items
        meetingService.getMeetingByIdForUser(id, currentUser.getId());
        return ResponseEntity.ok(meetingService.getExtractedItemsByMeetingId(id));
    }
}