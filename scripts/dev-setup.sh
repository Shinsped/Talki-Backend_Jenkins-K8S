#!/bin/bash

# Talki Application Development Environment Setup Script
# This script sets up the local development environment

set -e

echo "🚀 Setting up Talki development environment..."

# Check prerequisites
check_prerequisites() {
    echo "📋 Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        echo "❌ Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check Java
    if ! command -v java &> /dev/null; then
        echo "❌ Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    echo "✅ Prerequisites check passed"
}

# Start infrastructure services
start_infrastructure() {
    echo "🐳 Starting infrastructure services..."
    
    # Start MySQL and Kafka
    docker-compose up -d mysql kafka zookeeper
    
    # Wait for services to be ready
    echo "⏳ Waiting for services to be ready..."
    sleep 30
    
    # Check MySQL
    echo "🔍 Checking MySQL connection..."
    docker-compose exec -T mysql mysqladmin ping -h localhost --silent || {
        echo "❌ MySQL is not ready"
        exit 1
    }
    
    # Check Kafka
    echo "🔍 Checking Kafka connection..."
    docker-compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null || {
        echo "❌ Kafka is not ready"
        exit 1
    }
    
    echo "✅ Infrastructure services are ready"
}

# Build application
build_application() {
    echo "🔨 Building application..."
    
    # Make gradlew executable
    chmod +x ./gradlew
    
    # Build without tests first
    ./gradlew build -x test
    
    echo "✅ Application built successfully"
}

# Run tests
run_tests() {
    echo "🧪 Running tests..."
    
    # Run unit tests
    ./gradlew test
    
    echo "✅ Tests passed"
}

# Create Kafka topics
create_kafka_topics() {
    echo "📝 Creating Kafka topics..."
    
    # Create topics
    docker-compose exec -T kafka kafka-topics --create --topic audio-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 --if-not-exists
    docker-compose exec -T kafka kafka-topics --create --topic chat-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 --if-not-exists
    
    # List topics
    echo "📋 Created topics:"
    docker-compose exec -T kafka kafka-topics --list --bootstrap-server localhost:9092
    
    echo "✅ Kafka topics created"
}

# Setup development database
setup_database() {
    echo "🗄️ Setting up development database..."
    
    # Wait a bit more for MySQL to be fully ready
    sleep 10
    
    # Run any additional database setup if needed
    echo "✅ Database setup completed"
}

# Main execution
main() {
    echo "🎯 Starting Talki development environment setup"
    echo "================================================"
    
    check_prerequisites
    start_infrastructure
    build_application
    run_tests
    create_kafka_topics
    setup_database
    
    echo ""
    echo "🎉 Development environment setup completed!"
    echo ""
    echo "📍 Services are running at:"
    echo "   - Application: http://localhost:8080"
    echo "   - MySQL: localhost:3306"
    echo "   - Kafka: localhost:9092"
    echo ""
    echo "🚀 To start the application:"
    echo "   ./gradlew bootRun"
    echo ""
    echo "🛑 To stop infrastructure services:"
    echo "   docker-compose down"
    echo ""
}

# Execute main function
main "$@"

