package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageCollection;

import java.util.List;

public interface ICollectionService {
    MessageCollection getCollection(String sessionUUID);
    void addMessageToCollection(String sessionUUID, String messageId);
    void removeMessageFromCollection(String sessionUUID, String messageId);
    List<Message> getMessagesFromCollection(String sessionUUID);
    void clearCollection(String sessionUUID);

}
