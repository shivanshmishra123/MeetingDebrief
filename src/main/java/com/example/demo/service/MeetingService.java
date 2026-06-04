package com.example.demo.service;

import com.example.demo.dto.AiExtractionResponseDto;
import com.example.demo.dto.MeetingUploadRequest;
import com.example.demo.model.ExtractedItem;
import com.example.demo.model.ItemType;
import com.example.demo.model.Meeting;
import com.example.demo.model.User;
import com.example.demo.repository.ExtractedItemRepository;
import com.example.demo.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ExtractedItemRepository extractedItemRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * Full processing pipeline: save → structured extract → vectorize.
     * The caller must supply the authenticated User so the meeting is
     * associated with that user's account (user-scoped isolation).
     */
    @Transactional
    public Meeting processAndSaveMeeting(MeetingUploadRequest request, User currentUser) {

        System.out.println("========== INITIATING MEETING PROCESSING PIPELINE ==========");

        // 1. Save the raw meeting metadata first, linked to the current user
        Meeting meeting = new Meeting();
        meeting.setTitle(request.title);
        meeting.setTranscriptRaw(request.transcript);
        meeting.setMeetingDate(LocalDateTime.now());
        meeting.setUser(currentUser);                        // ← user-scoped FK
        Meeting savedMeeting = meetingRepository.save(meeting);

        System.out.println("✅ Meeting saved to PostgreSQL with ID: " + savedMeeting.getId()
                + " for user: " + currentUser.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // ── STEP A: Structured Extraction (Decisions, Action Items, etc.) ──
            System.out.println("========== STEP A: GEMINI STRUCTURED EXTRACTION ==========");

            Map<String, String> structuredPayload = new HashMap<>();
            structuredPayload.put("transcript", request.transcript);
            String structuredJson = objectMapper.writeValueAsString(structuredPayload);

            HttpEntity<String> structuredEntity = new HttpEntity<>(structuredJson, headers);
            String extractStructuredUrl = "http://localhost:8000/api/extract-structured";

            ResponseEntity<AiExtractionResponseDto> structuredResponse = restTemplate.postForEntity(
                    extractStructuredUrl,
                    structuredEntity,
                    AiExtractionResponseDto.class
            );

            AiExtractionResponseDto aiResponse = structuredResponse.getBody();

            System.out.println("========== RECEIVED STRUCTURED DATA FROM GEMINI ==========");

            // Persist structured extracted items
            if (aiResponse != null && aiResponse.getItems() != null) {
                List<ExtractedItem> itemsToSave = aiResponse.getItems().stream().map(dto -> {
                    ExtractedItem item = new ExtractedItem();
                    item.setMeeting(savedMeeting);
                    try {
                        item.setType(ItemType.valueOf(dto.getType()));
                    } catch (IllegalArgumentException e) {
                        item.setType(ItemType.KEY_CONTEXT); // fallback for unknown types
                    }
                    item.setContent(dto.getContent());
                    return item;
                }).collect(Collectors.toList());

                extractedItemRepository.saveAll(itemsToSave);
                System.out.println("✅ Saved " + itemsToSave.size() + " structured items to PostgreSQL.");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Structured extraction failed: " + e.getMessage());
            // Non-fatal: we still want to attempt vectorization below
        }

        try {
            // ── STEP B: Vector Embedding into Pinecone (for RAG chatbot) ──
            System.out.println("========== STEP B: PINECONE VECTOR EMBEDDING ==========");

            Map<String, String> vectorPayload = new HashMap<>();
            vectorPayload.put("meeting_id", savedMeeting.getId().toString());
            vectorPayload.put("title", request.title);
            LocalDateTime meetingDate = savedMeeting.getMeetingDate() != null ? savedMeeting.getMeetingDate() : LocalDateTime.now();
            vectorPayload.put("date", meetingDate.toLocalDate().toString());
            vectorPayload.put("transcript", request.transcript);
            String vectorJson = objectMapper.writeValueAsString(vectorPayload);

            HttpEntity<String> vectorEntity = new HttpEntity<>(vectorJson, headers);
            String extractVectorUrl = "http://localhost:8000/api/extract";

            restTemplate.postForEntity(extractVectorUrl, vectorEntity, String.class);
            System.out.println("✅ Transcript chunks vectorized and pushed to Pinecone.");

        } catch (Exception e) {
            System.err.println("⚠️ Pinecone vectorization failed: " + e.getMessage());
            throw new RuntimeException("Pinecone vectorization failed", e);
        }

        return savedMeeting;
    }

    /**
     * Returns ONLY meetings belonging to the current user.
     */
    public List<Meeting> getMeetingsForUser(UUID userId) {
        return meetingRepository.findByUserId(userId);
    }

    /**
     * Fetches a single meeting, verifying it belongs to the requesting user.
     * Throws RuntimeException (→ 404) if not found OR belongs to another user.
     */
    public Meeting getMeetingByIdForUser(UUID meetingId, UUID userId) {
        return meetingRepository.findByIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new RuntimeException("Meeting not found or access denied."));
    }

    public List<ExtractedItem> getExtractedItemsByMeetingId(UUID meetingId) {
        return extractedItemRepository.findByMeetingId(meetingId);
    }
}