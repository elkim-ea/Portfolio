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
                    sh './gradlew clean build'
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
                        docker save matcha-backend:latest | \
                        ssh -o StrictHostKeyChecking=no root@10.0.2.6 "docker load"

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

    post {
        always {
            // 🔥 1) JUnit 테스트 결과 (Test Trend)
            junit 'Matcha/backend/build/test-results/test/*.xml'

            // 🔥 2) JaCoCo 커버리지 보고서
            jacoco execPattern: 'Matcha/backend/build/jacoco/test.exec',
                   classPattern: 'Matcha/backend/build/classes/java/main',
                   sourcePattern: 'Matcha/backend/src/main/java'

            // 🔥 3) CheckStyle 코드품질 분석 (있을 경우만)
            recordIssues tools: [checkStyle(pattern: 'Matcha/backend/build/reports/checkstyle/*.xml')]
        }
    }
}
