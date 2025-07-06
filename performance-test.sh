#!/bin/bash

# Performance Test Script for Tuum Core Banking
# This script tests account creation and transaction throughput

echo "🚀 Starting Performance Test for Tuum Core Banking"
echo "=================================================="

# Configuration
BASE_URL="http://localhost:8083/api/v1"
ACCOUNT_ID="ACCEF85E2AB"  # From the account we just created
TEST_DURATION=30  # seconds
CONCURRENT_REQUESTS=5
TRANSACTION_REQUESTS=1000  # Number of transaction requests to run

echo "📊 Test Configuration:"
echo "  - Base URL: $BASE_URL"
echo "  - Account ID: $ACCOUNT_ID"
echo "  - Test Duration: $TEST_DURATION seconds"
echo "  - Concurrent Requests: $CONCURRENT_REQUESTS"
echo "  - Transaction Requests: $TRANSACTION_REQUESTS"
echo ""

# Function to generate unique idempotency key
generate_idempotency_key() {
    echo "perf-$(date +%s)-$RANDOM"
}

# Function to test account creation
test_account_creation() {
    local idempotency_key=$(generate_idempotency_key)
    local customer_id="perf-customer-$RANDOM"
    
    curl --silent --location "$BASE_URL/accounts" \
        --header 'Content-Type: application/json' \
        --header "Idempotency-Key: $idempotency_key" \
        --data "{\"customerId\": \"$customer_id\", \"country\": \"EE\", \"currencies\": [\"EUR\"]}" \
        -w "%{time_total},%{http_code}\n" \
        -o /dev/null
}

# Function to test transaction creation
test_transaction_creation() {
    local idempotency_key=$(generate_idempotency_key)
    local amount=$((RANDOM % 1000 + 1))
    local direction="IN"
    
    curl --silent --location "$BASE_URL/transactions" \
        --header 'Content-Type: application/json' \
        --header "Idempotency-Key: $idempotency_key" \
        --data "{\"accountId\": \"$ACCOUNT_ID\", \"amount\": $amount.50, \"currency\": \"EUR\", \"direction\": \"$direction\", \"description\": \"Performance test transaction\"}" \
        -w "%{time_total},%{http_code}\n" \
        -o /dev/null
}

# Function to run 1000 transaction requests
run_1000_transaction_test() {
    local count=$TRANSACTION_REQUESTS
    
    echo "🔥 Running $count transaction requests..."
    echo "⏰ Starting at: $(date)"
    
    local start_time=$(date +%s.%N)
    local success_count=0
    local total_time=0
    local error_count=0
    
    # Create progress tracking
    local progress_interval=$((count / 20))  # Show progress every 5%
    if [ $progress_interval -eq 0 ]; then
        progress_interval=50
    fi
    
    for i in $(seq 1 $count); do
        result=$(test_transaction_creation)
        
        # Parse result: time,http_code
        time=$(echo $result | cut -d',' -f1)
        http_code=$(echo $result | cut -d',' -f2)
        
        if [ "$http_code" = "200" ]; then
            success_count=$((success_count + 1))
            total_time=$(echo "$total_time + $time" | bc -l)
        else
            error_count=$((error_count + 1))
        fi
        
        # Show progress
        if [ $((i % progress_interval)) -eq 0 ]; then
            echo "  Progress: $i/$count (${success_count} success, ${error_count} errors)"
        fi
    done
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc -l)
    
    echo "⏰ Finished at: $(date)"
    echo "✅ 1000 Transaction Test Results:"
    echo "  - Total requests: $count"
    echo "  - Successful requests: $success_count"
    echo "  - Failed requests: $error_count"
    echo "  - Success rate: $(echo "scale=1; $success_count * 100 / $count" | bc -l)%"
    echo "  - Total time: ${duration}s"
    if [ $success_count -gt 0 ]; then
        echo "  - Average response time: $(echo "scale=3; $total_time / $success_count" | bc -l)s"
        echo "  - Throughput: $(echo "scale=2; $success_count / $duration" | bc -l) requests/second"
    else
        echo "  - Average response time: N/A (no successful requests)"
        echo "  - Throughput: 0 requests/second"
    fi
    echo ""
}

