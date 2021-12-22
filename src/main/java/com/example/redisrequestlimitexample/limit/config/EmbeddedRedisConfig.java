package com.example.redisrequestlimitexample.limit.config;

import org.springframework.context.annotation.Configuration;


@Configuration
public class EmbeddedRedisConfig {
/*

    private final RedisServer redisServer;

    public EmbeddedRedisConfig(@Value("${spring.redis.port}") int redisPort) {
        redisServer = new RedisServer(redisPort);
    }

    @PostConstruct
    public void startRedis() {
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }
*/

}
