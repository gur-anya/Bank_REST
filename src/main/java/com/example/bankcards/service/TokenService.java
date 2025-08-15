package com.example.bankcards.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    private final StringRedisTemplate tokensBlacklist;
    private final Long TOKEN_TTL;

    public TokenService(StringRedisTemplate tokensBlacklist,
                        @Value("${jwt.expiration}") Long tokenTtl) {
        this.tokensBlacklist = tokensBlacklist;
        this.TOKEN_TTL = tokenTtl;
    }

    public void blacklistToken(String token) {
        tokensBlacklist.opsForValue().set(token, "blacklisted", TOKEN_TTL, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        Boolean isBlacklisted = tokensBlacklist.hasKey(token);
        return Objects.requireNonNullElse(isBlacklisted, false);
    }
}
