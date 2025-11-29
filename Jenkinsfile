pipeline {
    agent any

    options {
        skipDefaultCheckout()
    }

    stages {

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

        stage('Check ENV') {
            steps {
                sh '''
                    echo "KEY: $NCP_ACCESS_KEY"
                    echo "SECRET: $NCP_SECRET_KEY"
                '''
            }
        }

        stage('Build Backend') {
            steps {
                dir('Matcha/backend') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build -t matcha-backend:latest Matcha/backend
                '''
            }
        }

        stage('Deploy to Server') {
            steps {
                sshagent(['deploy-ssh']) {
                    sh '''
                        echo "=== Save docker image ==="
                        docker save matcha-backend:latest -o matcha_image.tar

                        echo "=== Check image file ==="
                        ls -lh matcha_image.tar

                        echo "=== Transfer to deploy-server ==="
                        scp -o StrictHostKeyChecking=no matcha_image.tar root@10.0.2.6:/opt/matcha/

                        echo "=== Load image on deploy-server ==="
                        ssh -o StrictHostKeyChecking=no root@10.0.2.6 "
                            docker load -i /opt/matcha/matcha_image.tar
                        "

                        echo "=== Restart container ==="
                        ssh -o StrictHostKeyChecking=no root@10.0.2.6 "
                            docker stop matcha-backend || true
                            docker rm matcha-backend || true
                            docker run -d --restart always --name matcha-backend -p 8080:8080 matcha-backend:latest
                        "
                    '''
                }
            }
        }
    }

    post {
        always {
            junit 'Matcha/backend/build/test-results/test/*.xml'

            jacoco execPattern: 'Matcha/backend/build/jacoco/test.exec',
                   classPattern: 'Matcha/backend/build/classes/java/main',
                   sourcePattern: 'Matcha/backend/src/main/java'
        }
    }
}
