# 🚀 DevOps CI/CD Pipeline on Naver Cloud Platform (NCP)

React + Spring Boot + Docker + Jenkins + LoadBalancer + NAT Gateway

This project implements a fully automated CI/CD pipeline on **Naver Cloud Platform (NCP)** to deploy a full-stack system consisting of:
Frontend (React) + Backend (Spring Boot) + MariaDB

Jenkins is deployed in a **Public Subnet**, while the production Deploy Server runs in a **Private Subnet**.
The Backend is exposed externally through a **Load Balancer**, and the Frontend runs internally on port 80 inside the Deploy Server (Internal Only).


## 📌 1. Architecture Overview

Below is the complete CI/CD workflow:

### ✔ Full CI/CD Flow

| No. | Action |
|-----|-------------------------------------------------------------|
| 1 | Developer pushes code to GitHub |
| 2 | GitHub Webhook ➜ triggers Jenkins pipeline |
| 3 | Jenkins pulls source code from GitHub |
| 4 | Jenkins builds Backend / Frontend |
| 5 | Jenkins builds Docker images |
| 6 | Docker images exported as `.tar` files |
| 7 | Jenkins ➜ transfers images to Deploy Server |
| 8 | Deploy Server loads Docker images ➜ runs containers |
| 9 | Backend container registered to Load Balancer Target Group |
| 10 | Client Browser ➜ LB ➜ Backend(8080) |


## 🏗 NCP Architecture Diagram

<img src="./docs(eng)/architecture.png" width="800">


## 🧱 2. Network Configuration (VPC / Subnet / Routing / NAT)

### ✔ 2-1) VPC Information

| Item | Value |
|------|----------------|
| VPC CIDR | 10.0.0.0/16 |
| Purpose | Isolate Jenkins, Deploy Server, NAT, and LB zones |


### ✔ 2-2) Subnet Structure

| Subnet | CIDR | Zone | Role |
|--------|-----------|--------|----------------------------|
| cicd-subnet | 10.0.1.0/24 | KR-1 | Jenkins Server (Public) |
| private-subnet | 10.0.2.0/24 | KR-1 | Deploy Server (Private) |
| nat-subnet | 10.0.3.0/24 | KR-1 | NAT Gateway |
| public-subnet-1 | 10.0.4.0/24 | KR-2 | LoadBalancer Zone A |
| public-subnet-2 | 10.0.5.0/24 | KR-1 | LoadBalancer Zone B |

📸 Subnet List Screenshot
<img src="./docs(eng)/subnet-list.png" width="700">


### ✔ 2-3) Routing Table (Private Subnet)

| Destination | Target | Description |
|-------------|-----------|---------------------------|
| 0.0.0.0/0 | NAT Gateway | Outbound internet access for private server |
| 10.0.0.0/16 | LOCAL | Internal VPC communication |

📸 NAT Gateway Screenshot  
<img src="./docs(eng)/nat-gateway.png" width="700">


## 🔐 3. Security Configuration (ACG)

### ✔ 3-1) Jenkins ACG

| Protocol | Port | Source |
|----------|------|----------------|
| TCP | 22 | 0.0.0.0/0 |
| TCP | 8080 | 0.0.0.0/0 |


### ✔ 3-2) Deploy Server ACG

| Port | Source | Description |
|------|----------------------|---------------------------|
| 22 | Jenkins Server IP | SSH deployment |
| 80 | LB Subnet | Internal Frontend access |
| 8080 | LB Subnet | Backend API |
| 3306 | Private Only | DB internal communication |

📸 ACG Screenshots  
<img src="./docs(eng)/jenkins-acg.png" width="700">  
<img src="./docs(eng)/deploy-acg.png" width="700">


## 🏛 4. Server Configuration

| Server Name | Private IP | Public IP | Role |
|--------------|-------------|-----------|----------------|
| Jenkins Server | 10.0.1.6 | 211.188.54.xxx | Build / Dockerize |
| Deploy Server | 10.0.2.6 | ❌ None | Production docker-compose host |


## 🧰 5. Jenkins CI/CD Pipeline

### ✔ Pipeline Flow Summary

1. GitHub → Jenkins checkout  
2. Backend Gradle Build  
3. Frontend Build  
4. Docker image build  
5. Docker save (.tar)  
6. Transfer to Deploy Server  
7. docker load → restart containers  

📸 Jenkins UI  
<img src="./docs(eng)/jenkins-dashboard.png" width="700">

📸 Jenkins Build Trend  
<img src="./docs(eng)/jenkins-trend.png" width="700">

---

### ✔ Jenkinsfile (Summary)

```groovy
pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build Backend') {
      steps { sh './gradlew clean build -x test' }
    }
    stage('Build Frontend') {
      steps { sh 'cd frontend && npm install && npm run build' }
    }
    stage('Docker Build') {
      steps { sh 'docker build -t matcha-backend ./backend' }
    }
    stage('Deploy') {
      steps {
        sh 'scp backend.tar root@10.0.2.6:/opt/matcha'
        sh 'ssh root@10.0.2.6 "docker load < backend.tar && docker-compose up -d"'
      }
    }
  }
}
```

## 🐳 6. Deploy Server (docker-compose)

```groovy
version: "3.8"

services:
  backend:
    image: matcha-backend:latest
    ports:
      - "8080:8080"

  frontend:
    image: matcha-frontend:latest
    ports:
      - "80:80"

  db:
    image: mariadb:10.6
    environment:
      MYSQL_ROOT_PASSWORD: 1234
```

📸 docker-compose execution
<img src="./docs(eng)/docker-compose-run.png" width="700">

## 🌐 7. LoadBalancer Configuration
Item	Value
LB Subnets	public-subnet-1, public-subnet-2
Target Group	Deploy Server Backend (8080)
Health Check	/actuator/health

📸 LB Health Check
<img src="./docs(eng)/lb1.png" width="700">
<img src="./docs(eng)/lb1.png" width="700">
<img src="./docs(eng)/lb1.png" width="700">

## 🎉 8. Service Result Screens
<img src="./docs(eng)/web1.png" width="700">
<img src="./docs(eng)/web2.png" width="700"> 
<img src="./docs(eng)/web3.png" width="700">

## 📝 9. Project Structure
PORTFOLIO         
 ├── Deploy             
 │    ├── AWS          
 │    ├── GCP            
 │    └── NCP   ← This document           
 ├── Matcha     ← ESG FullStack App               
 ├── Jenkinsfile          
 └── README.md                 

## ⭐ 10. Key Takeaways
✔ Practical DevOps CI/CD environment on NCP
✔ Jenkins Public + Deploy Private architecture (secure)
✔ NAT Gateway enables outbound communication from private subnet
✔ LoadBalancer provides stable backend service
✔ Docker-based automated build & deploy pipeline
✔ Fully automated workflow: GitHub → Jenkins → Deploy Server → LB