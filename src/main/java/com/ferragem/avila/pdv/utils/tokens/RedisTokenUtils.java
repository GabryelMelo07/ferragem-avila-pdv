package com.ferragem.avila.pdv.utils.tokens;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTokenUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTokenUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

	public void storeOperationToken(String userId, String operationToken, Instant expirationTime) {
		long expirationTimeInSeconds = Duration.between(Instant.now(), expirationTime).getSeconds();
		redisTemplate.opsForValue().set(userId, operationToken, expirationTimeInSeconds, TimeUnit.SECONDS);
	}

	public void deleteOperationToken(String userId) {
		redisTemplate.delete(userId);
	}

	public Optional<String> getJweStoredToken(String userId) {
		return Optional.ofNullable(String.valueOf(redisTemplate.opsForValue().get(userId)));
	}

    public void revokeToken(String token, Instant expirationTime) {
        long expirationTimeInSeconds = Duration.between(Instant.now(), expirationTime).getSeconds();
        redisTemplate.opsForValue().set(token, "revoked", expirationTimeInSeconds, TimeUnit.SECONDS);
    }

    public boolean isTokenRevoked(String token) {
        return redisTemplate.hasKey(token);
    }

}
