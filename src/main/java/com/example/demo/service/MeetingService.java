package com.example.demo.service;

import com.example.demo.dto.AiExtractionResponseDto;
import com.example.demo.dto.MeetingUploadRequest;
import com.example.demo.dto.TranscriptRequestDto;
import com.example.demo.model.ExtractedItem;
import com.example.demo.model.Meeting;
import com.example.demo.repository.ExtractedItemRepository;
import com.example.demo.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ExtractedItemRepository extractedItemRepository;
    private final ObjectMapper objectMapper;

    // The old reliable RestTemplate. It never fails.
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public Meeting processAndSaveMeeting(MeetingUploadRequest request) {

        System.out.println("========== SENDING TO PYTHON ==========");

        // 1. Save the raw meeting metadata
        Meeting meeting = new Meeting();
        meeting.setTitle(request.title);
        meeting.setTranscriptRaw(request.transcript);
        Meeting savedMeeting = meetingRepository.save(meeting);

        try {
            // 2. Force a pure JSON string manually
            Map<String, String> payload = new HashMap<>();
            payload.put("transcript", request.transcript);
            String rawJson = objectMapper.writeValueAsString(payload);

            System.out.println("Raw JSON leaving Java: " + rawJson);

            // 3. Explicitly build the HTTP Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 4. Wrap the JSON and Headers into a raw HTTP Entity
            HttpEntity<String> httpEntity = new HttpEntity<>(rawJson, headers);

            // 5. Fire the request using RestTemplate
            ResponseEntity<AiExtractionResponseDto> response = restTemplate.postForEntity(
                    "http://localhost:8000/api/extract",
                    httpEntity,
                    AiExtractionResponseDto.class
            );

            AiExtractionResponseDto aiResponse = response.getBody();

            System.out.println("========== RECEIVED FROM PYTHON ==========");

            // 6. Save the extracted items
            if (aiResponse != null && aiResponse.getItems() != null) {
                List<ExtractedItem> itemsToSave = aiResponse.getItems().stream().map(dto -> {
                    ExtractedItem item = new ExtractedItem();
                    item.setMeeting(savedMeeting);
                    item.setType(dto.getType());
                    item.setContent(dto.getContent());
                    return item;
                }).collect(Collectors.toList());

                extractedItemRepository.saveAll(itemsToSave);
            }

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: " + e.getMessage());
            throw new RuntimeException("AI Extraction failed", e);
        }

        return savedMeeting;
    }
}