package com.ourwhisp.ourwhisp_backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "collections")
public class Collection {

    @Id
    private String id;

    private String sessionUUID;

    private Set<String> messageIds = new HashSet<>();
}
