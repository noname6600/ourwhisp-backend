package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.*;
import com.ourwhisp.ourwhisp_backend.exception.ResourceNotFoundException;
import com.ourwhisp.ourwhisp_backend.model.MessageCollection;
import com.ourwhisp.ourwhisp_backend.service.ICollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Collection", description = "User personal collection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collection")
public class CollectionController {

    private final ICollectionService collectionService;

    @Operation(summary = "Get user's collection")
    @GetMapping("/{sessionUUID}")
    public ResponseEntity<ApiResponse<MessageCollectionDto>> getCollection(@PathVariable String sessionUUID) {
        MessageCollection collection = collectionService.getCollection(sessionUUID);
        List<MessageRequestDto> messages = collection.getMessageIds().stream()
                .map(id -> MessageRequestDto.fromEntity(collectionService.getMessagesFromCollection(sessionUUID).stream()
                        .filter(m -> m.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Message not found"))))
                .collect(Collectors.toList());

        MessageCollectionDto dto = new MessageCollectionDto(collection.getSessionUUID(), messages);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @Operation(summary = "Add message to collection")
    @PostMapping("/{sessionUUID}/messages")
    public ResponseEntity<ApiResponse<MessageCollectionDto>> addMessage(
            @PathVariable String sessionUUID,
            @RequestParam String messageId) {

        collectionService.addMessageToCollection(sessionUUID, messageId);

        return getCollection(sessionUUID);
    }

    @Operation(summary = "Remove message from collection")
    @DeleteMapping("/{sessionUUID}/messages/{messageId}")
    public ResponseEntity<ApiResponse<MessageCollectionDto>> removeMessage(
            @PathVariable String sessionUUID,
            @PathVariable String messageId) {

        collectionService.removeMessageFromCollection(sessionUUID, messageId);

        return getCollection(sessionUUID);
    }

    @Operation(summary = "Remove a collection")
    @DeleteMapping("/{sessionUUID}")
    public ResponseEntity<ApiResponse<Void>> clearCollection(@PathVariable String sessionUUID) {
        collectionService.clearCollection(sessionUUID);
        return ResponseEntity.ok(ApiResponse.success(null, "Collection cleared"));
    }
}
