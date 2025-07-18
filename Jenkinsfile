pipeline {
    agent any

    environment {
        // Credentials
        DOCKER_HUB_CREDS = credentials('docker-hub-creds')
        NEXUS_CREDS = credentials('nexus-creds')
        SONAR_CREDS = credentials('sonar-creds')
        GITHUB_TOKEN = credentials('github-token')

        // URLs
        NEXUS_URL = 'http://localhost:8081'
        SONARQUBE_URL = 'http://localhost:9000'
        DOCKER_IMAGE = 'aakefi/foyer-app'
        
        // FIXED: Explicit project name for Docker Compose
        COMPOSE_PROJECT_NAME = 'foyer-pipeline'
    }

    parameters {
        booleanParam(
            name: 'KEEP_RUNNING',
            defaultValue: true,
            description: 'Keep services running after successful build'
        )
    }

    stages {
        stage('Clone Git Project') {
            steps {
                git(
                    url: 'https://${GITHUB_TOKEN}@github.com/AhmedAmineKefi/AhmedAmine-Kefi-Foyer.git',
                    credentialsId: 'github-token',
                    branch: '2ALINFO2OT'
                )
                sh 'ls -la'
                sh 'pwd'  // Show where we are
            }
        }

        stage('Maven Build package') {
            steps {
                sh 'mvn clean compile -DskipTests=true'
            }
        }

        stage('SonarQube Scan') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'sonar-creds', usernameVariable: 'SONAR_USER', passwordVariable: 'SONAR_PASS')]) {
                    sh """
                        mvn sonar:sonar \
                          -Dsonar.host.url=${SONARQUBE_URL} \
                          -Dsonar.login=${SONAR_USER} \
                          -Dsonar.password=${SONAR_PASS}
                    """
                }
            }
        }

        stage('Nexus Deploy') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-creds',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh """
                        mvn deploy \
                          -Dnexus.user=admin \
                          -Dnexus.password=admin \
                          -DaltDeploymentRepository=nexus::default::http://localhost:8081/repository/maven-snapshots/
                    """
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_IMAGE}:${env.BUILD_ID}")
                }
            }
        }

        // FIXED: Proper Docker Compose deployment that persists
        stage('Deploy Application') {
            steps {
                script {
                    sh '''
                        echo "🚀 Starting deployment with persistent containers..."
                        echo "Current working directory: $(pwd)"
                        echo "Docker Compose project name: ${COMPOSE_PROJECT_NAME}"
                        
                        # Show any existing containers for this project
                        echo "=== Existing containers for project ${COMPOSE_PROJECT_NAME} ==="
                        docker-compose -p ${COMPOSE_PROJECT_NAME} ps || echo "No existing containers"
                        
                        # Start services with explicit project name
                        echo "=== Starting services ==="
                        docker-compose -p ${COMPOSE_PROJECT_NAME} up -d --build
                        
                        # Wait for services to start
                        echo "=== Waiting for services to initialize ==="
                        sleep 45
                        
                        # Show final status
                        echo "=== Final container status ==="
                        docker-compose -p ${COMPOSE_PROJECT_NAME} ps
                    '''
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    sh '''
                        echo "=== Verifying deployment ==="
                        
                        # Show container details
                        docker-compose -p ${COMPOSE_PROJECT_NAME} ps
                        
                        # Test application health on multiple possible ports
                        echo "=== Testing application health ==="
                        APP_HEALTHY=false
                        
                        for port in 8087 8086 8080; do
                            echo "Testing port $port..."
                            if curl -f http://localhost:$port/actuator/health 2>/dev/null; then
                                echo "✅ Application is healthy on port $port"
                                APP_HEALTHY=true
                                break
                            elif curl -f http://localhost:$port/Foyer/actuator/health 2>/dev/null; then
                                echo "✅ Application is healthy on port $port with /Foyer path"
                                APP_HEALTHY=true
                                break
                            fi
                        done
                        
                        if [ "$APP_HEALTHY" = false ]; then
                            echo "❌ Application health check failed on all ports"
                            echo "Checking application logs:"
                            docker-compose -p ${COMPOSE_PROJECT_NAME} logs --tail=50 foyer-app
                            exit 1
                        fi
                        
                        # Test monitoring services
                        echo "=== Testing monitoring services ==="
                        curl -f http://localhost:16686/ && echo "✅ Jaeger accessible" || echo "⚠️ Jaeger not accessible"
                        curl -f http://localhost:3000/api/health && echo "✅ Grafana accessible" || echo "⚠️ Grafana not accessible"
                        curl -f http://localhost:9090/-/healthy && echo "✅ Prometheus accessible" || echo "⚠️ Prometheus not accessible"
                        
                        echo "=== Deployment verification complete ==="
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                echo "=== Pipeline Post-Actions ==="
                sh 'docker-compose -p ${COMPOSE_PROJECT_NAME} ps'
                
                if (params.KEEP_RUNNING && currentBuild.result != 'FAILURE') {
                    echo "🎉 SUCCESS - Keeping services running!"
                    echo ""
                    echo "📋 Your services are running with project name: ${COMPOSE_PROJECT_NAME}"
                    echo ""
                    echo "🔍 To check status from command line:"
                    echo "   docker-compose -p ${COMPOSE_PROJECT_NAME} ps"
                    echo ""
                    echo "🌐 Access URLs:"
                    echo "   Application: http://localhost:8087 (or 8086, 8080)"
                    echo "   Jaeger: http://localhost:16686"
                    echo "   Grafana: http://localhost:3000 (admin/admin123)"
                    echo "   Prometheus: http://localhost:9090"
                    echo ""
                    echo "🛑 To stop services:"
                    echo "   docker-compose -p ${COMPOSE_PROJECT_NAME} down"
                    echo ""
                    echo "✅ Containers will remain running after pipeline completion"
                    
                } else if (currentBuild.result == 'FAILURE') {
                    echo "❌ Pipeline failed - cleaning up"
                    sh 'docker-compose -p ${COMPOSE_PROJECT_NAME} down -v'
                } else {
                    echo "🧹 Parameter set to not keep running - cleaning up"
                    sh 'docker-compose -p ${COMPOSE_PROJECT_NAME} down'
                }
            }
        }
    }
}
