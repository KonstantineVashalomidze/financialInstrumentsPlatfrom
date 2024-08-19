package org.devexperts.interceptor;

public interface TokenExtractor {
    String extractToken(Object request);
}
