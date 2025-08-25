package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.dto.MessageSearchFilterDto;
import com.ourwhisp.ourwhisp_backend.dto.MessageSearchResultDto;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;

import java.util.List;

public interface IMessageService {
    Message createMessage(Message message);
    Message getMessageById(String id);
    Message getMessageById(String sessionUUID, String messageId);
    List<Message> getRandomMessagesForSession(String sessionUUID, int limit);
    MessageSearchResult searchMessages(String sessionUUID, MessageSearchFilterDto filter);
}
