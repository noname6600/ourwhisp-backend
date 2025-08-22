package com.ourwhisp.ourwhisp_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "message")
public class Message {
    @Id
    private String id = UUID.randomUUID().toString();
    private String content;
    private Instant creatAt;
    private Long view;
}
