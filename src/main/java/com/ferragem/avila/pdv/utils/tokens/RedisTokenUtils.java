package com.ferragem.avila.pdv.utils.tokens;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.utils.RedisUtils;

@Component
public class RedisTokenUtils {

    private final RedisUtils redisUtils;

    public RedisTokenUtils(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

	public void storeOperationToken(String userId, String operationToken, Instant expirationTime) {
		long expirationTimeInSeconds = Duration.between(Instant.now(), expirationTime).getSeconds();
		redisUtils.storeValue(userId, operationToken, expirationTimeInSeconds, TimeUnit.SECONDS);
	}

	public void deleteOperationToken(String userId) {
		redisUtils.deleteStoredValue(userId);
	}

	public Optional<String> getJweStoredToken(String userId) {
		return Optional.ofNullable((redisUtils.getValue(userId)));
	}

    public void revokeToken(String token, Instant expirationTime) {
        long expirationTimeInSeconds = Duration.between(Instant.now(), expirationTime).getSeconds();
		redisUtils.storeValue(token, "revoked", expirationTimeInSeconds, TimeUnit.SECONDS);
    }

    public boolean isTokenRevoked(String token) {
        return redisUtils.hasKey(token);
    }

}
