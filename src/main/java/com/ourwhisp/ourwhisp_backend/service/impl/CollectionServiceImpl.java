package com.ourwhisp.ourwhisp_backend.service.impl;

import com.ourwhisp.ourwhisp_backend.exception.ResourceNotFoundException;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageCollection;
import com.ourwhisp.ourwhisp_backend.repository.CollectionRepository;
import com.ourwhisp.ourwhisp_backend.service.ICollectionService;
import com.ourwhisp.ourwhisp_backend.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements ICollectionService {

    private final CollectionRepository collectionRepository;
    private final IMessageService messageService;

    @Override
    public MessageCollection getCollection(String sessionUUID) {
        return collectionRepository.findBySessionUUID(sessionUUID)
                .orElseGet(() -> {
                    MessageCollection c = new MessageCollection();
                    c.setSessionUUID(sessionUUID);
                    c.setMessageIds(new HashSet<>());
                    return collectionRepository.save(c);
                });
    }

    @Override
    public void addMessageToCollection(String sessionUUID, String messageId) {
        messageService.getMessageById(messageId);
        MessageCollection collection = collectionRepository.findBySessionUUID(sessionUUID)
                .orElseGet(() -> {
                    MessageCollection c = new MessageCollection();
                    c.setSessionUUID(sessionUUID);
                    c.setMessageIds(new HashSet<>());
                    return collectionRepository.save(c);
                });
        collection.getMessageIds().add(messageId);
        collectionRepository.save(collection);
    }

    @Override
    public void removeMessageFromCollection(String sessionUUID, String messageId) {
        messageService.getMessageById(messageId);
        MessageCollection collection = collectionRepository.findBySessionUUID(sessionUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        collection.getMessageIds().remove(messageId);
        collectionRepository.save(collection);
    }

    @Override
    public List<Message> getMessagesFromCollection(String sessionUUID) {
        MessageCollection collection = getCollection(sessionUUID);
        return collection.getMessageIds().stream()
                .map(messageService::getMessageById)
                .collect(Collectors.toList());
    }

    @Override
    public void clearCollection(String sessionUUID) {
        MessageCollection collection = collectionRepository.findBySessionUUID(sessionUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        collection.getMessageIds().clear();
        collectionRepository.save(collection);
    }
}
