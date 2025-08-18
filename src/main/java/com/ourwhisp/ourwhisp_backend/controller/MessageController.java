package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.*;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;
import com.ourwhisp.ourwhisp_backend.service.IMessageService;
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

    private final IMessageService messageService;


    @Operation(summary = "Get random 10 messages for user session")
    @GetMapping("/random")
    public ResponseEntity<ApiResponse<List<MessageResponseDto>>> getRandomMessagesForSession(
            @RequestParam String sessionUUID,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<MessageResponseDto> dtos = messageService.getRandomMessagesForSession(sessionUUID, limit)
                .stream()
                .map(MessageResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @Operation(summary = "Mark message as read for a session")
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<MessageResponseDto>> markAsRead(
            @RequestParam String sessionUUID,
            @PathVariable String id
    ) {
        Message updated = messageService.markAsRead(sessionUUID, id);
        return ResponseEntity.ok(ApiResponse.success(MessageResponseDto.fromEntity(updated), "Message marked as read"));
    }


    @Operation(summary = "Find a message by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponseDto>> getMessageById(@PathVariable String id) {
        Message msg = messageService.getMessageById(id);
        return ResponseEntity.ok(ApiResponse.success(MessageResponseDto.fromEntity(msg)));
    }

    @Operation(summary = "Create a new message ")
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponseDto>> createMessage(@RequestBody MessageRequestDto dto) {
        Message created = messageService.createMessage(dto.toEntity());
        return ResponseEntity.ok(ApiResponse.success(MessageResponseDto.fromEntity(created), "Message created"));
    }



    @Operation(summary = "Search messages")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<MessageSearchResultDto>> smartSearch(
            @RequestParam String sessionUUID,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String length,
            @RequestParam(required = false) Long minViews,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MessageSearchFilterDto filter = new MessageSearchFilterDto();
        filter.setKeyword(keyword);
        filter.setLength(length);
        filter.setMinViews(minViews);
        filter.setPage(page);
        filter.setSize(size);

        MessageSearchResult result = messageService.searchMessages(sessionUUID, filter);
        return ResponseEntity.ok(ApiResponse.success(MessageSearchResultDto.fromEntity(result)));
    }

}
