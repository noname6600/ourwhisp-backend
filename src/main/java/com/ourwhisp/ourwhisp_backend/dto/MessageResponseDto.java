package com.ourwhisp.ourwhisp_backend.dto;

import com.ourwhisp.ourwhisp_backend.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDto {
    private String id;
    private String content;
    private Instant createdAt;
    private Long view;

    public static MessageResponseDto fromEntity(Message message) {
        return new MessageResponseDto(
                message.getId(),
                message.getContent(),
                message.getCreatAt(),
                message.getView()
        );
    }
}
