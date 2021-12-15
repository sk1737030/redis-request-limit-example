package com.example.redisrequestlimitexample.limit.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;

@RestController
public class PingController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping("ping")

    public String ping(@RequestParam String openApiKey) {
        ZSetOperations<String, Object> zSet = redisTemplate.opsForZSet();

        long now = System.currentTimeMillis();
        long limit = 3000L;

        zSet.removeRangeByScore(openApiKey, 0, now - limit);
        Long currentSize = zSet.size(openApiKey);

        if (currentSize > 4) {
            return "Nope Api Over";
        }

        RedisOperations<String, Object> operations = zSet.getOperations();
        SessionCallback<Object> sessionCallback = new SessionCallback<>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                long now = System.currentTimeMillis();
                operations.multi();

                zSet.add(openApiKey, now, now);

                if (currentSize == 0) {
                    operations.expire((K) openApiKey, Duration.ofSeconds(10));
                }

                return operations.exec();
            }
        };

        operations.execute(sessionCallback);

        return "pong";
    }
}
