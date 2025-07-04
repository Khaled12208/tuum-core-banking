#!/bin/bash

echo "=== Docker Swarm Status ==="
docker stack services tuum-banking

echo -e "\n=== Testing Main Service Health ==="
curl -s -X GET http://localhost:8083/api/v1/actuator/health | jq .

echo -e "\n=== Testing Account Creation ==="
curl -s -X POST http://localhost:8083/api/v1/accounts \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-swarm-$(date +%s)" \
  -d '{"customerId": "12345", "country": "EE", "currencies": ["EUR", "USD"]}' | jq .

echo -e "\n=== Testing Account Retrieval ==="
curl -s -X GET http://localhost:8083/api/v1/accounts/12345 | jq .

echo -e "\n=== Testing Transaction Creation ==="
curl -s -X POST http://localhost:8083/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-swarm-tx-$(date +%s)" \
  -d '{"accountId": "12345", "amount": 100, "currency": "EUR", "direction": "IN", "description": "Test transaction"}' | jq .

echo -e "\n=== Stack Tasks Status ==="
docker stack ps tuum-banking

echo -e "\n=== Running Containers ==="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 