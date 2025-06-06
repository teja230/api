package com.enterprise.agents.common.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class TestConfig {
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Return a mock RedisConnectionFactory
        return Mockito.mock(RedisConnectionFactory.class);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // Return a mock RedisTemplate
        return Mockito.mock(RedisTemplate.class);
    }
}
