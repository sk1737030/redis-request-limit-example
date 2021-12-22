package com.example.redisrequestlimitexample.limit.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

@RestController

public class PingController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping("ping")
    public String ping(@RequestParam String openApiKey) {
        ZSetOperations<String, Object> zSet = redisTemplate.opsForZSet();
        long now = System.currentTimeMillis();
        double limit = 1000;
        zSet.removeRangeByScore(openApiKey, 0, now - limit);

        Object execute = redisTemplate.execute((RedisCallback<Object>) connection -> {
            byte[] openApiKeyBytes = openApiKey.getBytes(StandardCharsets.UTF_8);
            try {

                Set<byte[]> zRangeByScores = connection.zRangeByScore(openApiKeyBytes, 0, -1);
                if (zRangeByScores != null && !zRangeByScores.isEmpty()) {
                    return null;
                }
                connection.expire(openApiKeyBytes, 1000);
                connection.watch(openApiKeyBytes);
                connection.multi();
                connection.zAdd(openApiKeyBytes, now, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
                return connection.exec();
            } catch (Exception e) {
                connection.discard();
                return null;
            }
        });

        if (execute == null) {
            return "Nope";
        }
        return "pong";
    }
}
