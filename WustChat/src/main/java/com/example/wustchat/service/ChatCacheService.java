package com.example.wustchat.service;

import com.example.wustchat.config.WustChatCacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class ChatCacheService {
    private static final String QA_CACHE_SCHEMA_VERSION = "v2";

    private final RedisTemplate<String, Object> redisTemplate;
    private final WustChatCacheProperties cacheProperties;

    public ChatCacheService(RedisTemplate<String, Object> redisTemplate, WustChatCacheProperties cacheProperties) {
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
    }

    public long getKbVersion() {
        Object version = redisTemplate.opsForValue().get(cacheProperties.getKbVersionKey());
        if (version == null) {
            redisTemplate.opsForValue().setIfAbsent(cacheProperties.getKbVersionKey(), 1L);
            return 1L;
        }
        if (version instanceof Number) {
            return ((Number) version).longValue();
        }
        return Long.parseLong(String.valueOf(version));
    }

    public long bumpKbVersion() {
        Long version = redisTemplate.opsForValue().increment(cacheProperties.getKbVersionKey());
        return version == null ? getKbVersion() : version;
    }

    public String buildQaKey(String question, String tag, int topK) {
        return "wust:chat:qa:" + QA_CACHE_SCHEMA_VERSION + ":" + getKbVersion() + ":" + sha256(question + "|" + tag + "|" + topK);
    }

    public String buildCorpusListKey() {
        return "wust:chat:corpus:list:" + getKbVersion() + ":1:1000:all:all";
    }

    public String buildCorpusDetailKey(Long id) {
        return "wust:chat:corpus:detail:" + getKbVersion() + ":" + id;
    }

    public String buildTagsKey() {
        return "wust:chat:tags:" + getKbVersion();
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setQa(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, cacheProperties.getQaTtlMinutes(), TimeUnit.MINUTES);
    }

    public void setCorpusList(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, cacheProperties.getCorpusListTtlMinutes(), TimeUnit.MINUTES);
    }

    public void setCorpusDetail(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, cacheProperties.getCorpusDetailTtlMinutes(), TimeUnit.MINUTES);
    }

    public void setTags(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, cacheProperties.getTagsTtlMinutes(), TimeUnit.MINUTES);
    }

    public long clearQaCache() {
        var keys = redisTemplate.keys("wust:chat:qa:*");
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        Long deleted = redisTemplate.delete(keys);
        return deleted == null ? 0 : deleted;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte aByte : bytes) {
                sb.append(String.format("%02x", aByte));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
