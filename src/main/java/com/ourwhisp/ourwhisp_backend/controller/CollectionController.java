package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.*;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageCollection;
import com.ourwhisp.ourwhisp_backend.service.ICollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Collection", description = "User personal collection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collection")
public class CollectionController {

    private final ICollectionService collectionService;

    @Operation(summary = "Get user's collection")
    @GetMapping("/{sessionUUID}")
    public ResponseEntity<ApiResponse<PagedMessageCollectionDto>> getPageCollection(
            @PathVariable String sessionUUID,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = collectionService.getPageMessagesFromCollection(sessionUUID, keyword, pageable);

        List<MessageResponseDto> messages = messagePage.getContent().stream()
                .map(MessageResponseDto::fromEntity)
                .toList();

        PagedMessageCollectionDto dto = new PagedMessageCollectionDto(
                messages,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalPages(),
                messagePage.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    private MessageCollectionDto getCollection(String sessionUUID) {
        MessageCollection collection = collectionService.getCollection(sessionUUID);
        List<MessageResponseDto> messages = collectionService.getMessagesFromCollection(sessionUUID).stream()
                .map(MessageResponseDto::fromEntity)
                .toList();

        return new MessageCollectionDto(collection.getSessionUUID(), messages);
    }

    @Operation(summary = "Add message to collection")
    @PostMapping("/{sessionUUID}/messages")
    public ResponseEntity<ApiResponse<MessageCollectionDto>> addMessage(
            @PathVariable String sessionUUID,
            @RequestParam String messageId) {

        collectionService.addMessageToCollection(sessionUUID, messageId);

        return ResponseEntity.ok(ApiResponse.success(getCollection(sessionUUID)));
    }

    @Operation(summary = "Remove message from collection")
    @DeleteMapping("/{sessionUUID}/messages/{messageId}")
    public ResponseEntity<ApiResponse<MessageCollectionDto>> removeMessage(
            @PathVariable String sessionUUID,
            @PathVariable String messageId) {

        collectionService.removeMessageFromCollection(sessionUUID, messageId);

        return ResponseEntity.ok(ApiResponse.success(getCollection(sessionUUID)));
    }

    @Operation(summary = "Remove a collection")
    @DeleteMapping("/{sessionUUID}")
    public ResponseEntity<ApiResponse<Void>> clearCollection(@PathVariable String sessionUUID) {
        collectionService.clearCollection(sessionUUID);
        return ResponseEntity.ok(ApiResponse.success(null, "Collection cleared"));
    }
}
