package com.example.demo.dto;

import com.example.demo.model.ItemType;
import lombok.Data;

@Data
public class AiExtractedItemDto {
    private ItemType type;
    private String content;
}
