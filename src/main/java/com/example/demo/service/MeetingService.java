package com.example.demo.service;

import com.example.demo.dto.MeetingUploadRequest;
import com.example.demo.model.Meeting;
import com.example.demo.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public Meeting saveRawMeeting(MeetingUploadRequest request) {
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setTranscriptRaw(request.getTranscript());

        return meetingRepository.save(meeting);
    }
}