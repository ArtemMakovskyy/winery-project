package com.winestoreapp.common.ratelimit;

public interface RateLimitService {

    RateLimitResult check(String key, int maxRequests, int windowSeconds);
}
