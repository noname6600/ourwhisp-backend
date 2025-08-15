package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.ApiResponse;
import com.ourwhisp.ourwhisp_backend.dto.MessageDto;
import com.ourwhisp.ourwhisp_backend.exception.ResourceNotFoundException;
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
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;
    @Operation(summary = "Get all messages")
    @GetMapping("/all")
    //wont use cause ofc it gonna break the server too
    public ResponseEntity<ApiResponse<List<MessageDto>>> getAllMessages() {
        List<MessageDto> dtos = messageService.getAllMessages()
                .stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @Operation(summary = "Get random messages. Default 10")
    @GetMapping
    //temp api for testing
    public ResponseEntity<ApiResponse<List<MessageDto>>> getRandomMessages(
            @RequestParam(defaultValue = "10") int limit) {
        List<MessageDto> dtos = messageService.getRandomMessages(limit)
                .stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @Operation(summary = "Increase view of msg")
    @PostMapping("/{id}/view")
    //update view
    public ResponseEntity<ApiResponse<MessageDto>> incrementView(@PathVariable String id) {
        Message updated = messageService.incrementView(id);
        return ResponseEntity.ok(ApiResponse.success(MessageDto.fromEntity(updated), "View incremented"));
    }

    @Operation(summary = "Find msg by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageDto>> getMessageById(@PathVariable String id) {
        Message msg = messageService.getMessageById(id);
        return ResponseEntity.ok(ApiResponse.success(MessageDto.fromEntity(msg)));
    }

    @Operation(summary = "Send msg")
    @PostMapping
    public ResponseEntity<ApiResponse<MessageDto>> createMessage(@RequestBody MessageDto dto) {
        Message created = messageService.createMessage(dto.toEntity());
        return ResponseEntity.ok(ApiResponse.success(MessageDto.fromEntity(created), "Message created"));
    }

    @Operation(summary = "Delete msg")
    @DeleteMapping("/{id}")
    //probably not use just write for just in case
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable String id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted"));
    }
}
