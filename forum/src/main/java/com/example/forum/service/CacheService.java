package com.example.forum.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface CacheService {
    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    Object get(String key);

    boolean hasKey(String key);

    void delete(String key);

    long increment(String key);

    void setExpire (String key, long timeout, TimeUnit unit);

    long addToSet(String key,String value);

    long removeFromSet(String key,String value);

    long getSetSize(String key);

    Set<Object> getSetMembers(String key);
}
