package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiExtractionResponseDto {
    private List<AiExtractedItemDto> items;
    private List<String> drifted_ids;
}