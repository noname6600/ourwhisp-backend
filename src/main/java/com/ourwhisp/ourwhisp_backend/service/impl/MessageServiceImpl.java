package com.ourwhisp.ourwhisp_backend.service.impl;

import com.ourwhisp.ourwhisp_backend.dto.MessageSearchFilterDto;
import com.ourwhisp.ourwhisp_backend.exception.ResourceNotFoundException;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;
import com.ourwhisp.ourwhisp_backend.repository.MessageRepository;
import com.ourwhisp.ourwhisp_backend.service.IMessageService;
import com.ourwhisp.ourwhisp_backend.service.MessageCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final MessageCacheService messageCacheService;

    @Override
    public Message createMessage(Message message) {
        if (message.getCreatAt() == null) message.setCreatAt(Instant.now());
        if (message.getView() == null) message.setView(0L);

        Message saved = messageRepository.save(message);
        messageCacheService.cacheMessage(saved);
        return saved;
    }

    @Override
    public Message getMessageById(String id) {
        return getMessageById(null, id);
    }

    @Override
    //this function similar to mark as read/ or remove "pending" on msg. Another step to confirm if user actually read
    public Message getMessageById(String sessionUUID, String messageId) {
        Message cached = messageCacheService.getMessage(sessionUUID, messageId);
        if (cached != null) return cached;

        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        messageCacheService.cacheMessage(msg);
        return msg;
    }

    @Override
    //get random msg and mark "pending" on them so it wont pick up "pending" msg when calling this again
    public List<Message> getRandomMessagesForSession(String sessionUUID, int limit) {
        return messageCacheService.getRandomMessagesForSession(sessionUUID, limit);
    }

    @Override
    public MessageSearchResult searchMessages(String sessionUUID, MessageSearchFilterDto filter) {
        MessageSearchResult cached = messageCacheService.getSearchResult(sessionUUID, filter);
        if (cached != null) return cached;

        Query query = new Query();
        boolean hasFilter = false;
        List<Criteria> contentCriterias = new ArrayList<>();
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            contentCriterias.add(Criteria.where("content").regex(filter.getKeyword(), "i"));
        }

        if (filter.getLength() != null) {
            if (filter.getLength().equalsIgnoreCase("short")) {
                contentCriterias.add(Criteria.where("content").regex("^.{0,50}$"));
            } else if (filter.getLength().equalsIgnoreCase("long")) {
                contentCriterias.add(Criteria.where("content").regex("^.{51,}$"));
            }
        }
        if (!contentCriterias.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(contentCriterias.toArray(new Criteria[0])));
            hasFilter = true;
        }

        if (filter.getMinViews() != null) {
            query.addCriteria(Criteria.where("view").gte(filter.getMinViews()));
            hasFilter = true;
        }

        if (!hasFilter) {
            throw new IllegalArgumentException("At least one filter must be provided");
        }

        long totalCount = mongoTemplate.count(query, Message.class);
        query.skip((long) filter.getPage() * filter.getSize()).limit(filter.getSize());
        List<Message> results = mongoTemplate.find(query, Message.class);

        messageCacheService.saveSearchResult(sessionUUID, filter, results, totalCount);

        int totalPages = (int) Math.ceil((double) totalCount / filter.getSize());
        return new MessageSearchResult(results, filter.getPage(), filter.getSize(), totalPages, totalCount);
    }
}
