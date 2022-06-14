package dev.hanjoon.accountmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port}")
    private int redisPort;
    
    private RedisServer redisServer;

    @PostConstruct
    public void init() {
        redisServer = RedisServer.builder()
                                 .port(redisPort)
                                 .setting("maxmemory 64M")
                                 .build();
        redisServer.start();
    }

    @PreDestroy
    public void destroy() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
