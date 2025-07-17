pipeline {
    agent any

    environment {
        // Credentials (configure these in Jenkins)
        DOCKER_HUB_CREDS = credentials('docker-hub-creds')
        NEXUS_CREDS = credentials('nexus-creds')
        SONAR_CREDS = credentials('sonar-creds')
        GITHUB_TOKEN = credentials('github-token')

        // URLs
        NEXUS_URL = 'http://localhost:8081'
        SONARQUBE_URL = 'http://localhost:9000'
        DOCKER_IMAGE = 'aakefi/foyer-app'
    }

    stages {
        // Stage 1: Clone Git Repo
        stage('Clone Git Project') {
            steps {
                git(
                    url: 'https://${GITHUB_TOKEN}@github.com/AhmedAmineKefi/AhmedAmine-Kefi-Foyer.git',
                    credentialsId: 'github-token',
                    branch: '2ALINFO2OT'  // FIXED: Correct branch name
                )
                sh 'ls -la'  // Verify files
            }
        }

        // Stage 2: Maven Build
        stage('Maven Build package') {
            steps {
                sh 'mvn clean compile  -DskipTests=true'
            }
        }

        // Stage 3: SonarQube Analysis
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

        // Stage 4: Deploy to Nexus
        stage('Nexus Deploy') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-creds',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    // Debug: Check if variables exist (values will be masked)
                    sh '''
                        echo "NEXUS_USER is set (masked): ${NEXUS_USER}"
                        echo "NEXUS_PASS is set (masked): ${NEXUS_PASS}"
                    '''

                    // Proceed with Maven deploy
                    sh """
                        mvn deploy \
                          -Dnexus.user=admin \
                          -Dnexus.password=admin \
                          -DaltDeploymentRepository=nexus::default::http://localhost:8081/repository/maven-snapshots/
                    """
                }
            }
        }

        // Stage 5: Docker Build
        stage('Docker Build') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_IMAGE}:${env.BUILD_ID}")
                }
            }
        }

        // Stage 6: Push to Docker Hub
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin
                        docker push ${DOCKER_IMAGE}:${env.BUILD_ID}
                        docker tag ${DOCKER_IMAGE}:${env.BUILD_ID} ${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        // Stage 7: Docker Compose - FIXED VERSION
        stage('Docker Compose Up') {
            steps {
                script {
                    try {
                        // Stop existing containers gracefully
                        sh 'docker-compose down --remove-orphans || true'
                        
                        // Clean up any dangling images (but preserve volumes)
                        sh 'docker image prune -f || true'
                        
                        // Pull latest images quietly
                        sh 'docker-compose pull --quiet || true'

                        // Build and start with health checks and proper wait
                        sh 'docker-compose up -d --build --force-recreate'
                        
                        // Wait for services to be healthy
                        sh '''
                            echo "Waiting for services to be healthy..."
                            timeout=300  # 5 minutes timeout
                            elapsed=0
                            
                            while [ $elapsed -lt $timeout ]; do
                                if docker-compose ps | grep -q "Up (healthy)"; then
                                    echo "Services are starting to become healthy..."
                                fi
                                
                                # Check if all services are running
                                running_services=$(docker-compose ps --services --filter "status=running" | wc -l)
                                total_services=$(docker-compose ps --services | wc -l)
                                
                                if [ "$running_services" -eq "$total_services" ] && [ "$total_services" -gt 0 ]; then
                                    echo "All services are running!"
                                    break
                                fi
                                
                                echo "Waiting for services... ($elapsed/$timeout seconds)"
                                sleep 10
                                elapsed=$((elapsed + 10))
                            done
                            
                            # Final status check
                            echo "Final container status:"
                            docker-compose ps
                            
                            # Check if any containers failed
                            if docker-compose ps | grep -q "Exit"; then
                                echo "Some containers have exited. Checking logs..."
                                docker-compose logs --tail=50
                                exit 1
                            fi
                        '''
                        
                    } catch (Exception e) {
                        echo "Docker Compose deployment failed: ${e.getMessage()}"
                        sh 'docker-compose logs --tail=100'
                        sh 'docker-compose ps'
                        throw e
                    }
                }
            }
        }
        
        // Stage 8: Verify Deployment
        stage('Verify Deployment') {
            steps {
                script {
                    sh '''
                        echo "Verifying deployment..."
                        
                        # Check container status
                        echo "=== Container Status ==="
                        docker-compose ps
                        
                        # Check application health
                        echo "=== Application Health Check ==="
                        for i in {1..30}; do
                            if curl -f http://localhost:8086/Foyer/actuator/health 2>/dev/null; then
                                echo "Application is healthy!"
                                break
                            fi
                            echo "Attempt $i: Application not ready yet..."
                            sleep 10
                        done
                        
                        # Check Prometheus
                        echo "=== Prometheus Health Check ==="
                        curl -f http://localhost:9090/-/healthy || echo "Prometheus health check failed"
                        
                        # Check Grafana
                        echo "=== Grafana Health Check ==="
                        curl -f http://localhost:3000/api/health || echo "Grafana health check failed"
                        
                        # Final verification
                        echo "=== Final Container Status ==="
                        docker-compose ps
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                // Show final status before cleanup
                sh 'echo "=== Final Status Before Cleanup ==="'
                sh 'docker-compose ps || true'
                
                // Only clean up build artifacts, not running containers
               // sh 'docker image prune -f --filter "dangling=true" || true'
                
                // Don't run docker system prune as it removes everything
                // sh 'docker system prune -f'  // REMOVED: This was stopping containers
            }
            cleanWs()
        }
        success {
            echo """
            ✅ Pipeline Successful!
            ======================
            Application:      http://localhost:8086/Foyer
            API Documentation: http://localhost:8086/Foyer/swagger-ui.html
            Actuator Endpoints:
              - Health:      http://localhost:8086/Foyer/actuator/health
              - Metrics:     http://localhost:8086/Foyer/actuator/prometheus
              - Info:        http://localhost:8086/Foyer/actuator/info

            Database:
              - JDBC URL:    jdbc:mysql://localhost:3306/foyer
              - Username:    root
              - Password:    password

            Monitoring:
              - Prometheus:  http://localhost:9090
              - Grafana:     http://localhost:3000 (admin/admin)

            To check container status: docker-compose ps
            To view logs: docker-compose logs [service-name]
            """
        }
        failure {
            script {
                sh 'echo "=== Deployment Failed - Container Status ==="'
                sh 'docker-compose ps || true'
                sh 'echo "=== Recent Logs ==="'
                sh 'docker-compose logs --tail=100 || true'
            }
        }
    }
}
