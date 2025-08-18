package com.ourwhisp.ourwhisp_backend.service.impl;

import com.ourwhisp.ourwhisp_backend.dto.MessageSearchFilterDto;
import com.ourwhisp.ourwhisp_backend.exception.ResourceNotFoundException;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.model.MessageSearchResult;
import com.ourwhisp.ourwhisp_backend.repository.MessageRepository;
import com.ourwhisp.ourwhisp_backend.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;

    private final  MongoTemplate mongoTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;


    private static final String KEY_ALL = "our:all";
    private static final String KEY_MSG_PREFIX = "our:msg:";
    private static final String KEY_VIEWS_PREFIX = "our:views:";
    private static final String KEY_READ_PREFIX = "our:read:";

    @Override
    public Message createMessage(Message message) {
        if (message.getCreatAt() == null) message.setCreatAt(Instant.now());
        if (message.getView() == null) message.setView(0L);

        Message saved = messageRepository.save(message);

        double score = ThreadLocalRandom.current().nextDouble();
        redisTemplate.opsForZSet().add(KEY_ALL, saved.getId(), score);

        cacheMessage(saved);

        return saved;
    }


    @Override
    public Message getMessageById(String id) {
        String key = KEY_MSG_PREFIX + id;
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(key);

        if (cached != null && !cached.isEmpty()) {
            Message msg = new Message();
            msg.setId(id);
            msg.setContent((String) cached.get("content"));
            msg.setCreatAt(Instant.parse((String) cached.get("createdAt")));
            msg.setView(Long.parseLong((String) cached.get("view")));
            return msg;
        }

        return messageRepository.findById(id)
                .map(m -> {
                    cacheMessage(m);
                    return m;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    private Message incrementViewOnce(String sessionUUID, String messageId) {
        String readKey = KEY_READ_PREFIX + sessionUUID;
        String viewKey = KEY_VIEWS_PREFIX + messageId;

        Boolean alreadyRead = redisTemplate.opsForSet().isMember(readKey, messageId);

        if (Boolean.FALSE.equals(alreadyRead)) {
            redisTemplate.opsForSet().add(readKey, messageId);
            redisTemplate.opsForValue().increment(viewKey);
            redisTemplate.expire(readKey, java.time.Duration.ofHours(1));
        }

        return getMessageById(messageId);
    }

    @Override
    public Message markAsRead(String sessionUUID, String messageId) {
        return incrementViewOnce(sessionUUID, messageId);
    }


    @Override
    public List<Message> getRandomMessagesForSession(String sessionUUID, int limit) {
        final Set<String> readIds = Optional.ofNullable(redisTemplate.opsForSet().members(KEY_READ_PREFIX + sessionUUID))
                .orElse(Collections.emptySet())
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        Long poolSize = redisTemplate.opsForZSet().zCard(KEY_ALL);
        int fetchCount = Math.max(limit * 10, 10);
        if (poolSize != null && poolSize > 0) {
            fetchCount = Math.min(fetchCount, poolSize.intValue());
        }

        List<String> randomIds = Optional.ofNullable(redisTemplate.opsForZSet().randomMembers(KEY_ALL, fetchCount))
                .orElse(Collections.emptyList());

        if (randomIds.isEmpty()) {
            List<Message> mongoSample = mongoTemplate.aggregate(
                    Aggregation.newAggregation(Aggregation.sample(fetchCount)),
                    "messages",
                    Message.class
            ).getMappedResults();

            for (Message m : mongoSample) {
                double score = ThreadLocalRandom.current().nextDouble();
                redisTemplate.opsForZSet().add(KEY_ALL, m.getId(), score);
            }

            randomIds = mongoSample.stream().map(Message::getId).collect(Collectors.toList());
        }

        List<String> filtered = randomIds.stream()
                .filter(id -> !readIds.contains(id))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());

        if (filtered.size() < limit) {
            for (String id : randomIds) {
                if (!filtered.contains(id)) {
                    filtered.add(id);
                    if (filtered.size() == limit) break;
                }
            }
        }

        return filtered.stream()
                .map(this::getMessageById)
                .collect(Collectors.toList());
    }




    private void cacheMessage(Message m) {
        String key = KEY_MSG_PREFIX + m.getId();
        redisTemplate.opsForHash().put(key, "content", m.getContent());
        redisTemplate.opsForHash().put(key, "createdAt", m.getCreatAt().toString());
        redisTemplate.opsForHash().put(key, "view", String.valueOf(m.getView()));
        redisTemplate.expire(key, java.time.Duration.ofMinutes(30));
    }

    @Override
    public MessageSearchResult searchMessages(String sessionUUID, MessageSearchFilterDto filter) {
        String cacheKey = String.format(
                "smartSearch:%s:keyword:%s:length:%s:minViews:%s:page:%d:size:%d",
                sessionUUID,
                filter.getKeyword() == null ? "" : filter.getKeyword().toLowerCase(),
                filter.getLength() == null ? "" : filter.getLength().toLowerCase(),
                filter.getMinViews() == null ? "" : filter.getMinViews(),
                filter.getPage(),
                filter.getSize()
        );
        String totalKey = cacheKey + ":total";

        List<Message> cachedMessages = (List<Message>) objectRedisTemplate.opsForValue().get(cacheKey);
        Object cachedTotalObj = objectRedisTemplate.opsForValue().get(totalKey);
        long cachedTotal = cachedTotalObj == null ? 0L : ((Number) cachedTotalObj).longValue();

        if (cachedMessages != null && cachedTotalObj != null) {
            Collections.shuffle(cachedMessages);
            int totalPages = (int) Math.ceil((double) cachedTotal / filter.getSize());
            return new MessageSearchResult(cachedMessages, filter.getPage(), filter.getSize(), totalPages, cachedTotal);
        }

        Query query = new Query();
        boolean hasFilter = false;

        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            query.addCriteria(Criteria.where("content").regex(filter.getKeyword(), "i"));
            hasFilter = true;
        }

        if (filter.getLength() != null) {
            if (filter.getLength().equalsIgnoreCase("short")) {
                query.addCriteria(Criteria.where("content").regex("^.{0,50}$"));
            } else if (filter.getLength().equalsIgnoreCase("long")) {
                query.addCriteria(Criteria.where("content").regex("^.{51,}$"));
            }
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
        Collections.shuffle(results);

        objectRedisTemplate.opsForValue().set(cacheKey, results, Duration.ofMinutes(5));
        objectRedisTemplate.opsForValue().set(totalKey, totalCount, Duration.ofMinutes(5));

        int totalPages = (int) Math.ceil((double) totalCount / filter.getSize());
        return new MessageSearchResult(results, filter.getPage(), filter.getSize(), totalPages, totalCount);
    }

}
