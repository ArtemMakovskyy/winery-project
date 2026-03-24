package com.winestoreapp.common.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * Maximum number of requests allowed within the time window.
     * Default is 10 requests.
     *
     * @return max requests per time window
     */
    int maxRequests() default 10;

    /**
     * Time window in seconds for rate limiting.
     * Default is 60 seconds (1 minute).
     *
     * @return time window in seconds
     */
    int timeWindowSeconds() default 60;

    /**
     * Custom key for rate limiting.
     * Can use Spring EL expressions (e.g., "#userId", "#request.getHeader('X-API-Key')").
     * If empty, uses client IP address as the key.
     *
     * @return custom key expression
     */
    String key() default "";
}
