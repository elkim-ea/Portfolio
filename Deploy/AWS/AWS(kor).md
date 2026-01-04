## 🚀 AWS 기반 ECS Fargate CI/CD 배포 구축

Spring Boot + MariaDB + Docker + GitHub Actions + Amazon ECR + ECS Fargate + ALB + Route 53 + CloudFront + S3 + NAT Gateway

본 프로젝트는 **Amazon Web Services(AWS)** 환경에서  
Frontend(S3 + CloudFront) + Backend(Spring Boot) + DB(MariaDB)를  
**ECS Fargate 기반 컨테이너 아키텍처**로 운영하고,  
GitHub Actions를 활용해 **자동 빌드/배포(CI/CD)** 까지 구성한 실전형 DevOps 프로젝트입니다.

- Source Push 발생 → GitHub Actions 트리거  
- Docker 이미지 빌드 → Amazon ECR Push  
- ECS Service 업데이트 → **Runtime에서 이미지 Pull**  
- Backend는 **ALB(Application Load Balancer)** 를 통해 외부 공개  
- Frontend는 **S3 + CloudFront** 로 정적 서비스 제공  
- Private Subnet 리소스의 Outbound 트래픽은 **NAT Gateway** 기반 처리


## 📌 1. 전체 아키텍처 개요

아래는 시스템의 전체 **CI/CD 및 Runtime 흐름**을 요약한 아키텍처입니다.  
(점선 = CI/CD, 실선 = Runtime 트래픽/동작)

🏗 **ECS Fargate 최종 아키텍처 다이어그램**

<img src="./docs/architecture-ecs-final.png" width="800">


## 🔄 2. CI/CD + Runtime 전체 동작 흐름

### ✔ 전체 동작 흐름 (번호 기준)

| 번호 | 동작 |
|------|-------------------------------------------------------------|
| 1 | Source Push 발생 (GitHub Repository) |
| 2 | GitHub Actions Workflow 트리거 |
| 3 | GitHub Actions에서 Docker Build |
| 4 | Docker 이미지를 Amazon ECR로 Push |
| 5 | ECS Fargate에서 **Runtime Image Pull** (Task 생성/재시작/롤링업데이트 시) |
| 6 | User → Route 53 → CloudFront → S3 (Frontend) |
| 7 | Client/Admin → Route 53 → ALB → Backend 접근 |
| 8 | ALB → ECS Task (Spring Boot API) |
| 9 | ECS Task → RDS MariaDB (Private) |
| 10 | ECS Task → NAT Gateway → Internet (Outbound Only) |

> 📌 포인트: 아키텍처 설계 포인트

#### ✔ Public / Private Subnet 분리
- 외부 트래픽은 **ALB만 Public Subnet**에 배치
- ECS Task / RDS는 **Private Subnet**에 배치하여 직접 접근 차단

#### ✔ Inbound / Outbound 트래픽 제어
- Inbound:  
  - Internet → ALB → ECS만 허용
- Outbound:  
  - ECS → NAT Gateway → Internet

#### ✔ Security Group 중심 보안 설계
- Security Group Reference 기반 접근 제어
  - ALB SG → ECS SG
  - ECS SG → RDS SG
- Subnet 단위 제어(NACL)는 기본 설정 유지

#### ✔ 무중단 배포 및 롤백 전략
- ALB Health Check(`/actuator/health`) 기반 트래픽 제어
- Task Definition Revision 기반 롤백 가능
- 신규 Task 장애 시 기존 Task 유지

#### ✔ 비용 최적화 고려
- ECS Fargate 사용으로 EC2 노드 상시 비용 제거
- NAT Gateway 단일 구성
- 최소 Task 수 유지 (`desiredCount = 1`)
- 필요 시 Auto Scaling 확장 가능 구조


## 🧱 3. AWS 리소스 구성 (Account / Network / Registry / ECS)

### ✔ 3-1) Account / Region 정보

| 항목 | 값 |
|------|--------------------|
| Cloud | AWS |
| Region | ap-northeast-2 (Seoul) |

📸 AWS Region / Account 정보 이미지  

<img src="./docs/aws-account-region.png" width="700">


### ✔ 3-2) VPC / Subnet 구성

| 항목 | 값 |
|------|--------------------|
| VPC | matcha-vpc |
| CIDR | 10.0.0.0/16 |

#### ✔ Subnet 구성

| Subnet | AZ | CIDR | 역할 |
|------|----|------|------|
| Public Subnet | a / c | 10.0.1.0/24, 10.0.2.0/24 | ALB, NAT Gateway |
| Private Subnet | a / c | 10.0.11.0/24, 10.0.12.0/24 | ECS Fargate, RDS |

📸 Subnet 구성 UI 이미지  

<img src="./docs/aws-subnet-list.png" width="700">


### ✔ 3-3) NAT Gateway (Outbound 전용)

| 항목 | 값 |
|------|--------------------|
| NAT Gateway | 1개 (AZ-a) |
| 역할 | Private Subnet 리소스의 외부 통신 Outbound 처리 |

- 비용 최적화를 위해 단일 NAT 구성
- Route Table 분리로 향후 AZ별 확장 가능

📸 NAT Gateway 설정 이미지  

<img src="./docs/aws-nat-gateway.png" width="700">


### ✔ 3-4) Amazon ECR (Docker Images 저장소)

Amazon ECR은 **VPC/Subnet 내부가 아닌 AWS 관리형 서비스**이며,  
GitHub Actions가 이미지를 Push하고 ECS가 Runtime에 Pull

| 항목 | 값 |
|------|--------------------|
| 서비스 | Amazon ECR |
| Repo | matcha-backend |
| 내용 | Backend Docker Image |
| 흐름 | Actions → ECR(push), ECS → ECR(pull) |

