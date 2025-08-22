package com.ourwhisp.ourwhisp_backend.dto;

import com.ourwhisp.ourwhisp_backend.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class MessageResponseDto {
    private String id;
    private String content;
    private Instant createdAt;
    private Long view;

}
