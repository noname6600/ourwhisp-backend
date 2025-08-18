package com.ourwhisp.ourwhisp_backend.repository;

import com.ourwhisp.ourwhisp_backend.model.MessageCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CollectionRepository extends MongoRepository<MessageCollection, String> {
    Optional<MessageCollection> findBySessionUUID(String sessionUUID);
}
