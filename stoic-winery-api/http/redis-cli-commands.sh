# ==========================================
# Redis CLI Commands for Testing
# ==========================================
# Usage: Copy and paste these commands into terminal
# ==========================================

# ==========================================
# BASIC SETUP
# ==========================================

# Set Redis password alias (run once per session)
export REDIS_PASS="dev_redis_password_change_me"

# Get container name
docker ps --filter "name=redis"

# ==========================================
# CONNECT TO REDIS
# ==========================================

# Connect to Redis CLI
docker exec -it winery-project-redis-1 redis-cli -a $REDIS_PASS

# Or with full authentication
docker exec -it winery-project-redis-1 redis-cli -a dev_redis_password_change_me

# ==========================================
# VIEW ALL KEYS
# ==========================================

# Show all keys
KEYS *

# Count all keys
DBSIZE

# ==========================================
# WINE CACHE
# ==========================================

# Show all wine cache keys
KEYS wines:*

# Count wine cache keys
KEYS wines:* | wc -l

# View specific wine cache entry
GET wines::1

# View wine with specific ID
GET wines::5

# Check TTL (time to live) for wine cache
TTL wines::1

# View all wine cache with pattern
KEYS wines::all-*

# ==========================================
# RATE LIMITING
# ==========================================

# Show all rate limit keys
KEYS rate_limit:*

# Count rate limit keys
KEYS rate_limit:* | wc -l

# View rate limit counter value
GET rate_limit:127.0.0.1:HealthCheckController:rateLimitedEndpoint

# Check TTL for rate limit key
TTL rate_limit:127.0.0.1:HealthCheckController:rateLimitedEndpoint

# ==========================================
# JWT BLACKLIST
# ==========================================

# Show all blacklist keys
KEYS blacklist:*

# Count blacklist keys
KEYS blacklist:* | wc -l

# View blacklisted token (shows "revoked")
GET blacklist:<token-here>

# Check TTL for blacklisted token
TTL blacklist:<token-here>

# ==========================================
# DETAILED INFO
# ==========================================

# Get key type
TYPE wines::1

# Get key info (memory, TTL, etc)
MEMORY USAGE wines::1

# Scan keys (better than KEYS for production)
SCAN 0 MATCH wines:* COUNT 100

# ==========================================
# CLEAR DATA
# ==========================================

# Delete specific key
DEL wines::1

# Delete all wine cache keys (pattern)
redis-cli -a $REDIS_PASS KEYS "wines:*" | xargs redis-cli -a $REDIS_PASS DEL

# Delete all rate limit keys
redis-cli -a $REDIS_PASS KEYS "rate_limit:*" | xargs redis-cli -a $REDIS_PASS DEL

# Delete all blacklist keys
redis-cli -a $REDIS_PASS KEYS "blacklist:*" | xargs redis-cli -a $REDIS_PASS DEL

# Flush all data (DANGER!)
FLUSHALL

# ==========================================
# MONITORING
# ==========================================

# Monitor all Redis commands in real-time
MONITOR

# Watch slow queries
SLOWLOG GET 10

# Get Redis info
INFO

# Get memory info
INFO memory

# Get stats
INFO stats

# ==========================================
# ONE-LINER COMMANDS (Quick Check)
# ==========================================

# Quick check: Show all keys
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "*"

# Quick check: Count wine cache entries
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "wines:*" | wc -l

# Quick check: Show wine cache keys
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "wines:*"

# Quick check: Show rate limit keys
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "rate_limit:*"

# Quick check: Show blacklist keys
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "blacklist:*"

# Quick check: View first wine cache entry
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS GET "wines::1"

# Quick check: View rate limit counter
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS GET "rate_limit:127.0.0.1:HealthCheckController:rateLimitedEndpoint"

# ==========================================
# TESTING WORKFLOW
# ==========================================

# 1. Clear all cache before testing
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS FLUSHALL

# 2. Make API request to /api/wines
curl http://localhost:8080/api/wines

# 3. Check what's cached
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "wines:*"

# 4. Make same request again (should use cache)
curl http://localhost:8080/api/wines

# 5. Check logs for cache hit
docker logs winery-project-api-backend-1 | grep -i cache

# ==========================================
# RATE LIMIT TESTING
# ==========================================

# Clear rate limits
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "rate_limit:*" | xargs docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS DEL

# Make 6 quick requests (6th should fail)
for i in {1..6}; do echo "Request $i:"; curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/api/health/rate-limited; done

# Check rate limit keys after requests
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS KEYS "rate_limit:*"

# View counter value
docker exec winery-project-redis-1 redis-cli -a $REDIS_PASS GET "rate_limit:127.0.0.1:HealthCheckController:rateLimitedEndpoint"
