package com.winestoreapp.common.ratelimit;

public record RateLimitResult(boolean allowed, long currentCount) {
}
