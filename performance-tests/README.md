# 🚀 Performance Testing for Stoic Winery API

## 📋 What to Do

### 1. Prepare Test Data

#### Get JWT token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

Save the token from the response (`token` field).

#### Get User ID and Wine ID:
```bash
# Get current user
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get list of wines
curl -X GET http://localhost:8080/api/wines
```

### 2. Configure JMeter Test

1. **Open JMeter**
2. **Load file:** `performance-tests/jmeter/winery-api-test.jmx`
3. **Edit variables** (find `Test Plan` → `User Defined Variables`):
   - `BASE_URL` — your URL (e.g. `http://localhost:8080`)
   - `JWT_TOKEN` — token from step 1
   - `USER_ID` — user ID
   - `WINE_ID` — wine ID

### 3. Run the Test

1. In JMeter click **Run** (green triangle) or `Ctrl+R`
2. Wait for test completion (~2-3 minutes)
3. Check **Summary Report**

### 4. Record Results

In **Summary Report** you need these columns:

| Metric | Description | Target |
|---------|------|------|
| **Average** | Average response time | < 100ms |
| **Median** | Median (50th percentile) | < 100ms |
| **90% Line** | 90% of requests faster than this | < 200ms |
| **95% Line** | 95% of requests faster than this | < 300ms |
| **Error %** | Percentage of errors | 0% |
| **Throughput** | Requests per second | > 50/sec |

### 5. Save Results

Results are automatically saved to:
```
performance-tests/results/winery-api-results-YYYYMMDD-HHMMSS.jtl
```

To export to CSV:
1. In Summary Report click **Configure**
2. Select fields to export
3. Click **Save Table As...** → select CSV

---

## 📊 Expected Results

After adding indexes, expect:

| Endpoint | Average | 90% Line | Throughput |
|----------|---------|----------|------------|
| GET /api/wines/{id} | 50-100ms | < 150ms | 100+ req/sec |
| GET /api/orders/user/{id} | 80-150ms | < 200ms | 50+ req/sec |
| GET /api/reviews/wine/{id} | 50-100ms | < 150ms | 100+ req/sec |
| POST /api/auth/login | 100-200ms | < 300ms | 50+ req/sec |

---

## 🔧 Additional Commands

### Run from command line:
```bash
jmeter -n -t performance-tests/jmeter/winery-api-test.jmx \
  -l performance-tests/results/results.jtl \
  -e -o performance-tests/report
```

### Generate HTML report:
```bash
jmeter -g performance-tests/results/results.jtl \
  -o performance-tests/html-report
```

---

## ✅ Checklist

- [ ] Got JWT token
- [ ] Configured variables in JMeter
- [ ] Ran the test
- [ ] Recorded Average response time
- [ ] Recorded Throughput
- [ ] Saved results
- [ ] Updated VALIDATION_STATUS.md
