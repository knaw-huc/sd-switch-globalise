package org.knaw.huc.sdswitch.server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<K, V> {
    private final long expiryTime;
    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final Map<K, Long> insertionTimes = new ConcurrentHashMap<>();

    public Cache() {
        this(1000L);
    }

    public Cache(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public V put(K key, V value) {
        insertionTimes.put(key, System.currentTimeMillis());
        return cache.put(key, value);
    }

    public V get(K key) {
        long currentTime = System.currentTimeMillis();
        if (insertionTimes.containsKey(key) && currentTime > (insertionTimes.get(key) + expiryTime)) {
            insertionTimes.remove(key);
            cache.remove(key);
        }
        return cache.get(key);
    }
}