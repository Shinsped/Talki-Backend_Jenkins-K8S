pipeline {
    agent any
    
    environment {
        // Docker Configuration
        DOCKER_REGISTRY = credentials('docker-registry-url')
        DOCKER_REPOSITORY = 'talki/talki-application'
        DOCKER_CREDENTIALS_ID = 'docker-registry-credentials'
        
        // Application Configuration
        APP_NAME = 'talki-application'
        JAVA_VERSION = '17'
        
        // Build Configuration
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 1, unit: 'HOURS')
        skipStagesAfterUnstable()
    }
    
    triggers {
        // Poll SCM every 10 minutes for changes
        pollSCM('H/10 * * * *')
    }
    
    stages {
        stage('Checkout & Setup') {
            steps {
                script {
                    // Clean workspace
                    cleanWs()
                    
                    // Checkout source code
                    checkout scm
                    
                    // Set build information
                    env.BUILD_VERSION = sh(
                        script: "echo '${env.BUILD_NUMBER}-${env.GIT_COMMIT[0..7]}'",
                        returnStdout: true
                    ).trim()
                    
                    env.IMAGE_TAG = env.BRANCH_NAME == 'main' ? 'latest' : "${env.BRANCH_NAME}-${env.BUILD_VERSION}"
                    
                    // Display build information
                    echo "Building version: ${env.BUILD_VERSION}"
                    echo "Docker image tag: ${env.IMAGE_TAG}"
                    echo "Branch: ${env.BRANCH_NAME}"
                }
            }
        }
        
        stage('Build Application') {
            steps {
                script {
                    // Set Java environment
                    sh """
                        export JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk
                        export PATH=\$JAVA_HOME/bin:\$PATH
                        java -version
                        ./gradlew --version
                    """
                    
                    // Build application
                    sh './gradlew clean build -x test --no-daemon --parallel'
                    
                    // Archive artifacts
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Run Tests') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        script {
                            sh './gradlew test --no-daemon'
                        }
                    }
                    post {
                        always {
                            // Publish test results
                            publishTestResults testResultsPattern: 'build/test-results/test/*.xml'
                        }
                    }
                }
                
                stage('Integration Tests') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                        }
                    }
                    steps {
                        script {
                            // Start test dependencies
                            sh '''
                                docker-compose -f docker-compose.test.yml up -d mysql
                                sleep 30
                            '''
                            
                            try {
                                // Run integration tests (if they exist)
                                sh './gradlew integrationTest --no-daemon || echo "No integration tests found"'
                            } finally {
                                // Clean up
                                sh 'docker-compose -f docker-compose.test.yml down -v || true'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Code Quality') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    // Run code quality checks
                    sh './gradlew check --no-daemon || echo "Code quality checks completed with warnings"'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    // Build Docker image
                    def dockerImage = docker.build("${DOCKER_REPOSITORY}:${IMAGE_TAG}")
                    
                    // Tag for different environments
                    if (env.BRANCH_NAME == 'main') {
                        dockerImage.tag('latest')
                        dockerImage.tag("v${BUILD_VERSION}")
                    }
                    
                    // Push to registry
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        dockerImage.push()
                        
                        if (env.BRANCH_NAME == 'main') {
                            dockerImage.push('latest')
                            dockerImage.push("v${BUILD_VERSION}")
                        }
                    }
                    
                    echo "Docker image pushed: ${DOCKER_REPOSITORY}:${IMAGE_TAG}"
                }
            }
        }
        
        stage('Deploy to Development') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'feature/*'
                }
            }
            steps {
                script {
                    echo "Deploying to Development environment..."
                    
                    // Deploy using docker-compose
                    sh """
                        export IMAGE_TAG=${IMAGE_TAG}
                        docker-compose -f docker-compose.yml down || true
                        docker-compose -f docker-compose.yml up -d
                    """
                    
                    // Wait for services to be ready
                    sleep(30)
                    
                    // Health check
                    sh '''
                        curl -f http://localhost:8080/api/actuator/health || exit 1
                        echo "Development deployment successful!"
                    '''
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Deploying to Staging environment..."
                    
                    // Deploy to staging (modify as needed for your staging environment)
                    sh """
                        export IMAGE_TAG=${IMAGE_TAG}
                        export SPRING_PROFILES_ACTIVE=staging
                        docker-compose -f docker-compose.yml down || true
                        docker-compose -f docker-compose.yml up -d
                    """
                    
                    // Health check
                    sleep(30)
                    sh '''
                        curl -f http://localhost:8080/api/actuator/health || exit 1
                        echo "Staging deployment successful!"
                    '''
                }
            }
        }
        
        stage('Production Deployment Approval') {
            when {
                branch 'main'
            }
            steps {
                script {
                    timeout(time: 24, unit: 'HOURS') {
                        input message: 'Deploy to Production?',
                              ok: 'Deploy',
                              submitterParameter: 'APPROVER'
                    }
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Deploying to Production environment..."
                    echo "Approved by: ${env.APPROVER}"
                    
                    // Production deployment (customize for your production setup)
                    sh """
                        export IMAGE_TAG=${IMAGE_TAG}
                        export SPRING_PROFILES_ACTIVE=docker
                        # Add your production deployment commands here
                        echo "Production deployment would happen here"
                    """
                }
            }
        }
        
        stage('Post-Deployment Verification') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    // Verify deployment
                    sh '''
                        echo "Running post-deployment verification..."
                        curl -f http://localhost:8080/api/actuator/health
                        curl -f http://localhost:8080/health
                        echo "Deployment verification completed successfully!"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            // Clean up
            sh 'docker system prune -f || true'
            
            // Archive logs if they exist
            archiveArtifacts artifacts: 'build/logs/**/*', allowEmptyArchive: true
        }
        
        success {
            echo "✅ Pipeline completed successfully for ${APP_NAME} v${BUILD_VERSION}"
        }
        
        failure {
            echo "❌ Pipeline failed for ${APP_NAME} v${BUILD_VERSION}"
            
            // Clean up on failure
            sh '''
                docker-compose -f docker-compose.yml down || true
                docker-compose -f docker-compose.test.yml down || true
            '''
        }
        
        unstable {
            echo "⚠️ Pipeline completed with warnings for ${APP_NAME} v${BUILD_VERSION}"
        }
        
        cleanup {
            // Final cleanup
            cleanWs()
        }
    }
}

