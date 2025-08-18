package com.ourwhisp.ourwhisp_backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Document(collection = "collections")
public class MessageCollection {

    @Id
    private String id = UUID.randomUUID().toString();;

    private String sessionUUID;

    private Set<String> messageIds = new HashSet<>();
}
