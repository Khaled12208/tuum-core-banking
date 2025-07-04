# Docker Swarm Setup for Tuum Core Banking

## Overview

This document describes the Docker Swarm deployment configuration for the Tuum Core Banking system, which consists of:

- **fs-accounts-service**: Main API service (2 replicas)
- **cs-accounts-events-consumer**: Event processing service (2 replicas)
- **postgres**: PostgreSQL database (1 replica)
- **rabbitmq**: RabbitMQ message broker (1 replica)

## Architecture

```
┌─────────────────┐    ┌─────────────────────┐    ┌─────────────────┐
│   Load Balancer │    │  Docker Swarm       │    │   PostgreSQL    │
│   (Port 8083)   │───▶│  - 2x fs-accounts   │───▶│   (Port 5432)   │
└─────────────────┘    │  - 2x consumer      │    └─────────────────┘
                       │  - 1x RabbitMQ      │
                       │  - 1x PostgreSQL    │    ┌─────────────────┐
                       └─────────────────────┘───▶│   RabbitMQ      │
                                                  │   (Port 5672)   │
                                                  └─────────────────┘
```

## Prerequisites

- Docker Desktop with Swarm mode enabled
- Docker Compose v3.8+
- At least 2GB available memory
- Ports 8083, 8084, 5432, 5672, 15672 available

## Deployment

### 1. Initialize Docker Swarm (if not already done)

```bash
docker swarm init
```

### 2. Build Docker Images

```bash
# Build the main service
docker build -t tuum-fs-accounts-service:latest -f fs-accounts-service/Dockerfile .

# Build the consumer service
docker build -t tuum-cs-accounts-events-consumer:latest -f cs-accounts-events-consumer/Dockerfile .
```

### 3. Deploy the Stack

```bash
docker stack deploy -c docker-stack.yml tuum-banking
```

### 4. Monitor Deployment

```bash
# Check service status
docker stack services tuum-banking

# Check task status
docker stack ps tuum-banking

# View logs
docker service logs tuum-banking_fs-accounts-service
docker service logs tuum-banking_cs-accounts-events-consumer
```

## Configuration Details

### Service Configuration

#### fs-accounts-service

- **Replicas**: 2
- **Port**: 8083
- **Context Path**: `/api/v1`
- **Health Check**: `http://localhost:8083/api/v1/actuator/health`
- **Resource Limits**: 0.5 CPU, 512MB RAM
- **Resource Reservations**: 0.25 CPU, 256MB RAM

#### cs-accounts-events-consumer

- **Replicas**: 2
- **Port**: 8084
- **Context Path**: `/` (root)
- **Health Check**: `http://localhost:8084/actuator/health`
- **Resource Limits**: 0.5 CPU, 512MB RAM
- **Resource Reservations**: 0.25 CPU, 256MB RAM

#### postgres

- **Replicas**: 1
- **Port**: 5432
- **Database**: tuum_banking
- **Health Check**: `pg_isready -U tuum_user -d tuum_banking`

#### rabbitmq

- **Replicas**: 1
- **Ports**: 5672 (AMQP), 15672 (Management UI)
- **Health Check**: `rabbitmq-diagnostics ping`

### Network Configuration

- **Network Type**: Overlay network
- **Network Name**: tuum-banking_tuum-network
- **Driver**: overlay
- **Attachable**: true

### Volume Configuration

- **postgres_data**: Local driver for PostgreSQL data persistence
- **rabbitmq_data**: Local driver for RabbitMQ data persistence

## Scaling

### Scale Services

```bash
# Scale fs-accounts-service to 3 replicas
docker service scale tuum-banking_fs-accounts-service=3

# Scale consumer to 4 replicas
docker service scale tuum-banking_cs-accounts-events-consumer=4
```

### Update Services

```bash
# Update service with new image
docker service update --image tuum-fs-accounts-service:new-version tuum-banking_fs-accounts-service
```

## Monitoring and Logging

### View Service Logs

```bash
# View logs for specific service
docker service logs tuum-banking_fs-accounts-service

# Follow logs in real-time
docker service logs -f tuum-banking_cs-accounts-events-consumer

# View logs for specific task
docker logs <container-name>
```

