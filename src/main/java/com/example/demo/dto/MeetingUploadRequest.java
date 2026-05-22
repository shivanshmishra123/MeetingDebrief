package com.example.demo.dto;

import lombok.Data;

@Data
public class MeetingUploadRequest {
    private String title;
    private String transcript;
}