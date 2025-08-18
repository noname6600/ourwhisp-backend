package com.ourwhisp.ourwhisp_backend.dto;

import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchResultDto {
    private List<MessageResponseDto> messages;
    private int page;
    private int size;
    private int totalPages;
    private long totalMessages;

    public static MessageSearchResultDto fromEntity(MessageSearchResult result) {
        List<MessageResponseDto> dtos = result.getMessages().stream()
                .map(MessageResponseDto::fromEntity)
                .collect(Collectors.toList());

        return new MessageSearchResultDto(
                dtos,
                result.getPage(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalMessages()
        );
    }
}

