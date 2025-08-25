package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.model.Message;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class MessageRedisService extends BaseRedisService<Message> {

    private static final String HASH_KEY = "messages:hash";
    private static final String ZSET_KEY = "messages:zset";
    private static final String READ_PREFIX = "messages:read:";
    private static final String PENDING_PREFIX = "messages:pending:";

    public static String getPendingPrefix() {
        return PENDING_PREFIX;
    }

    public void saveMessage(Message message) {
        if (message.getCreatAt() == null) message.setCreatAt(Instant.now());
        if (message.getView() == null) message.setView(0L);

        saveToHash(HASH_KEY, message.getId(), message);
        addToZSet(ZSET_KEY, message.getId(), ThreadLocalRandom.current().nextDouble());
    }

    public Message getMessage(String id) {
        return getFromHash(HASH_KEY, id);
    }

    public void deleteMessage(String id) {
        deleteFromHash(HASH_KEY, id);
        removeFromZSet(ZSET_KEY, id);
    }

    private String pendingKey(String sessionUUID) {
        return PENDING_PREFIX + sessionUUID;
    }

    private String readKey(String sessionUUID) {
        return READ_PREFIX + sessionUUID;
    }

    // mark message pending
    public void markPending(String sessionUUID, String messageId) {
        addToZSet(pendingKey(sessionUUID), messageId, Instant.now().toEpochMilli());
        expireKey(pendingKey(sessionUUID), 1, TimeUnit.HOURS);
    }

    public Message confirmReadIfPending(String sessionUUID, String messageId) {
        if (hasMemberInZSet(pendingKey(sessionUUID), messageId)) {
            removeFromZSet(pendingKey(sessionUUID), messageId);
            addToZSet(readKey(sessionUUID), messageId, Instant.now().toEpochMilli());
            expireKey(readKey(sessionUUID), 1, TimeUnit.HOURS);

            Message m = getMessage(messageId);
            if (m != null) {
                m.setView(m.getView() + 1);
                saveToHash(HASH_KEY, m.getId(), m);
                return m;
            }
        }
        return getMessage(messageId);
    }

    public boolean isPending(String sessionUUID, String messageId) {
        return hasMemberInZSet(pendingKey(sessionUUID), messageId);
    }

    public boolean isRead(String sessionUUID, String messageId) {
        return hasMemberInZSet(readKey(sessionUUID), messageId);
    }

    public Set<String> getAllMessageIds() {
        return Optional.ofNullable(getRangeFromZSet(ZSET_KEY, 0, -1))
                .orElse(Collections.emptySet());
    }

    public Set<String> getReadIds(String sessionUUID) {
        return Optional.ofNullable(getRangeFromZSet(readKey(sessionUUID), 0, -1))
                .orElse(Collections.emptySet());
    }
}
