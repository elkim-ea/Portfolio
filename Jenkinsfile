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

        stage('Push to Deploy Server') {
            steps {
                sh '''
                echo "여기서 ssh 또는 docker push로 배포 서버에 전달할 예정"
                '''
            }
        }
    }
}
