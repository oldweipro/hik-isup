package com.oldwei.hikisup.service;

public interface ISimpleCacheService {
    // 向缓存中添加数据
    void put(String key, Object value);

    // 从缓存中读取数据
    Object get(String key);

    // 从缓存中移除数据
    void remove(String key);
}
