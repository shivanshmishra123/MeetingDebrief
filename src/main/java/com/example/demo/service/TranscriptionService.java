package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/*
 * LEARNING POINT — How to forward a file upload from Java to Python:
 *
 * When you upload a file to Spring Boot, it arrives as MultipartFile.
 * To forward it to Python, we can't just send JSON — we need to send
 * another multipart/form-data request.
 *
 * The process:
 *   1. Read the file bytes from MultipartFile into memory
 *   2. Wrap them in a ByteArrayResource (Spring's way to treat bytes as a file)
 *   3. Build a MultiValueMap with the field name "audio" (matching FastAPI's parameter)
 *   4. Set Content-Type to multipart/form-data
 *   5. POST it to Python and read the response
 */
@Service
public class TranscriptionService {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    @Autowired
    public TranscriptionService(RestTemplate restTemplate, @org.springframework.beans.factory.annotation.Value("${ai.service.url}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        String transcriptionUrl = aiServiceUrl + "/api/transcribe";

        // STEP 1: Read all bytes from the uploaded file into memory.
        // MultipartFile gives us the raw bytes — we need to wrap them
        // so RestTemplate can send them as a file part.
        byte[] audioBytes = audioFile.getBytes();

        // STEP 2: ByteArrayResource is Spring's way to say
        // "treat these bytes as a named resource (file)".
        // We override getFilename() so the HTTP part has the correct name.
        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                // The filename seen by FastAPI — preserves original name
                return audioFile.getOriginalFilename();
            }
        };

        // STEP 3: Build the multipart body
        // MultiValueMap<String, Object> is Spring's structure for
        // multipart/form-data — keys are field names, values are the data.
        // FastAPI expects the field name "audio" (matches: audio: UploadFile = File(...))
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", audioResource);

        // STEP 4: Set headers to tell the server this is a multipart upload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // STEP 5: Send to FastAPI and parse the response
        // FastAPI returns: { "transcript": "...", "duration_seconds": 120, "language": "en" }
        ResponseEntity<Map> response = restTemplate.exchange(
                transcriptionUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Python transcription service returned an error");
        }

        // Extract just the transcript text from the response map
        Object transcript = response.getBody().get("transcript");
        if (transcript == null || transcript.toString().isBlank()) {
            throw new RuntimeException("Transcription returned empty text");
        }

        return transcript.toString();
    }
}
