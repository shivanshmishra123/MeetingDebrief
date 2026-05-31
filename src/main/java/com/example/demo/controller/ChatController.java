package com.example.demo.controller;
import com.example.demo.dto.QueryRequestDTO;
import com.example.demo.dto.QueryResponseDTO;
import com.example.demo.service.AiIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiIntegrationService aiIntegrationService;

    @Autowired
    public ChatController(AiIntegrationService aiIntegrationService) {
        this.aiIntegrationService = aiIntegrationService;
    }

    @PostMapping("/ask")
    public ResponseEntity<QueryResponseDTO> askQuestion(@RequestBody QueryRequestDTO request) {
        try {
            // Send the question to Python and get the LLM's answer back
            QueryResponseDTO response = aiIntegrationService.askMeetingQuestion(
                    request.getQuestion(),
                    request.getMeetingId()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Fallback response if Python is unreachable or crashes
            QueryResponseDTO errorResponse = new QueryResponseDTO();
            errorResponse.setAnswer("Sorry, I'm having trouble reaching my Second Brain right now. " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}