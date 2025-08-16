package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.ApiResponse;
import com.ourwhisp.ourwhisp_backend.dto.MessageDto;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Message", description = "Operations about messages")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;


    @Operation(summary = "Get random 10 messages for user session")
    @GetMapping("/random")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getRandomMessagesForSession(
            @RequestParam String sessionUUID,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<MessageDto> dtos = messageService.getRandomMessagesForSession(sessionUUID, limit)
                .stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @Operation(summary = "Mark message as read for a session")
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<MessageDto>> markAsRead(
            @RequestParam String sessionUUID,
            @PathVariable String id
    ) {
        Message updated = messageService.markAsRead(sessionUUID, id);
        return ResponseEntity.ok(ApiResponse.success(MessageDto.fromEntity(updated), "Message marked as read"));
    }


    @Operation(summary = "Find a message by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageDto>> getMessageById(@PathVariable String id) {
        Message msg = messageService.getMessageById(id);
        return ResponseEntity.ok(ApiResponse.success(MessageDto.fromEntity(msg)));
    }

    @Operation(summary = "Create a new message ")
    @PostMapping
    public ResponseEntity<ApiResponse<MessageDto>> createMessage(@RequestBody MessageDto dto) {
        Message created = messageService.createMessage(dto.toEntity());
        return ResponseEntity.ok(ApiResponse.success(MessageDto.fromEntity(created), "Message created"));
    }

    @Operation(summary = "Delete a message ")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable String id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted"));
    }
}
