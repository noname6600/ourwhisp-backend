package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.dto.MessageDto;
import com.ourwhisp.ourwhisp_backend.dto.MessageSearchResultDto;
import com.ourwhisp.ourwhisp_backend.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageSearchService {

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_SEARCH_PREFIX = "SEARCH:";

    public MessageSearchResultDto searchMessages(String keyword, int page, int size) {
        String cacheKey = KEY_SEARCH_PREFIX + keyword.toLowerCase() + ":page:" + page + ":size:" + size;

        List<MessageDto> cached = (List<MessageDto>) redisTemplate.opsForValue().get(cacheKey);
        Long total = (Long) redisTemplate.opsForValue().get(KEY_SEARCH_PREFIX + keyword.toLowerCase() + ":total");
        if (cached != null && total != null) {
            Collections.shuffle(cached);
            int totalPages = (int) Math.ceil((double) total / size);
            return new MessageSearchResultDto(cached, page, size, totalPages, total);
        }

        Query countQuery = new Query();
        countQuery.addCriteria(Criteria.where("content").regex(keyword, "i"));
        long totalCount = mongoTemplate.count(countQuery, Message.class);

        Query query = new Query();
        query.addCriteria(Criteria.where("content").regex(keyword, "i"));
        query.skip(page * size).limit(size);

        List<Message> results = mongoTemplate.find(query, Message.class);
        List<MessageDto> dtos = results.stream().map(MessageDto::fromEntity).collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, dtos, Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(KEY_SEARCH_PREFIX + keyword.toLowerCase() + ":total", totalCount, Duration.ofMinutes(5));

        Collections.shuffle(dtos);
        int totalPages = (int) Math.ceil((double) totalCount / size);
        return new MessageSearchResultDto(dtos, page, size, totalPages, totalCount);
    }
}