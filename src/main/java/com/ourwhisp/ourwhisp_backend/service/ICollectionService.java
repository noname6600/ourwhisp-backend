package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICollectionService {
    MessageCollection getCollection(String sessionUUID);
    void addMessageToCollection(String sessionUUID, String messageId);
    void removeMessageFromCollection(String sessionUUID, String messageId);
    List<Message> getMessagesFromCollection(String sessionUUID);
    void clearCollection(String sessionUUID);
    Page<Message> getPageMessagesFromCollection(String sessionUUID, String keyword, Pageable pageable);

}
