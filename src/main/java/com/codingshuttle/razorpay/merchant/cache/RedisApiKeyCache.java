package com.codingshuttle.razorpay.merchant.cache;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisApiKeyCache implements ApiKeyCache {
//fhgjfjjfut                            ddgkgdfdnrgrdlrbdwsgftgnghghgtjgjddbdb
    private static final String PREFIX = "apikey:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(PREFIX+keyId);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, ApiKeyCacheEntry.class));
        } catch (Exception e) {
            log.warn("ApiKey cache read filed, keyId: {}", keyId);
            return Optional.empty();
        }
    }

//    @Override
//    public void put(String keyId, ApiKeyCacheEntry entry) {
//        try {
//            stringRedisTemplate.opsForValue().set(PREFIX+keyId,
//                    objectMapper.writeValueAsString(entry),
//                    TTL);
//        } catch (Exception e) {
//            log.warn("ApiKey cache put failed, keyId: {}", keyId);
//        }
//    }
@Override
public void put(String keyId, ApiKeyCacheEntry entry) {
    try {
        log.info("Putting API key into Redis. keyId={}", keyId);

        String json = objectMapper.writeValueAsString(entry);
        log.info("Redis cache value={}", json);

        stringRedisTemplate.opsForValue().set(
                PREFIX + keyId,
                json,
                TTL
        );

        log.info("Successfully stored Redis key={}", PREFIX + keyId);

    } catch (Exception e) {
        log.error("ApiKey cache put failed, keyId={}", keyId, e);
    }
}

    @Override
    public void evict(String keyId) {
        stringRedisTemplate.delete(PREFIX+keyId);
    }
}

