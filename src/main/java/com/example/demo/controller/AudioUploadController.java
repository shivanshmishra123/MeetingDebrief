package com.example.demo.controller;

import com.example.demo.dto.MeetingUploadRequest;
import com.example.demo.model.Meeting;
import com.example.demo.model.User;
import com.example.demo.service.MeetingService;
import com.example.demo.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/*
 * LEARNING POINT — @RequestParam vs @RequestBody:
 *
 * Normally you use @RequestBody for JSON.
 * But for file uploads, the browser sends multipart/form-data — a special
 * HTTP encoding that can carry both binary file data AND text fields together.
 *
 * @RequestParam("audio") MultipartFile  → receives the binary file
 * @RequestParam("title") String         → receives the text field
 *
 * Both arrive in the same HTTP request, just in different "parts".
 */
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class AudioUploadController {

    private final TranscriptionService transcriptionService;
    private final MeetingService meetingService;

    @PostMapping("/upload-audio")
    public ResponseEntity<?> uploadAudio(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("title") String title,
            @AuthenticationPrincipal User currentUser) {   // ← injected from JWT

        // Validate that a file was actually sent
        if (audioFile.isEmpty()) {
            return ResponseEntity.badRequest().body("No audio file provided.");
        }

        System.out.println("🎙️ Audio upload received: " + audioFile.getOriginalFilename()
                + " (" + audioFile.getSize() / 1024 + " KB) for meeting: " + title
                + " by user: " + currentUser.getEmail());

        try {
            // STEP 1: Send the audio file to Python for transcription
            String transcript = transcriptionService.transcribeAudio(audioFile);
            System.out.println("✅ Got transcript back from Python. Length: " + transcript.length() + " chars");

            // STEP 2: Package the transcript into the upload request DTO
            MeetingUploadRequest request = new MeetingUploadRequest();
            request.title = title;
            request.transcript = transcript;

            // STEP 3: Run the full meeting processing pipeline, scoped to this user
            Meeting savedMeeting = meetingService.processAndSaveMeeting(request, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Audio transcribed and meeting processed successfully!",
                    "meetingId", savedMeeting.getId().toString(),
                    "transcriptPreview", transcript.substring(0, Math.min(300, transcript.length())) + "..."
            ));

        } catch (Exception e) {
            System.err.println("❌ Audio upload pipeline failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process audio: " + e.getMessage());
        }
    }
}
