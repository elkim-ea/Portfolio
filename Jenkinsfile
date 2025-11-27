pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git(
                    branch: 'main',
                    credentialsId: 'github-token',
                    url: 'https://github.com/elkim-ea/Portfolio.git'
                )
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
                    docker save matcha-backend:latest | ssh -o StrictHostKeyChecking=no root@10.0.2.6 "docker load"

                    ssh -o StrictHostKeyChecking=no root@10.0.2.6 "
                        docker stop matcha-backend || true &&
                        docker rm matcha-backend || true &&
                        docker run -d --name matcha-backend -p 8080:8080 matcha-backend:latest
                    "
                    '''
                }
            }
        }

    }
}
