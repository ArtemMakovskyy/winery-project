# 🔴 Redis Integration

Redis is used in Stoic Winery API for caching, rate limiting, and JWT blacklist storage.

---

## 📖 Table of Contents

- [Architecture](#architecture)
- [Configuration](#configuration)
- [Caching](#caching)
- [Rate Limiting](#rate-limiting)
- [JWT Blacklist](#jwt-blacklist)
- [Debug Endpoints](#debug-endpoints)
    - [Main Endpoints](#main-endpoints)
    - [Example Requests](#example-requests)
- [Testing](#testing)
- [Redis CLI](#redis-cli)
- [Monitoring](#monitoring)
- [HTTP Requests](#http-requests)

---

## 🏗️ Architecture

Redis is integrated into three main components:

| Component         | Purpose            | Implementation              |
|-------------------|--------------------|-----------------------------|
| **Cache**         | Wine caching       | `@Cacheable`, `@CacheEvict` |
| **Rate Limit**    | Request throttling | Lua scripts + Redis         |
| **JWT Blacklist** | Token revocation   | Redis Set storage           |

---

## ⚙️ Configuration

### Docker Compose

Redis is added to `docker-compose.yaml`:

```yaml
redis:
  image: redis:7-alpine
  container_name: redis
  ports:
    - "6379:6379"
  volumes:
    - redis-data:/data
  command: redis-server --appendonly yes
```

### Environment Variables

```env
# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=dev_redis_password_change_me
REDIS_DATABASE=0

# Redis Blacklist
REDIS_BLACKLIST_KEY=jwt_blacklist
REDIS_BLACKLIST_TTL=86400
```

### Application Properties

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:dev_redis_password_change_me}
      database: ${REDIS_DATABASE:0}
  cache:
    type: redis
    redis:
      time-to-live: 3600000 # 1 hour
      cache-null-values: false
```

---

## 💾 Caching

### WineService Cache

Caching is used to reduce database load:

```java

@Cacheable(value = "wines", key = "#id")
public WineDto getWineById(Long id) {
    // Database query
}

@CacheEvict(value = "wines", key = "#id")
public void updateWine(Long id, WineDto dto) {
    // Update logic
}

@CacheEvict(value = "wines", allEntries = true)
public void deleteWine(Long id) {
    // Delete logic
}
```

### Cache Names

| Cache Name | TTL | Description |
|------------|-----|-------------|
| `wines`    | 1h  | Wine cache  |

### Clear Cache

Debug endpoint for forced cache clearing:

```bash
POST http://localhost:8080/api/redis-debug/cache/clear
```

---

## 🚦 Rate Limiting

### Configuration

```yaml
rate-limit:
  default-requests: 100
  default-window-seconds: 60
  enabled: true
```

### Lua Script

Rate limiting is implemented via Lua script for atomicity:

```lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local current = redis.call('INCR', key)

if current == 1 then
    redis.call('EXPIRE', key, window)
end

if current > limit then
    return 0
end

return current
```

### Rate Limited Endpoints

| Endpoint             | Limit | Window |
|----------------------|-------|--------|
| `/api/auth/login`    | 5     | 60s    |
| `/api/auth/register` | 3     | 60s    |
| `/api/orders`        | 10    | 60s    |
| `/api/reviews`       | 5     | 60s    |
| `/api/users/**`      | 10    | 60s    |

### RateLimit Annotation

```java

@RateLimit(requests = 5, windowSeconds = 60)
@PostMapping("/login")
public AuthResponse login(@RequestBody LoginRequest request) {
    // ...
}
```

---

## 🚫 JWT Blacklist

### Configuration

```yaml
redis:
  blacklist:
    key: jwt_blacklist
    ttl: 86400 # 24 hours
```

### TokenBlacklistService

```java
public void blacklistToken(String token, long expirationTime) {
    long ttl = expirationTime - System.currentTimeMillis();
    if (ttl > 0) {
        redisTemplate.opsForSet().add(blacklistKey, token);
        redisTemplate.expire(blacklistKey, ttl, TimeUnit.MILLISECONDS);
    }
}

public boolean isBlacklisted(String token) {
    return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(blacklistKey, token));
}
```

### Logout Flow

1. User sends `POST /api/auth/logout`
2. Token is added to blacklist
3. Token is stored until its natural expiration

---

## 🐛 Debug Endpoints

### Main Endpoints

| Endpoint                           | Method | Description                                         |
|------------------------------------|--------|-----------------------------------------------------|
| `/api/redis/info`                  | GET    | General Redis information                           |
| `/api/redis/keys`                  | GET    | All keys in Redis                                   |
| `/api/redis/keys/pattern?pattern=` | GET    | Keys by pattern                                     |
| `/api/redis/stats`                 | GET    | Cache statistics                                    |
| `/api/redis/stats/extended`        | GET    | Extended statistics (memory, hit/miss rate, uptime) |
| `/api/redis/value?key=`            | GET    | Get value by key                                    |
| `/api/redis/key-info?key=`         | GET    | Get detailed key information                        |
| `/api/redis/ttl?key=`              | GET    | Get key TTL                                         |
| `/api/redis/key?key=`              | DELETE | Delete key                                          |
| `/api/redis/blacklist/count`       | GET    | Number of tokens in blacklist                       |
| `/api/redis/cache/clear`           | POST   | Clear cache                                         |

### Example Response `/api/redis/info`

```json
{
  "redisVersion": "7.0.0",
  "connectedClients": 5,
  "usedMemory": "1.2M",
  "uptimeSeconds": 3600,
  "totalKeys": 150,
  "blacklistSize": 25
}
```

### Example Request `/api/redis/stats/extended`

```bash
GET http://localhost:8080/api/redis/stats/extended
Authorization: Bearer {{auth_token}}
```

### Example Response `/api/redis/stats/extended`

```json
{
  "connectedClients": 5,
  "usedMemory": "1.2M",
  "usedMemoryPeak": "2.4M",
  "usedMemoryHuman": "1.2M",
  "uptimeSeconds": 3600,
  "totalKeys": 150,
  "hitRate": 0.85,
  "missRate": 0.15,
  "hitCount": 850,
  "missCount": 150,
  "keyspaceHits": 850,
  "keyspaceMisses": 150
}
```

### Example Request `/api/redis/keys/pattern`

```bash
GET http://localhost:8080/api/redis/keys/pattern?pattern=wines::*
Authorization: Bearer {{auth_token}}
```

### Example Response `/api/redis/keys/pattern`

```json
[
  "wines::1",
  "wines::2",
  "wines::3"
]
```

### Example Request `/api/redis/value`

```bash
GET http://localhost:8080/api/redis/value?key=wines::1
Authorization: Bearer {{auth_token}}
```

### Example Request `/api/redis/key-info`

```bash
GET http://localhost:8080/api/redis/key-info?key=wines::1
Authorization: Bearer {{auth_token}}
```

### Example Response `/api/redis/key-info`

```json
{
  "key": "wines::1",
  "type": "string",
  "encoding": "embstr",
  "size": 256,
  "ttl": -1
}
```

### Example Request `/api/redis/ttl`

```bash
GET http://localhost:8080/api/redis/ttl?key=wines::1
Authorization: Bearer {{auth_token}}
```

### Example Response `/api/redis/ttl`

```json
{
  "key": "wines::1",
  "ttl": -1
}
```

### Example Request `/api/redis/key` (DELETE)

```bash
DELETE http://localhost:8080/api/redis/key?key=wines::1
Authorization: Bearer {{auth_token}}
```

---

## 🧪 Testing

### Testcontainers

Integration tests use Testcontainers for Redis:

```java

@Testcontainers
class RateLimitServiceIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

### Test Classes

| Class                             | Purpose             |
|-----------------------------------|---------------------|
| `RateLimitServiceIntegrationTest` | Rate limiting tests |
| `TokenBlacklistServiceTest`       | JWT blacklist tests |
| `WineServiceCacheTest`            | Caching tests       |

### Running Tests

```bash
# All tests
mvn test

# Only integration tests
mvn test -Dtest=RateLimitServiceIntegrationTest

# Redis tests
mvn test -Dtest=*Redis*
```

---

## 🔧 Redis CLI

### Connect

```bash
# Docker container
docker exec -it redis redis-cli

# Local Redis
redis-cli -h localhost -p 6379
```

### Useful Commands

```bash
# Check all keys
KEYS *

# Check key type
TYPE wine::1

# View value
GET cache::wines::1

# View blacklist
SMEMBERS jwt_blacklist

# Count total keys
DBSIZE

# Clear database
FLUSHDB

# Real-time monitoring
MONITOR

# Statistics
INFO

# Memory statistics
INFO memory

# Check key TTL
TTL jwt_blacklist
```

### Examples for Stoic Winery

```bash
# View all cache keys
KEYS cache::*

# View all wine keys
KEYS wines::*

# View all rate limit keys
KEYS ratelimit::*

# View blacklist tokens
SMEMBERS jwt_blacklist

# Clear wine cache
DEL wines::1 wines::2

# Check rate limit statistics
GET ratelimit:/api/auth/login:192.168.1.1

# Get key type
TYPE wines::1

# Get key TTL
TTL wines::1

# Get memory info
INFO memory

# Get keys count
DBSIZE
```

---

## 📊 Monitoring

### Metrics

Redis metrics available via Prometheus:

```
redis_connected_clients
redis_used_memory_bytes
redis_keys_total
redis_hit_rate
redis_miss_rate
```

### Grafana Dashboard

1. Open Grafana: `http://localhost:3030`
2. Dashboard → Redis Metrics
3. View real-time metrics

---

## 📝 HTTP Requests

For testing Redis endpoints, use the `http/redis.http` file in IntelliJ IDEA:

1. Open `stoic-winery-api/http/redis.http`
2. Select `development` environment (top right corner)
3. Run `redis_login` request first to get the token
4. Run any Redis debug endpoint

Available requests:

- `redis_login` — Login and get JWT token
- `redis_stats` — Get basic cache statistics
- `redis_stats_extended` — Get extended statistics (memory, hit/miss rate)
- `redis_all_keys` — Get all Redis keys
- `redis_keys_pattern` — Get keys by pattern (e.g., `wines::*`)
- `redis_value` — Get value by key
- `redis_key_info` — Get detailed key information
- `redis_ttl` — Get key TTL
- `redis_delete_key` — Delete a key

