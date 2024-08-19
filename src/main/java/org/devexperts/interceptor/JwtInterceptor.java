package org.devexperts.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.devexperts.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    private TokenCache tokenCache;

    private TokenExtractor tokenExtractor;

    public JwtInterceptor(TokenCache tokenCache)
    {
        tokenExtractor = new HttpTokenExtractor();
        this.tokenCache = tokenCache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        String token = tokenExtractor.extractToken(request);

        if (token != null)
        {
            String username = tokenCache.validateToken(token);
            if (username != null) {
                request.setAttribute("username", username);
                return true;
            }
        }

        return false;
    }


}