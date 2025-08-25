package com.ourwhisp.ourwhisp_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public abstract class BaseRedisService<T> {

    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> objectRedisTemplate;


    public void saveToHash(String key, String field, T value) {
        objectRedisTemplate.opsForHash().put(key, field, value);
    }

    public T getFromHash(String key, String field) {
        return (T) objectRedisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> getAllFromHash(String key) {
        return objectRedisTemplate.opsForHash().entries(key);
    }

    public void deleteFromHash(String key, String field) {
        objectRedisTemplate.opsForHash().delete(key, field);
    }

    public void addToZSet(String key, String member, double score) {
        stringRedisTemplate.opsForZSet().add(key, member, score);
    }

    public Set<String> getRangeFromZSet(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().range(key, start, end);
    }

    public boolean hasMemberInZSet(String key, String member) {
        return stringRedisTemplate.opsForZSet().score(key, member) != null;
    }

    public Long removeFromZSet(String key, String member) {
        return stringRedisTemplate.opsForZSet().remove(key, member);
    }

    public Long countZSet(String key) {
        return stringRedisTemplate.opsForZSet().size(key);
    }

    public boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public void deleteKey(String key) {
        stringRedisTemplate.delete(key);
        objectRedisTemplate.delete(key);
    }

    public void expireKey(String key, long timeout, TimeUnit unit) {
        stringRedisTemplate.expire(key, timeout, unit);
        objectRedisTemplate.expire(key, timeout, unit);
    }
}
