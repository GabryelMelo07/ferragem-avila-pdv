package com.ferragem.avila.pdv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    public void save(String key, Object value) {
        String objectSerialized = null;

        try {
            objectSerialized = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        redisTemplate.opsForValue().set(key, objectSerialized);
    }

    public Object find(String key, Class<?> classType) {
        String objectStr = (String) redisTemplate.opsForValue().get(key);
        Object object = null;

        try {
            object = objectMapper.readValue(objectStr, classType);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return object;
    }

    public boolean existsByKey(String key) {
        if (redisTemplate.opsForValue().get(key) == null)
            return false;
        
        return true;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
}
