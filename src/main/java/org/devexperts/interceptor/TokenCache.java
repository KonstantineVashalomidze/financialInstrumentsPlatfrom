package org.devexperts.interceptor;

import org.devexperts.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TokenCache
{
    @Value("${jwt.cache.size:1000}")
    private int cacheSize;
    @Value("${jwt.cache.expiration:3600000}")
    private long cacheExpiration;
    private JwtUtil jwtUtil;
    private Map<String, CacheEntry> tokenCache;


    public TokenCache(JwtUtil jwtUtil)
    {
        this.jwtUtil = jwtUtil;
        this.tokenCache = Collections.synchronizedMap(new LinkedHashMap<>(cacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > cacheSize;
            }
        });
    }

    public String validateToken(String token) {
        synchronized (tokenCache) {
            CacheEntry entry = tokenCache.get(token);
            if (entry != null && !entry.isExpired()) {
                return entry.username();
            }
        }

        String username = jwtUtil.extractUsername(token);
        if (username != null && jwtUtil.validateToken(token, username)) {
            synchronized (tokenCache) {
                tokenCache.put(token, new CacheEntry(username, cacheExpiration));
            }
            return username;
        }

        return null;
    }


    /**
     * Cache entry which is pair, username and expiration time, username is the username of the user of which
     * the corresponding jwt key belongs to. expiration time is set to 1 hour for every token, in every 1 hour they
     * will be deleted from cache.
     */
    private record CacheEntry(String username, long expirationTime) {

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }



}
