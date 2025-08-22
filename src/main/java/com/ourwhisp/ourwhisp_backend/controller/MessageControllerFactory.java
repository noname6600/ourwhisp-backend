package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.IResponseFactory;
import com.ourwhisp.ourwhisp_backend.dto.MessageRequestDto;
import com.ourwhisp.ourwhisp_backend.dto.MessageResponseDto;
import com.ourwhisp.ourwhisp_backend.factory.IMessageFactory;
import com.ourwhisp.ourwhisp_backend.factory.base.IDataFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
public class MessageControllerFactory extends BaseController<UUID, MessageRequestDto, MessageResponseDto> {

    private final IMessageFactory messageFactory;

    protected MessageControllerFactory(IResponseFactory responseFactory,
                                       IDataFactory<UUID, MessageRequestDto, MessageResponseDto> dataFactory,
                                       IMessageFactory messageFactory) {
        super(responseFactory, dataFactory);
        this.messageFactory = messageFactory;
    }

    @Override
    protected MessageResponseDto save(MessageResponseDto dto) {
        return messageFactory.save(dto.getId(), dto, messageFactory);
    }

    @Override
    protected MessageResponseDto save(MessageResponseDto dto, UUID id) {
        return messageFactory.save(id, dto, messageFactory);
    }

    @Override
    protected Optional<MessageResponseDto> findById(UUID id) {
        return messageFactory.findById(id, messageFactory);
    }

    @Override
    protected List<MessageResponseDto> findAll() {
        return messageFactory.findAll(messageFactory);
    }

    @Override
    protected void deleteEntity(UUID id) {
        messageFactory.delete(id);
    }

    @PostMapping("/{id}/read")
    public ApiResponse<MessageResponseDto> markAsRead(@PathVariable UUID id,
                                                      @RequestParam String sessionUUID) {
        MessageResponseDto dto = messageFactory.markAsRead(sessionUUID, id);
        return responseFactory.success(dto, "Message marked as read");
    }
}
