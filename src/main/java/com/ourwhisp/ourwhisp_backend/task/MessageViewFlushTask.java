package com.ourwhisp.ourwhisp_backend.task;

import com.ourwhisp.ourwhisp_backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class MessageViewFlushTask {

    private final RedisTemplate<String, String> redisTemplate;
    private final MessageRepository messageRepository;

    private static final String KEY_VIEWS_PREFIX = "our:views:";

    @Scheduled(fixedRate = 60000)
    public void flushViewsToMongo() {
        Set<String> keys = redisTemplate.keys(KEY_VIEWS_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String id = key.replace(KEY_VIEWS_PREFIX, "");
            String viewsStr = redisTemplate.opsForValue().get(key);
            if (viewsStr != null) {
                long increment = Long.parseLong(viewsStr);

                messageRepository.findById(id).ifPresent(msg -> {
                    msg.setView((msg.getView() == null ? 0 : msg.getView()) + increment);
                    messageRepository.save(msg);
                });

                redisTemplate.delete(key);
            }
        }
    }
}
