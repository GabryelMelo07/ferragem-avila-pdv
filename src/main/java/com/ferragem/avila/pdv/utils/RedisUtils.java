package com.ferragem.avila.pdv.utils;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.json.JsonParseException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RedisUtils {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	public RedisUtils(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMessage(String channel, String message) {
		redisTemplate.convertAndSend(channel, message);
	}

	public Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	public Object getValue(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public <T> void storeValue(String key, T value, long timeout, TimeUnit timeUnit) {		
		try {
			String valueStr = (value instanceof String) ? (String) value : objectMapper.writeValueAsString(value);
			redisTemplate.opsForValue().set(key, valueStr, 3, TimeUnit.HOURS);
		} catch (JsonProcessingException e) {
			throw new JsonParseException(e);
		}
	}

	public <T> void storeValue(String key, T value) {		
		try {
			String resultJson = objectMapper.writeValueAsString(value);
			redisTemplate.opsForValue().set(key, resultJson);
		} catch (JsonProcessingException e) {
			throw new JsonParseException(e);
		}
	}

	public void deleteStoredValue(String key) {
		redisTemplate.delete(key);
	}

}
