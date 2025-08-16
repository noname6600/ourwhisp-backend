package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.ApiResponse;
import com.ourwhisp.ourwhisp_backend.dto.MessageDto;
import com.ourwhisp.ourwhisp_backend.model.Collection;
import com.ourwhisp.ourwhisp_backend.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Collection", description = "User personal collection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collection")
public class CollectionController {

    private final CollectionService collectionService;

    @Operation(summary = "Get user's collection")
    @GetMapping
    public ResponseEntity<ApiResponse<Collection>> getCollection(@RequestParam String sessionUUID) {
        Collection c = collectionService.getCollection(sessionUUID);
        return ResponseEntity.ok(ApiResponse.success(c));
    }

    @Operation(summary = "Add message to collection")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Collection>> addMessage(
            @RequestParam String sessionUUID,
            @RequestParam String messageId) {
        Collection c = collectionService.addMessageToCollection(sessionUUID, messageId);
        return ResponseEntity.ok(ApiResponse.success(c, "Message added to collection"));
    }

    @Operation(summary = "Remove message from collection")
    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<Collection>> removeMessage(
            @RequestParam String sessionUUID,
            @RequestParam String messageId) {
        Collection c = collectionService.removeMessageFromCollection(sessionUUID, messageId);
        return ResponseEntity.ok(ApiResponse.success(c, "Message removed from collection"));
    }

    @Operation(summary = "Get all messages with content in user's collection")
    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessagesFromCollection(
            @RequestParam String sessionUUID) {
        List<MessageDto> dtos = collectionService.getMessagesFromCollection(sessionUUID);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
