package com.example.wustchat.service;

import com.example.wustchat.util.UidUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    private final RedisTemplate<String, Object> redisTemplate;

    public AdminAuthService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String requireUid() {
        String uid = UidUtils.getUid();
        if (uid == null || uid.isBlank()) {
            throw new AccessDeniedException("未登录");
        }
        return uid;
    }

    public String requireToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new AccessDeniedException("未登录");
        }
        String token = String.valueOf(authentication.getCredentials());
        if (token.isBlank()) {
            throw new AccessDeniedException("未登录");
        }
        return token;
    }

    public Integer requireAdminRole() {
        requireUid();
        String token = requireToken();
        Object username = redisTemplate.opsForValue().get("Admin:" + token);
        Object role = redisTemplate.opsForValue().get("AdminRole:" + token);
        if (username == null || role == null) {
            throw new AccessDeniedException("管理员权限校验失败");
        }
        if (role instanceof Number) {
            return ((Number) role).intValue();
        }
        return Integer.parseInt(String.valueOf(role));
    }
}
