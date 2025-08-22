package com.ourwhisp.ourwhisp_backend.factory;

import com.ourwhisp.ourwhisp_backend.dto.MessageRequestDto;
import com.ourwhisp.ourwhisp_backend.dto.MessageResponseDto;
import com.ourwhisp.ourwhisp_backend.model.Message;

import com.ourwhisp.ourwhisp_backend.repository.MessageRepository;
import com.ourwhisp.ourwhisp_backend.factory.base.BasePersistDataFactory;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MessageFactory extends BasePersistDataFactory<
        UUID, MessageRequestDto, MessageResponseDto, UUID, Message, MessageRepository>
        implements IMessageFactory {

    protected MessageFactory(MessageRepository repository) {
        super(repository);
    }

    @Override
    public MessageResponseDto toDetail(Message message) {
        return MessageResponseDto
                .builder()
                .id(message.getId())
                .view(message.getView())
                .content(message.getContent())
                .createdAt(message.getCreatAt())
                .build();
    }

    @Override
    public MessageRequestDto toInfo(Message message) {
        return MessageRequestDto
                .builder()
                .content(message.getContent())
                .build();
    }

    @Override
    public Message toEntity(MessageResponseDto messageResponseDto) {
        return Message
                .builder()
                .id(messageResponseDto.getId())
                .content(messageResponseDto.getContent())
                .creatAt(messageResponseDto.getCreatedAt())
                .view(messageResponseDto.getView())
                .build();
    }


    @Override
    public MessageRequestDto toInfoDto(MessageResponseDto detail) {
        return null;
    }
}
