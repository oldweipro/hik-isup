package com.oldwei.hikisup.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalCacheService {
    // 私有构造函数，防止外部实例化
    private GlobalCacheService() {
    }

    private final java.util.concurrent.locks.ReentrantReadWriteLock readWriteLock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    // 静态内部类实现延迟加载的单例模式
    public static class InstanceHolder {
        private static final GlobalCacheService INSTANCE = new GlobalCacheService();
    }

    // 使用 ConcurrentHashMap 作为内存中的键值对存储
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    // 获取全局单例的实例
    public static GlobalCacheService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    // 向缓存中添加数据
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    // 从缓存中读取数据
    public Object get(String key) {
        return cache.get(key);
    }

    // 从缓存中移除数据
    public void remove(String key) {
        cache.remove(key);
    }

    public Map<String, Object> getAll() {
        readWriteLock.readLock().lock();
        try {
            // 返回一个不可修改的视图，以防止外部对集合的修改
            return Collections.unmodifiableMap(cache);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
