package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.exception.ResourceNotFoundException;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private MongoTemplate mongoTemplate;

    private static final String KEY_ALL = "our:all";
    private static final String KEY_MSG_PREFIX = "our:msg:";
    private static final String KEY_VIEWS_PREFIX = "our:views:";
    private static final String KEY_READ_PREFIX = "our:read:";


    public Message createMessage(Message message) {
        if (message.getCreatAt() == null) message.setCreatAt(Instant.now());
        if (message.getView() == null) message.setView(0L);

        Message saved = messageRepository.save(message);

        double score = ThreadLocalRandom.current().nextDouble();
        redisTemplate.opsForZSet().add(KEY_ALL, saved.getId(), score);

        cacheMessage(saved);

        return saved;
    }

    public void deleteMessage(String id) {
        messageRepository.deleteById(id);
        redisTemplate.opsForZSet().remove(KEY_ALL, id);
        redisTemplate.delete(KEY_MSG_PREFIX + id);
        redisTemplate.delete(KEY_VIEWS_PREFIX + id);
    }

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

    public Message incrementView(String id) {
        redisTemplate.opsForValue().increment(KEY_VIEWS_PREFIX + id);
        return getMessageById(id);
    }

    public Message markAsRead(String sessionUUID, String messageId) {
        redisTemplate.opsForSet().add(KEY_READ_PREFIX + sessionUUID, messageId);
        redisTemplate.expire(KEY_READ_PREFIX + sessionUUID, java.time.Duration.ofHours(1));

        return incrementView(messageId);
    }

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


    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    private void cacheMessage(Message m) {
        String key = KEY_MSG_PREFIX + m.getId();
        redisTemplate.opsForHash().put(key, "content", m.getContent());
        redisTemplate.opsForHash().put(key, "createdAt", m.getCreatAt().toString());
        redisTemplate.opsForHash().put(key, "view", String.valueOf(m.getView()));
        redisTemplate.expire(key, java.time.Duration.ofMinutes(30));
    }
}
