package com.example.demo.dto;

import lombok.Data;

@Data
public class AiExtractedItemDto {
    private String type;  // Receives "DECISION", "ACTION_ITEM", etc. as plain string from FastAPI
    private String content;
}
