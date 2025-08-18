package com.ourwhisp.ourwhisp_backend.dto;


import com.ourwhisp.ourwhisp_backend.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDto {
    private String content;

    public static MessageRequestDto fromEntity(Message message) {
        MessageRequestDto dto = new MessageRequestDto();
        dto.setContent(message.getContent());
        return dto;
    }

    public Message toEntity() {
        Message msg = new Message();
        msg.setContent(content);
        return msg;
    }
}
