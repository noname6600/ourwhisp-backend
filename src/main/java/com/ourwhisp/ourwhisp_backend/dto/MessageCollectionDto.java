package com.ourwhisp.ourwhisp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageCollectionDto {
    private String sessionUUID;
    private List<MessageRequestDto> messages;
}
