package com.nukkadseva.nukkadsevabackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(
                redisConnectionFactory
        );
        template.setKeySerializer(
                new StringRedisSerializer()
        );
        template.setValueSerializer(
                new StringRedisSerializer()
        );
        template.setHashKeySerializer(
                new StringRedisSerializer()
        );
        template.setHashValueSerializer(
                new StringRedisSerializer()
        );

        template.afterPropertiesSet();

        return template;
    }

    // string redis template for operations with string keys and values
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
