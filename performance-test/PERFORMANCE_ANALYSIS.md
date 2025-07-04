# Performance Analysis - Tuum Core Banking System

## 🚀 Throughput Estimation

### **Estimated Transaction Throughput: 50-200 TPS**

Based on the current system architecture and configuration, here's a detailed breakdown of expected performance:

## 📊 System Architecture Performance Factors

### 1. **Database Layer (PostgreSQL)**

- **Max Connections**: 200
- **Shared Buffers**: 512MB
- **Work Memory**: 8MB per connection
- **Effective Cache Size**: 2GB
- **Expected DB Throughput**: 500-1000 TPS (with proper indexing)

### 2. **Message Broker (RabbitMQ)**

- **Queue TTL**: 5 minutes
- **Max Queue Length**: 1000 messages
- **Consumer Retry**: 3 attempts
- **Expected RabbitMQ Throughput**: 1000-5000 messages/second

### 3. **Application Services**

- **Spring Boot with embedded Tomcat**
- **Default connection pool**: HikariCP
- **Default thread pool**: 200 threads
- **Expected Service Throughput**: 100-500 requests/second

## 🔍 Performance Bottlenecks Analysis

### Current Bottlenecks:

1. **Synchronous Processing Pattern**

   ```
   Client → API → RabbitMQ → Consumer → Database → Response
   ```

   - Each transaction waits for full processing cycle
   - 30-second timeout per transaction
   - Blocking until consumer completes

2. **Database Operations**

   - Balance updates with optimistic locking
   - Transaction inserts with constraints
   - Idempotency checks
   - Processed message tracking

3. **Message Processing**

   - Single consumer instance
   - JSON serialization/deserialization
   - WebSocket notifications

4. **Network Latency**
   - Docker container communication
   - Localhost networking overhead

## 📈 Performance Test Results

### Test Configuration:

- **Concurrent Requests**: 10
- **Total Requests**: 100 per test
- **Transaction Types**: Small (€10), Medium (€100), Large (€1000)
- **Directions**: IN (deposits), OUT (withdrawals)

### Expected Results:

| Transaction Type | Expected TPS | Response Time | Success Rate |
| ---------------- | ------------ | ------------- | ------------ |
| Small IN (€10)   | 80-120       | 200-500ms     | 95-99%       |
| Small OUT (€5)   | 60-100       | 300-800ms     | 90-95%       |
| Medium IN (€100) | 70-110       | 250-600ms     | 95-99%       |
| Large IN (€1000) | 50-80        | 400-1000ms    | 95-99%       |

## 🛠️ Performance Optimization Recommendations

### 1. **Database Optimizations**

```sql
-- Add composite indexes for better query performance
CREATE INDEX CONCURRENTLY idx_transactions_account_currency_created
ON transactions(account_id, currency, created_at);

CREATE INDEX CONCURRENTLY idx_balances_account_currency_version
ON balances(account_id, currency, version_number);

-- Partition large tables by date
CREATE TABLE transactions_2024 PARTITION OF transactions
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
```

### 2. **Connection Pool Optimization**

```yaml
# Add to application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 3. **RabbitMQ Consumer Scaling**

```yaml
# Add to consumer application.yml
spring:
  rabbitmq:
    listener:
      simple:
        concurrency: 5
        max-concurrency: 10
        prefetch: 10
```

### 4. **Application-Level Optimizations**

```java
// Enable async processing
@Async
public CompletableFuture<TransactionResult> processTransactionAsync(TransactionRequest request) {
    // Process transaction asynchronously
}

// Add caching for frequently accessed data
@Cacheable("account-balances")
public Balance getAccountBalance(String accountId, String currency) {
    // Cache balance lookups
}
```

## 🔧 Performance Testing

### Run Performance Test:

```bash
# Make script executable
chmod +x performance-test.sh

# Run performance test
./performance-test.sh
```

### Manual Testing:

```bash
# Test single transaction
curl -X POST http://localhost:8084/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-$(date +%s)" \
  -d '{
    "accountId": "YOUR_ACCOUNT_ID",
    "amount": 100.00,
    "currency": "EUR",
    "direction": "IN",
    "description": "Performance test"
  }'

# Monitor system resources
docker stats
```

## 📊 Monitoring and Metrics

### Key Performance Indicators:

1. **Transaction Throughput (TPS)**

   - Target: 100+ TPS
   - Monitor: Response times and success rates

2. **Database Performance**

   - Target: < 100ms average query time
   - Monitor: Connection pool usage, slow queries

3. **Message Queue Performance**

   - Target: < 1 second message processing
   - Monitor: Queue depth, consumer lag

4. **System Resources**
   - CPU: < 80% utilization
   - Memory: < 70% utilization
   - Disk I/O: < 80% utilization

### Monitoring Commands:

```bash
# Check database performance
docker exec tuum-postgres psql -U tuum_user -d tuum_banking -c "
SELECT
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats
WHERE tablename IN ('transactions', 'balances', 'accounts')
ORDER BY tablename, attname;"

# Check RabbitMQ queue status
curl -s -u tuum_user:tuum_password http://localhost:15672/api/queues/%2F/transactions-events-queue | jq '.'

# Check application metrics
curl http://localhost:8084/actuator/metrics/http.server.requests
```

## 🚀 Scaling Strategies

### 1. **Horizontal Scaling**

- Deploy multiple consumer instances
- Use load balancer for API service
- Implement database read replicas

### 2. **Vertical Scaling**

- Increase JVM heap size
- Optimize database configuration
- Add more CPU/memory resources

### 3. **Architecture Improvements**

- Implement event sourcing
- Add message persistence
- Use database sharding for large datasets

## 📋 Performance Checklist

### Before Production:

- [ ] Run performance tests with realistic load
- [ ] Optimize database indexes
- [ ] Configure connection pools
- [ ] Set up monitoring and alerting
- [ ] Implement circuit breakers
- [ ] Add rate limiting
- [ ] Configure auto-scaling policies

### Ongoing Monitoring:

- [ ] Track transaction throughput
- [ ] Monitor response times
- [ ] Check error rates
- [ ] Monitor resource utilization
- [ ] Review slow queries
- [ ] Analyze queue depths

## 🎯 Performance Targets

### Development Environment:

- **Throughput**: 50-200 TPS
- **Response Time**: < 1 second
- **Success Rate**: > 95%

### Production Environment (Optimized):

- **Throughput**: 500-2000 TPS
- **Response Time**: < 500ms
- **Success Rate**: > 99.9%

## 📝 Conclusion

The current Tuum Core Banking System is designed for **moderate throughput** with a focus on **data consistency** and **reliability**. The estimated throughput of **50-200 TPS** is suitable for:

- Small to medium-sized banks
- Development and testing environments
- Proof of concept implementations

For higher throughput requirements, the system can be optimized using the recommendations above, potentially reaching **500-2000 TPS** in a production environment with proper scaling and optimization.

The performance test script (`performance-test.sh`) provides a baseline measurement that can be used to validate these estimates and identify specific bottlenecks in your environment.