### Health Monitoring

```bash
# Check service health
curl http://localhost:8083/api/v1/actuator/health
curl http://localhost:8084/actuator/health

# Check database connectivity
docker exec -it <postgres-container> pg_isready -U tuum_user -d tuum_banking

# Check RabbitMQ management UI
open http://localhost:15672
# Username: tuum_user, Password: tuum_password
```

## Testing

### Run Test Script

```bash
./test-swarm.sh
```

### Manual API Testing

```bash
# Create account
curl -X POST http://localhost:8083/api/v1/accounts \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-$(date +%s)" \
  -d '{"customerId": "12345", "country": "EE", "currencies": ["EUR", "USD"]}'

# Get account
curl -X GET http://localhost:8083/api/v1/accounts/12345

# Create transaction
curl -X POST http://localhost:8083/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-tx-$(date +%s)" \
  -d '{"accountId": "12345", "amount": 100, "currency": "EUR", "direction": "IN", "description": "Test"}'
```

## Troubleshooting

### Common Issues

#### 1. Services Not Starting

```bash
# Check service status
docker stack services tuum-banking

# Check task status
docker stack ps tuum-banking

# View service logs
docker service logs tuum-banking_fs-accounts-service
```

#### 2. Health Check Failures

- Verify health check endpoints are accessible
- Check if services are running on correct ports
- Ensure context paths are correctly configured

#### 3. Network Connectivity Issues

```bash
# Check network configuration
docker network ls
docker network inspect tuum-banking_tuum-network

# Test connectivity between services
docker exec -it <container-name> ping <service-name>
```

#### 4. Resource Constraints

```bash
# Check resource usage
docker stats

# Adjust resource limits in docker-stack.yml if needed
```

### Recovery Procedures

#### Restart Failed Services

```bash
# Restart specific service
docker service update --force tuum-banking_fs-accounts-service

# Restart entire stack
docker stack rm tuum-banking
docker stack deploy -c docker-stack.yml tuum-banking
```

#### Rollback Service Updates

```bash
# Rollback to previous version
docker service rollback tuum-banking_fs-accounts-service
```

## Maintenance

### Backup and Restore

```bash
# Backup PostgreSQL data
docker exec -it <postgres-container> pg_dump -U tuum_user tuum_banking > backup.sql

# Restore PostgreSQL data
docker exec -i <postgres-container> psql -U tuum_user tuum_banking < backup.sql
```

### Cleanup

```bash
# Remove stack
docker stack rm tuum-banking

# Remove unused networks
docker network prune

# Remove unused volumes
docker volume prune

# Leave swarm (if needed)
docker swarm leave --force
```

## Performance Considerations

### Resource Optimization

- Monitor CPU and memory usage with `docker stats`
- Adjust resource limits based on actual usage
- Consider using resource reservations for critical services

### Scaling Strategy

- Start with 2 replicas for application services
- Scale based on load testing results
- Monitor queue depths in RabbitMQ management UI

### High Availability

- Deploy to multiple nodes for true HA
- Use external database and message broker for production
- Implement proper backup and disaster recovery procedures

## Security Considerations

### Network Security

- Use overlay networks for service-to-service communication
- Restrict external access to only necessary ports
- Consider using Docker secrets for sensitive data

### Access Control

- Use strong passwords for database and message broker
- Implement proper authentication and authorization
- Regular security updates for base images

## Production Recommendations

1. **Use External Database**: Deploy PostgreSQL on dedicated infrastructure
2. **Use External Message Broker**: Deploy RabbitMQ on dedicated infrastructure
3. **Implement Monitoring**: Use Prometheus, Grafana, or similar tools
4. **Log Aggregation**: Use ELK stack or similar for centralized logging
5. **Load Balancing**: Use external load balancer (HAProxy, nginx, etc.)
6. **SSL/TLS**: Implement proper SSL termination
7. **Backup Strategy**: Implement automated backup procedures
8. **Disaster Recovery**: Plan for complete system recovery
