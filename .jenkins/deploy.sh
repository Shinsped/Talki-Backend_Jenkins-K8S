#!/bin/bash

# Deployment script for Talki Application
# Usage: ./deploy.sh [environment] [image_tag]

set -e

# Configuration
APP_NAME="talki-application"
DOCKER_COMPOSE_FILE="docker-compose.yml"
HEALTH_CHECK_URL="http://localhost:8080/api/actuator/health"
MAX_WAIT_TIME=120

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Parse arguments
ENVIRONMENT=${1:-development}
IMAGE_TAG=${2:-latest}

log_info "Starting deployment for environment: $ENVIRONMENT"
log_info "Using image tag: $IMAGE_TAG"

# Set environment variables
export IMAGE_TAG=$IMAGE_TAG
export SPRING_PROFILES_ACTIVE=$ENVIRONMENT

# Pre-deployment checks
log_info "Running pre-deployment checks..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    log_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose file exists
if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
    log_error "Docker compose file not found: $DOCKER_COMPOSE_FILE"
    exit 1
fi

# Stop existing containers
log_info "Stopping existing containers..."
docker compose -f $DOCKER_COMPOSE_FILE down || true

# Pull latest images
log_info "Pulling latest images..."
docker compose -f $DOCKER_COMPOSE_FILE pull || log_warn "Failed to pull some images"

# Start services
log_info "Starting services..."
docker compose -f $DOCKER_COMPOSE_FILE up -d

# Wait for services to be ready
log_info "Waiting for services to be ready..."
WAIT_TIME=0
while [ $WAIT_TIME -lt $MAX_WAIT_TIME ]; do
    if curl -f $HEALTH_CHECK_URL > /dev/null 2>&1; then
        log_info "Application is ready!"
        break
    fi
    
    log_info "Waiting for application to start... ($WAIT_TIME/$MAX_WAIT_TIME seconds)"
    sleep 5
    WAIT_TIME=$((WAIT_TIME + 5))
done

# Final health check
if curl -f $HEALTH_CHECK_URL > /dev/null 2>&1; then
    log_info "✅ Deployment successful!"
    log_info "Application is running at: http://localhost:8080"
    log_info "Health check: $HEALTH_CHECK_URL"
    
    # Show running containers
    log_info "Running containers:"
    docker-compose -f $DOCKER_COMPOSE_FILE ps
    
    exit 0
else
    log_error "❌ Deployment failed! Application is not responding."
    log_error "Check logs with: docker-compose -f $DOCKER_COMPOSE_FILE logs"
    
    # Show container status
    docker-compose -f $DOCKER_COMPOSE_FILE ps
    
    exit 1
fi

