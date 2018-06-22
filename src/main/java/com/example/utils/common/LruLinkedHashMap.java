package com.example.utils.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支持LRU算法的LinkedHashMap
 *
 * @author chen.qian
 * @param <K> key
 * @param <V> value
 */
public class LruLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int CACHE_SIZE;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    public LruLinkedHashMap(int cacheSize) {
        super((int) Math.ceil(cacheSize / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, true);
        CACHE_SIZE = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > CACHE_SIZE;
    }

}
