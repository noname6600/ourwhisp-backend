package com.ourwhisp.ourwhisp_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchResult {
    private List<Message> messages;
    private int page;
    private int size;
    private int totalPages;
    private long totalMessages;
}
