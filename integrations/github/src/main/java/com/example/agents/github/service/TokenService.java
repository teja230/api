package com.example.agents.github.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class TokenService {

    private static final Duration TOKEN_EXPIRY = Duration.ofHours(1);
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeToken(String userId, String token) {
        String key = "github:token:" + userId;
        redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRY);
    }

    public Optional<String> getToken(String userId) {
        String key = "github:token:" + userId;
        Object token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token).map(Object::toString);
    }

    public void revokeToken(String userId) {
        String key = "github:token:" + userId;
        redisTemplate.delete(key);
    }

    public boolean isTokenValid(String userId) {
        return getToken(userId).isPresent();
    }
} 