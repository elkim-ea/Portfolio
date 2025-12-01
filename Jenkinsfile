pipeline {
    agent any

    options {
        skipDefaultCheckout()
    }

    environment {
        NCP_ACCESS_KEY = credentials('ncp-access-key')
        NCP_SECRET_KEY = credentials('ncp-secret-key')
    }

    stages {

        /* ========== 1. Checkout ========== */
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/elkim-ea/Portfolio.git',
                        credentialsId: 'github-jenkins-token'
                    ]]
                ])
            }
        }

        /* ========== 2. Build Backend ========== */
        stage('Build Backend') {
            steps {
                dir('Matcha/backend') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
                }
            }
        }

        /* ========== 3. Build Frontend ========== */
        stage('Build Frontend') {
            steps {
                dir('Matcha/frontend') {
                    sh '''
                        npm install
                        npm run build
                    '''
                }
            }
        }

        /* ========== 4. Docker Build (Backend + Frontend) ========== */
        stage('Build Docker Images') {
            steps {
                sh '''
                    echo "=== Backend Docker Build ==="
                    docker build -t matcha-backend:latest Matcha/backend

                    echo "=== Frontend Docker Build ==="
                    docker build -t matcha-frontend:latest Matcha/frontend
                '''
            }
        }

        /* ========== 5. Save images as tar (Backend + Frontend) ========== */
        stage('Save Docker Images as TAR') {
            steps {
                sh '''
                    echo "=== Save Backend Image TAR ==="
                    docker save matcha-backend:latest -o matcha_backend.tar

                    echo "=== Save Frontend Image TAR ==="
                    docker save matcha-frontend:latest -o matcha_frontend.tar
                '''
            }
        }

        /* ========== 6. Deploy to Private Server ========== */
        stage('Deploy to Private Server') {
            steps {
                sshagent(['deploy-ssh']) {

                    sh '''
                        echo "=== Transfer TAR files to deploy-server ==="
                        scp -o StrictHostKeyChecking=no matcha_backend.tar root@10.0.2.6:/opt/matcha/
                        scp -o StrictHostKeyChecking=no matcha_frontend.tar root@10.0.2.6:/opt/matcha/

                        echo "=== Load Backend & Frontend images on deploy-server ==="
                        ssh -o StrictHostKeyChecking=no root@10.0.2.6 "
                            docker load -i /opt/matcha/matcha_backend.tar
                            docker load -i /opt/matcha/matcha_frontend.tar
                        "

                        echo "=== Restart Backend container ==="
                        ssh -o StrictHostKeyChecking=no root@10.0.2.6 "
                            docker stop matcha-backend || true
                            docker rm matcha-backend || true

                            docker run -d --restart always \
                                --name matcha-backend \
                                -e NCP_ACCESS_KEY=${NCP_ACCESS_KEY} \
                                -e NCP_SECRET_KEY=${NCP_SECRET_KEY} \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                -p 8080:8080 matcha-backend:latest
                        "

                        echo "=== Restart Frontend container ==="
                        ssh -o StrictHostKeyChecking=no root@10.0.2.6 "
                            docker stop matcha-frontend || true
                            docker rm matcha-frontend || true

                            docker run -d --restart always \
                                --name matcha-frontend \
                                -p 80:80 matcha-frontend:latest
                        "
                    '''
                }
            }
        }
    }
}
