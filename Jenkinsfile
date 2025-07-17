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
                    branch: '2ALINFO2'
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

        // Stage 7: Docker Compose
        stage('Docker Compose Up') {
            steps {
                sh '''
                    docker-compose down || true
                    docker-compose pull --quiet
                    docker-compose up -d --build
                '''
            }
        }
    }

    post {
        always {
            sh 'docker system prune -f'
            cleanWs()
        }
        success {
            echo """
            ✅ Pipeline Successful!
            Application: http://localhost:8080
            SonarQube: ${SONARQUBE_URL} (admin/admin)
            Nexus: ${NEXUS_URL} (admin/dd66a977-636f-47c9-ba17-f1c6e0dcd371)
            """
        }
    }
}
