package com.chen.utils;


import lombok.Data;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class InMemoryCache<K,V> {

    private final Map<K, CacheItem<V>> cache = new ConcurrentHashMap<>();

    private final long defaultExpirationTimeMillis;

    private final long extensionTimeMillis;

    private final int maxEntries;

    public InMemoryCache(long defaultExpirationTimeMillis, long extensionTimeMillis, int maxEntries) {
        this.defaultExpirationTimeMillis = defaultExpirationTimeMillis;
        this.extensionTimeMillis = extensionTimeMillis;
        this.maxEntries = maxEntries;
    }

    // Default: 1 minute expiration time, extend by 30 seconds on access
    public InMemoryCache() {
        this(60 * 1000, 30 * 1000, 100);
    }

    public void put(K key, V value, long expirationTimeMillis) {
        if (cache.size() >= maxEntries) {
            sweep();
        }
        cache.put(key, new CacheItem<>(value, System.currentTimeMillis() + expirationTimeMillis));
    }

    public void put(K key, V value) {
        put(key, value, defaultExpirationTimeMillis);
    }

    public Object get(K key) {
        CacheItem<V> item = cache.get(key);
        if (item != null && item.isValid()) {
            item.extendExpirationTime(extensionTimeMillis);
            return item.getValue();
        } else {
            remove(key);
            return null;
        }
    }

    public boolean containsKey(K key) {
        CacheItem<V> item = cache.get(key);
        return item != null && item.isValid();
    }

    public synchronized void sweep() {
        if (cache.size() < maxEntries) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        List<K> keysToRemove = new ArrayList<>();
        for (K key : cache.keySet()) {
            CacheItem<V> item = cache.get(key);
            if (!item.isValid() || currentTimeMillis >= item.getExpirationTimeMillis()) {
                keysToRemove.add(key);
            }
        }
        for (K keyToRemove : keysToRemove) {
            cache.remove(keyToRemove);
        }
    }

    public void remove(K key) {
        cache.remove(key);
    }

    @Data
    private static class CacheItem<V> {

        private final SoftReference<V> reference;

        private long expirationTimeMillis;

        public CacheItem(V value , long expirationTimeMillis) {
            this.reference = new SoftReference<>(value);
            this.expirationTimeMillis = expirationTimeMillis;
        }

        public V getValue() {
            return reference.get();
        }

        public boolean isValid() {
            return System.currentTimeMillis() < expirationTimeMillis && reference.get() != null;
        }

        public synchronized void extendExpirationTime(long extensionTimeMillis) {
            expirationTimeMillis += extensionTimeMillis;
        }
    }


}