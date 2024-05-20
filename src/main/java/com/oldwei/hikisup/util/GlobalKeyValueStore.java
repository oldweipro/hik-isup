package com.oldwei.hikisup.util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalKeyValueStore {
    // 创建一个全局的线程安全Key-Value存储实例
    private static final HashMap<String, String> store = new HashMap<>();

    // 提供一个方法来添加元素（如果Key已经存在，将覆盖旧的值）
    public static void put(String key, String value) {
        store.put(key, value);
    }

    // 提供一个方法来获取元素
    public static String get(String key) {
        return store.get(key);
    }

    // 提供一个方法来删除元素
    public static String remove(String key) {
        return store.remove(key);
    }

    // 提供一个方法来检查Key是否存在
    public static boolean containsKey(String key) {
        return store.containsKey(key);
    }
}
