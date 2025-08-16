package com.ourwhisp.ourwhisp_backend.dto;


import com.ourwhisp.ourwhisp_backend.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String content;

    public static MessageDto fromEntity(Message message) {
        MessageDto dto = new MessageDto();
        dto.setContent(message.getContent());
        return dto;
    }

    public Message toEntity() {
        Message msg = new Message();
        msg.setContent(content);
        return msg;
    }
}
