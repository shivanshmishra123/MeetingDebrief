package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiExtractionResponseDto {
    private List<AiExtractedItemDto> items;
}