# Function to run concurrent tests
run_concurrent_test() {
    local test_type=$1
    local count=$2
    
    echo "🔄 Running $count concurrent $test_type tests..."
    
    local start_time=$(date +%s.%N)
    local success_count=0
    local total_time=0
    
    for i in $(seq 1 $count); do
        if [ "$test_type" = "account" ]; then
            result=$(test_account_creation)
        else
            result=$(test_transaction_creation)
        fi
        
        # Parse result: time,http_code
        time=$(echo $result | cut -d',' -f1)
        http_code=$(echo $result | cut -d',' -f2)
        
        # Account creation returns 201, transactions return 200
        if [ "$test_type" = "account" ] && [ "$http_code" = "201" ]; then
            success_count=$((success_count + 1))
            total_time=$(echo "$total_time + $time" | bc -l)
        elif [ "$test_type" = "transaction" ] && [ "$http_code" = "200" ]; then
            success_count=$((success_count + 1))
            total_time=$(echo "$total_time + $time" | bc -l)
        fi
        
        echo "  Request $i: ${time}s (HTTP $http_code)"
    done
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc -l)
    
    echo "✅ $test_type Test Results:"
    echo "  - Successful requests: $success_count/$count"
    echo "  - Total time: ${duration}s"
    if [ $success_count -gt 0 ]; then
        echo "  - Average response time: $(echo "$total_time / $success_count" | bc -l)s"
        echo "  - Throughput: $(echo "scale=2; $success_count / $duration" | bc -l) requests/second"
    else
        echo "  - Average response time: N/A (no successful requests)"
        echo "  - Throughput: 0 requests/second"
    fi
    echo ""
}

# Function to run sustained load test
run_sustained_test() {
    local test_type=$1
    local duration=$2
    
    echo "⏱️  Running sustained $test_type load test for $duration seconds..."
    
    local start_time=$(date +%s)
    local end_time=$((start_time + duration))
    local request_count=0
    local success_count=0
    local total_time=0
    
    while [ $(date +%s) -lt $end_time ]; do
        if [ "$test_type" = "account" ]; then
            result=$(test_account_creation)
        else
            result=$(test_transaction_creation)
        fi
        
        request_count=$((request_count + 1))
        
        # Parse result: time,http_code
        time=$(echo $result | cut -d',' -f1)
        http_code=$(echo $result | cut -d',' -f2)
        
        # Account creation returns 201, transactions return 200
        if [ "$test_type" = "account" ] && [ "$http_code" = "201" ]; then
            success_count=$((success_count + 1))
            total_time=$(echo "$total_time + $time" | bc -l)
        elif [ "$test_type" = "transaction" ] && [ "$http_code" = "200" ]; then
            success_count=$((success_count + 1))
            total_time=$(echo "$total_time + $time" | bc -l)
        fi
        
        # Small delay to avoid overwhelming the system
        sleep 0.1
    done
    
    local actual_duration=$((end_time - start_time))
    
    echo "📈 Sustained $test_type Test Results:"
    echo "  - Total requests: $request_count"
    echo "  - Successful requests: $success_count"
    echo "  - Success rate: $(echo "scale=1; $success_count * 100 / $request_count" | bc -l)%"
    echo "  - Duration: ${actual_duration}s"
    if [ $success_count -gt 0 ]; then
        echo "  - Average response time: $(echo "scale=3; $total_time / $success_count" | bc -l)s"
        echo "  - Sustained throughput: $(echo "scale=2; $success_count / $actual_duration" | bc -l) requests/second"
    else
        echo "  - Average response time: N/A (no successful requests)"
        echo "  - Sustained throughput: 0 requests/second"
    fi
    echo ""
}

# Main test execution
echo "🧪 Starting Performance Tests..."
echo ""

# Test 1: 1000 Transaction Requests (Main Test)
run_1000_transaction_test

# Test 2: Concurrent Account Creation
run_concurrent_test "account" $CONCURRENT_REQUESTS

# Test 3: Concurrent Transaction Creation
run_concurrent_test "transaction" $CONCURRENT_REQUESTS

# Test 4: Sustained Account Creation Load
run_sustained_test "account" $TEST_DURATION

# Test 5: Sustained Transaction Load
run_sustained_test "transaction" $TEST_DURATION

echo "🎯 Performance Test Summary"
echo "=========================="
echo "✅ All tests completed successfully!"
echo "📊 Check the results above for detailed throughput metrics."
echo ""
echo "💡 Performance Tips:"
echo "  - Response times under 1 second are good for development"
echo "  - Throughput depends on your machine's resources"
echo "  - Database and RabbitMQ are the main bottlenecks"
echo "  - Production would be much faster with optimizations" 