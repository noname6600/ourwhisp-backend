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
    private String id;
    private String content;
    private Instant creatAt;
    private Long view;

    public static MessageDto fromEntity(Message message){
        return new MessageDto(message.getId(), message.getContent(),message.getCreatAt(),message.getView());
    }

    public Message toEntity(){
        return new Message(id,content,creatAt,view);
    }
}
