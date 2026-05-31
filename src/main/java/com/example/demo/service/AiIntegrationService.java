package com.example.demo.service;

import com.example.demo.dto.AiExtractedItemDto;
import com.example.demo.dto.AiExtractionResponseDto;
import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.dto.QueryResponseDTO;
import com.example.demo.dto.TranscriptRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class AiIntegrationService {

    private final RestTemplate restTemplate;

    // Split into specific endpoints
    private final String AI_QUERY_URL = "http://localhost:8000/api/query";
    private final String AI_EXTRACT_URL = "http://localhost:8000/api/extract-structured";

    @Autowired
    public AiIntegrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Queries the Second Brain for an answer.
     * Pass null for meetingId to search across all meetings.
     */
    public QueryResponseDTO askMeetingQuestion(String question, String meetingId) {
        QueryRequestDTO requestPayload = new QueryRequestDTO(question, meetingId);

        // Fires the POST request and automatically maps the JSON back to our DTO
        return restTemplate.postForObject(AI_QUERY_URL, requestPayload, QueryResponseDTO.class);
    }

    /**
     * Calls the Python AI Gateway to extract structured data (Decisions, Action Items, etc.)
     */
    public List<AiExtractedItemDto> extractStructuredData(String transcript) {
        TranscriptRequestDto requestPayload = new TranscriptRequestDto(transcript);

        AiExtractionResponseDto response = restTemplate.postForObject(
                AI_EXTRACT_URL,
                requestPayload,
                AiExtractionResponseDto.class
        );

        // Null-safety check before returning the list
        return response != null && response.getItems() != null
                ? response.getItems()
                : Collections.emptyList();
    }
}