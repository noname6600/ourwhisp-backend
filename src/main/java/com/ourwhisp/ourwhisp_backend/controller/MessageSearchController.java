package com.ourwhisp.ourwhisp_backend.controller;

import com.ourwhisp.ourwhisp_backend.dto.ApiResponse;
import com.ourwhisp.ourwhisp_backend.dto.MessageSearchResultDto;
import com.ourwhisp.ourwhisp_backend.service.MessageSearchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/messages")
@RequiredArgsConstructor
public class MessageSearchController {

    private final MessageSearchService searchService;
    @Operation(summary = "Search message directly from DB")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<MessageSearchResultDto>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MessageSearchResultDto result = searchService.searchMessages(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Search results"));
    }
}


