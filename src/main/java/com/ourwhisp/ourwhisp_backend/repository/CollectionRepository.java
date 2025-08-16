package com.ourwhisp.ourwhisp_backend.repository;

import com.ourwhisp.ourwhisp_backend.model.Collection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CollectionRepository extends MongoRepository<Collection, String> {
    Optional<Collection> findBySessionUUID(String sessionUUID);
}
