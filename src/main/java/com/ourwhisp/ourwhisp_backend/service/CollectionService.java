package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.dto.MessageDto;
import com.ourwhisp.ourwhisp_backend.model.Collection;
import com.ourwhisp.ourwhisp_backend.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final MessageService messageService;

    public Collection getCollection(String sessionUUID) {
        return collectionRepository.findBySessionUUID(sessionUUID)
                .orElseGet(() -> {
                    Collection c = new Collection();
                    c.setSessionUUID(sessionUUID);
                    c.setMessageIds(new HashSet<>());
                    return collectionRepository.save(c);
                });
    }

    public Collection addMessageToCollection(String sessionUUID, String messageId) {
        Collection c = getCollection(sessionUUID);
        c.getMessageIds().add(messageId);
        return collectionRepository.save(c);
    }

    public Collection removeMessageFromCollection(String sessionUUID, String messageId) {
        Collection c = getCollection(sessionUUID);
        c.getMessageIds().remove(messageId);
        return collectionRepository.save(c);
    }

    public List<MessageDto> getMessagesFromCollection(String sessionUUID) {
        Collection c = getCollection(sessionUUID);
        return c.getMessageIds().stream()
                .map(messageService::getMessageById)
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }
}