📸 ECR Repository / Image 목록 이미지  

<img src="./docs/aws-ecr-images.png" width="700">


## ⚙️ 4. GitHub Actions CI/CD 구성

### ✔ 4-1) Actions 실행 결과 (성공 증명)

📸 GitHub Actions Runs 이미지  

<img src="./docs/aws-github-actions-runs.png" width="700">


### ✔ 4-2) Repository Secrets 구성

| Secret Name | 설명 |
|------------|------|
| AWS_ACCESS_KEY_ID | AWS IAM Access Key |
| AWS_SECRET_ACCESS_KEY | AWS IAM Secret |
| AWS_REGION | ap-northeast-2 |
| ECS_CLUSTER | ECS Cluster 이름 |
| ECS_SERVICE | ECS Service 이름 |
| ECR_REPOSITORY | ECR Repository 이름 |

📸 GitHub Secrets 설정 이미지  

<img src="./docs/aws-github-secrets.png" width="700">


### ✔ 4-3) Workflow 동작 요약

1) Checkout  
2) AWS 인증 (IAM Credential)  
3) Docker Build  
4) Amazon ECR Push  
5) ECS Service Update  
6) Rolling Deployment 수행  

```yaml
name: Deploy Backend to AWS ECS

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:
```

## 🐳 5. ECS Fargate 리소스 구성

본 프로젝트에서는 Backend 애플리케이션을  
**Amazon ECS Fargate(Serverless Container)** 기반으로 운영했습니다.

외부 공개가 필요한 Backend는 **Application Load Balancer(ALB)** 를 통해서만 접근 가능하도록 구성했으며,  
실제 애플리케이션과 데이터베이스는 **Private Subnet** 에 배치해  
외부 직접 접근을 차단했습니다.


### ✔ 5-1) 리소스 구성 요약

| 컴포넌트 | ECS 구성 | 연결 방식 | 외부 공개 | 설명 |
|---------|---------|----------|----------|------|
| Backend | ECS Service / Task (Fargate) | ALB Target Group | ✅ | Spring Boot API |
| Database | RDS MariaDB | Private Endpoint | ❌ | 내부 통신 전용 |

📸 ECS Service / Task 실행 상태 (Running / Desired Count 확인)  

<img src="./docs/aws-ecs-service-task.png" width="700">


### ✔ 5-2) ECS Cluster / Service 구성

- **ECS Cluster**: `matcha-cluster`
- **ECS Service**: `matcha-backend-service`
- Launch Type: **Fargate**
- Network Mode: `awsvpc`

ECS Service는 다음 역할을 수행합니다:

- Task 수 관리 (`desiredCount`)
- ALB Target Group 자동 등록
- Health Check 기반 무중단 배포
- Rolling Update 전략 적용

📌 ECS Cluster는 **논리적 리소스**이며,  
실제 네트워크에 배치되는 것은 **ECS Task** 입니다.


### ✔ 5-3) ECS Task Definition 구성

ECS Task는 Spring Boot 기반 Backend 애플리케이션을 실행합니다.

- Container Image: **Amazon ECR**
- Container Port: `8080`
- Health Check:
  - Path: `/actuator/health`
- Logging:
  - CloudWatch Logs 연동

📌 Task Definition은 **Revision 기반**으로 관리되며,  
배포 실패 시 이전 Revision으로 즉시 롤백할 수 있습니다.

📸 Task Definition / Container 설정 이미지  

<img src="./docs/aws-task-definition.png" width="700">


### ✔ 5-4) 배포 및 롤링 업데이트 방식

ECS Service는 **Rolling Update 방식**으로 배포됩니다.

1. GitHub Actions에서 새로운 Docker Image를 ECR로 Push
2. ECS Service Update 실행
3. 새로운 Task 생성
4. ALB Health Check 통과 후 트래픽 전달
5. 기존 Task Drain 후 종료

📌 Health Check 실패 시 트래픽은 기존 Task로 유지되어  
**서비스 중단 없이 배포 실패를 감지**할 수 있습니다.


## 🎉 6. 서비스 결과 화면 (실제 동작)

- **Frontend (CloudFront)**: `https://matchaworld.shop`
- **Backend API (ALB)**: `https://api.matchaworld.shop`

📸 서비스 메인 화면  

<img src="./docs/aws-result-main.png" width="700">

📸 로그인 / 기능 동작 화면  

<img src="./docs/aws-result-login.png" width="700">

📸 관리자 페이지 (예: 사용자 관리)  

<img src="./docs/aws-result-admin.png" width="700">


## 📝 7. 전체 프로젝트 구조

```text
PORTFOLIO
 ├── Deploy
 │   ├── AWS
 │   │   ├── ecs
 │   │   ├── docs
 │   │   └── README.md   ← 본 문서
 │   ├── GCP
 │   └── NCP
 ├── Matcha              ← ESG FullStack App (Frontend / Backend)
 └── README.md
```

## ⭐ 8. 핵심 요약

✔ ECS Fargate 기반 **Serverless Container 운영 경험**  
✔ GitHub Actions 기반 **CI/CD 자동화 구축 (Build → Push → Deploy)**  
✔ Amazon ECR을 통한 **Docker 이미지 저장 및 버전 관리**  
✔ ALB를 통한 Backend 외부 공개 및 **Health Check 기반 무중단 배포**  
✔ Private Subnet 중심 보안 아키텍처 설계  
✔ NAT Gateway 기반 **Outbound Only 네트워크 제어**  
✔ 비용과 확장성을 고려한 실무형 인프라 구성
