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

    parameters {
        booleanParam(
            name: 'DEPLOY_MODE',
            defaultValue: true,
            description: 'Deploy and keep services running after pipeline'
        )
        booleanParam(
            name: 'FORCE_REBUILD',
            defaultValue: false,
            description: 'Force rebuild of all containers'
        )
    }

    stages {
        // Stage 1: Clone Git Repo
        stage('Clone Git Project') {
            steps {
                git(
                    url: 'https://${GITHUB_TOKEN}@github.com/AhmedAmineKefi/AhmedAmine-Kefi-Foyer.git',
                    credentialsId: 'github-token',
                    branch: '2ALINFO2OT'
                )
                sh 'ls -la'
            }
        }

        // Stage 2: Maven Build
        stage('Maven Build package') {
            steps {
                sh 'mvn clean compile -DskipTests=true'
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

        // Stage 6: Docker Compose Deploy - FIXED VERSION
        stage('Docker Compose Deploy') {
            steps {
                script {
                    try {
                        // Only stop containers if we're doing a force rebuild
                        if (params.FORCE_REBUILD) {
                            echo "Force rebuild requested - stopping existing containers"
                            sh 'docker-compose down --remove-orphans || true'
                        } else {
                            echo "Checking existing containers..."
                            sh 'docker-compose ps || true'
                        }
                        
                        // Clean up dangling images
                        sh 'docker image prune -f || true'
                        
                        // Build and start containers
                        if (params.FORCE_REBUILD) {
                            sh 'docker-compose up -d --build --force-recreate'
                        } else {
                            sh 'docker-compose up -d --build'
                        }
                        
                        // Wait for services to start
                        sh '''
                            echo "Waiting for services to start..."
                            sleep 30
                            
                            # Check container status
                            echo "=== Container Status ==="
                            docker-compose ps
                            
                            # Wait for application to be ready
                            echo "=== Waiting for Application ==="
                            for i in {1..30}; do
                                # Try both possible ports
                                if curl -f http://localhost:8087/actuator/health 2>/dev/null; then
                                    echo "✅ Application is healthy on port 8087!"
                                    break
                                elif curl -f http://localhost:8086/Foyer/actuator/health 2>/dev/null; then
                                    echo "✅ Application is healthy on port 8086!"
                                    break
                                elif curl -f http://localhost:8080/actuator/health 2>/dev/null; then
                                    echo "✅ Application is healthy on port 8080!"
                                    break
                                fi
                                echo "Attempt $i: Application not ready yet..."
                                sleep 10
                            done
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
        
        // Stage 7: Verify Deployment
        stage('Verify Deployment') {
            steps {
                script {
                    sh '''
                        echo "=== Final Deployment Verification ==="
                        
                        # Check container status
                        echo "Container Status:"
                        docker-compose ps
                        
                        # Test application endpoints
                        echo "=== Testing Application Endpoints ==="
                        
                        # Try different ports to find the application
                        APP_PORT=""
                        for port in 8087 8086 8080; do
                            if curl -f http://localhost:$port/actuator/health 2>/dev/null; then
                                APP_PORT=$port
                                echo "✅ Application found on port $port"
                                break
                            fi
                        done
                        
                        if [ -z "$APP_PORT" ]; then
                            echo "❌ Application not responding on any expected port"
                            echo "Checking application logs:"
                            docker-compose logs --tail=50 foyer-app
                        else
                            echo "✅ Application is healthy on port $APP_PORT"
                        fi
                        
                        # Check monitoring services
                        echo "=== Checking Monitoring Services ==="
                        curl -f http://localhost:9090/-/healthy && echo "✅ Prometheus healthy" || echo "❌ Prometheus not healthy"
                        curl -f http://localhost:3000/api/health && echo "✅ Grafana healthy" || echo "❌ Grafana not healthy"
                        curl -f http://localhost:16686/ && echo "✅ Jaeger healthy" || echo "❌ Jaeger not healthy"
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                echo "=== Pipeline Completed ==="
                sh 'docker-compose ps'
                
                if (params.DEPLOY_MODE && currentBuild.result != 'FAILURE') {
                    echo "🚀 DEPLOYMENT MODE - Services will remain running"
                    echo ""
                    echo "📋 Access URLs:"
                    echo "   Application: http://localhost:8087 (or check other ports)"
                    echo "   Jaeger: http://localhost:16686"
                    echo "   Grafana: http://localhost:3000 (admin/admin123)"
                    echo "   Prometheus: http://localhost:9090"
                    echo ""
                    echo "🔍 To check status later: docker-compose ps"
                    echo "🛑 To stop services: docker-compose down"
                    echo ""
                    echo "✅ Containers will remain running after pipeline completion"
                } else if (currentBuild.result == 'FAILURE') {
                    echo "❌ Pipeline failed - containers may have stopped"
                    sh 'docker-compose logs --tail=100'
                } else {
                    echo "Pipeline completed - check container status manually"
                }
            }
        }
    }
}
