package com.oldwei.hikisup.service.impl;

import com.oldwei.hikisup.service.ISimpleCacheService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleCacheServiceImpl implements ISimpleCacheService {
    // 使用 ConcurrentHashMap 作为内存中的键值对存储
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }
}
