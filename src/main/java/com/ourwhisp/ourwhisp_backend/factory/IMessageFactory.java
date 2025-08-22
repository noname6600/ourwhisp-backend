package com.ourwhisp.ourwhisp_backend.factory;

import com.ourwhisp.ourwhisp_backend.dto.MessageRequestDto;
import com.ourwhisp.ourwhisp_backend.dto.MessageResponseDto;
import com.ourwhisp.ourwhisp_backend.dto.MessageSearchFilterDto;
import com.ourwhisp.ourwhisp_backend.factory.base.IDataFactory;
import com.ourwhisp.ourwhisp_backend.factory.base.IPersistDataFactory;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;

import java.util.List;
import java.util.UUID;

public interface IMessageFactory extends
        IPersistDataFactory<MessageRequestDto, MessageResponseDto, Message>,
        IDataFactory<UUID, MessageRequestDto, MessageResponseDto> {

//    MessageResponseDto markAsRead(String sessionUUID, UUID messageId);
//    List<MessageResponseDto> getRandomMessagesForSession(String sessionUUID, int limit);
//    MessageSearchResult searchMessages(String sessionUUID, MessageSearchFilterDto filter);
}
