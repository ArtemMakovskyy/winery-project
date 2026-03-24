package com.winestoreapp.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redis.blacklist")
public class RedisBlacklistProperties {

    /**
     * Prefix for Redis blacklist keys.
     * Keys are stored as: "blacklist:{token}"
     */
    private String prefix = "blacklist:";

    /**
     * Default TTL in seconds for blacklisted tokens.
     * Should match JWT expiration time.
     */
    private long defaultTtl = 3600;
}
