package org.devexperts.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.devexperts.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Konstantine Vashalomidze
 * All http requests will go through this first to authorize it's source. This class uses LRU cache, sacrificing memory
 * over speed. This class contains custom classes 'LRUCache' and 'CacheEntry' for this prupose, every 1000 client's
 * tokens will be stored in our cache. No longer necessary to extract token and do this costly operations on every
 * subsequent call.
 */

// TODO: rate limiting must be done.
@Component
public class JwtInterceptor
        implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);
    private final Map<String, CacheEntry> tokenCache;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${jwt.cache.size:1000}")
    private int cacheSize;
    @Value("${jwt.cache.expiration:3600000}")
    private long cacheExpiration;

    public JwtInterceptor() {
        this.tokenCache = Collections.synchronizedMap(new LinkedHashMap<>(cacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > cacheSize;
            }
        });
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        String token = extractToken(request);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String username = validateToken(token);
        if (username != null) {
            request.setAttribute("username", username);
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String validateToken(String token) {
        synchronized (tokenCache) {
            CacheEntry entry = tokenCache.get(token);
            if (entry != null && !entry.isExpired()) {
                logger.debug("Cache hit for token");
                return entry.username();
            }
        }

        String username = jwtUtil.extractUsername(token);
        if (username != null && jwtUtil.validateToken(token, username)) {
            synchronized (tokenCache) {
                tokenCache.put(token, new CacheEntry(username, cacheExpiration));
            }
            logger.debug("Token validated and cached");
            return username;
        }

        logger.debug("Token validation failed");
        return null;
    }

    @Scheduled(fixedRateString = "${jwt.cache.cleanup.interval:3600000}")
    public void clearExpiredEntries() {
        synchronized (tokenCache) {
            tokenCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
        logger.debug("Expired cache entries cleared");
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