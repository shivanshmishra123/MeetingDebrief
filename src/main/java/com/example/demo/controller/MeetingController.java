package com.example.demo.controller;

import com.example.demo.dto.MeetingUploadRequest;
import com.example.demo.model.Meeting;
import com.example.demo.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    // ... inside MeetingController ...
    @PostMapping("/upload")
    public ResponseEntity<String> uploadMeeting(@RequestBody MeetingUploadRequest request) {
        Meeting savedMeeting = meetingService.processAndSaveMeeting(request);

        return new ResponseEntity<>(
                "Meeting processed successfully. ID: " + savedMeeting.getId(),
                HttpStatus.CREATED
        );
    }
}