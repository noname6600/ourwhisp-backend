package com.ourwhisp.ourwhisp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedMessageCollectionDto {
    private List<MessageResponseDto> messages;
    private int page;
    private int size;
    private int totalPages;
    private long totalMessages;
}

