package com.ourwhisp.ourwhisp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchResultDto {
    private List<MessageDto> messages;
    private int page;
    private int size;
    private int totalPages;
    private long totalMessages;
}
