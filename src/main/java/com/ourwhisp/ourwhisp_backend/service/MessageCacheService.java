package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.dto.MessageSearchFilterDto;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageCacheService {

    private final MessageRedisService messageRedisService;
    private final MessageSearchCacheService searchCacheService;
    private final MongoTemplate mongoTemplate;

    public MessageCacheService(MessageRedisService messageRedisService,
                               MessageSearchCacheService searchCacheService,
                               MongoTemplate mongoTemplate) {
        this.messageRedisService = messageRedisService;
        this.searchCacheService = searchCacheService;
        this.mongoTemplate = mongoTemplate;
    }

    public void cacheMessage(Message message) {
        messageRedisService.saveMessage(message);
    }

    public Message getMessage(String sessionUUID, String messageId) {
        return messageRedisService.confirmReadIfPending(sessionUUID, messageId);
    }

    public List<Message> getRandomMessagesForSession(String sessionUUID, int limit) {
        int fetchCount = Math.max(limit * 10, 10);

        List<Message> mongoSample = mongoTemplate.aggregate(
                Aggregation.newAggregation(Aggregation.sample(fetchCount)),
                "message",
                Message.class
        ).getMappedResults();

        for (Message m : mongoSample) {
            messageRedisService.saveMessage(m);
        }

        Set<String> pendingIds = Optional.ofNullable(
                messageRedisService.getRangeFromZSet(messageRedisService.getPendingPrefix() + sessionUUID, 0, -1)
        ).orElse(Collections.emptySet());

        List<Message> candidates = mongoSample.stream()
                .filter(m -> !pendingIds.contains(m.getId()))
                .collect(Collectors.toList());

        Collections.shuffle(candidates);
        List<Message> selected = candidates.stream()
                .limit(limit)
                .collect(Collectors.toList());

        for (Message m : selected) {
            messageRedisService.markPending(sessionUUID, m.getId());
        }

        return selected;
    }




    public void saveSearchResult(String sessionUUID, MessageSearchFilterDto filter, List<Message> messages, long totalCount) {
        searchCacheService.saveSearchResult(sessionUUID, filter, messages, totalCount);
    }

    public MessageSearchResult getSearchResult(String sessionUUID, MessageSearchFilterDto filter) {
        return searchCacheService.getSearchResult(sessionUUID, filter);
    }

}



