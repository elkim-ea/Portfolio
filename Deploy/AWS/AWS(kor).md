## AWS 기반 ECS Fargate CI/CD + Terraform(IaC) 구축

Spring Boot + MariaDB + Docker + GitHub Actions + Amazon ECR  
ECS Fargate + ALB + Route 53 + CloudFront + S3 + NAT Gateway + Terraform

본 프로젝트는 **Amazon Web Services(AWS)** 환경에서  
Frontend(S3 + CloudFront) + Backend(Spring Boot) + DB(MariaDB)를  
**ECS Fargate 기반 컨테이너 아키텍처**로 운영한 실전형 DevOps 프로젝트

초기에는 **AWS 콘솔 중심으로 인프라를 구성하여 배포까지 완료**했으나,  
운영 이후 다음과 같은 한계를 명확히 인식하였음

- 인프라 설정이 코드로 남지 않아 **재현이 어려움**
- 변경 이력 추적 불가
- 환경을 다시 구성하려면 수작업 반복 필요

이에 따라 기존 AWS 리소스를 **정리(clean-up)** 한 뒤,  
Terraform을 도입하여 **AWS 인프라를 Infrastructure as Code(IaC)** 로  
다시 설계·구성

이 프로젝트는 단순 배포 경험이 아니라,  
**“문제 인식 → 구조적 개선 → 재설계”까지 포함한 DevOps 관점의 포트폴리오**


## 1. 전체 아키텍처 개요

아래는 시스템의 전체 **CI/CD 및 Runtime 흐름**을 요약한 아키텍처
(점선 = CI/CD, 실선 = Runtime 트래픽/동작)

**ECS Fargate 최종 아키텍처 다이어그램**

<img src="./docs/architecture-ecs-final.png" width="800">

### 아키텍처 설계 방향

- **Core Infrastructure**
  - VPC / Subnet / Security Group
  - Terraform으로 IaC 구성
- **Application / Runtime Layer**
  - ECS Fargate / ALB / RDS
  - GitHub Actions + AWS 콘솔 기반 운영

Terraform은 **변경 빈도가 낮고 재현이 중요한 인프라 영역**에 적용하고,  
운영 중 잦은 변경이 필요한 리소스는 CI/CD 중심으로 관리

## 2. CI/CD + Runtime 전체 동작 흐름

### ✔ 전체 동작 흐름 (번호 기준)

| 번호 | 동작 |
|------|-------------------------------------------------------------|
| 1 | Source Push 발생 (GitHub Repository) |
| 2 | GitHub Actions Workflow 트리거 |
| 3 | GitHub Actions에서 Docker Build |
| 4 | Docker 이미지를 Amazon ECR로 Push |
| 5 | ECS Fargate에서 **Runtime Image Pull** |
| 6 | User → Route 53 → CloudFront → S3 (Frontend) |
| 7 | Client/Admin → Route 53 → ALB → Backend 접근 |
| 8 | ALB → ECS Task (Spring Boot API) |
| 9 | ECS Task → RDS MariaDB (Private) |
| 10 | ECS Task → NAT Gateway → Internet (Outbound Only) |

GitHub Actions 실행 결과

<img src="./docs/github-actions-runs.png" width="700">

## 3. Terraform 기반 Infrastructure as Code (IaC)

### ✔ Terraform 도입 배경

AWS 배포를 완료한 이후,  
**IaC가 없는 인프라는 운영·확장·복구 관점에서 한계가 명확**하다고 판단

- 동일한 환경을 다시 만들 수 없음
- 설정 변경 이력 관리 불가
- 협업 및 확장에 취약

이에 따라 기존 AWS 리소스를 제거한 뒤,  
Terraform으로 **인프라를 처음부터 다시 구성**

### ✔ Terraform 적용 범위

본 프로젝트에서는 **모든 리소스를 Terraform으로 관리하지 않고**,  
다음 기준에 따라 적용 범위를 명확히 구분

| 구분 | 적용 방식 |
|----|----|
| VPC / Subnet | Terraform |
| Security Group | Terraform |
| ECS / ALB / RDS | 콘솔 + CI/CD |
| Application 배포 | GitHub Actions |

**IaC의 장점과 운영 편의성을 균형 있게 고려한 설계**

### ✔ Terraform 리소스 구성 요약

| 리소스 | 설명 |
|------|------|
| Provider | AWS (ap-northeast-2) |
| VPC | matcha-vpc (10.0.0.0/16) |
| Public Subnet | 10.0.1.0/24 |
| Security Group | 80 / 443 Inbound 허용 |
| Tag | Name, Environment |

### ✔ Terraform 코드 예시

```hcl
provider "aws" {
  region = "ap-northeast-2" # 서울 리전
}

# 2. VPC 생성 (이름: matcha-vpc, 대역대: 10.0.0.0/16)
resource "aws_vpc" "main" {
  cidr_block       = "10.0.0.0/16"
  instance_tenancy = "default"

  tags = {
    Name = "matcha-vpc"
    Environment = "Portfolio"
  }
}
```

Terraform apply 성공 화면

<img src="./docs/terraform-apply-success.png" width="700">

Terraform 파일 구조

<img src="./docs/terraform-aws-tree.png" width="700">

## 4. AWS 네트워크 구성 (Terraform 재설계)

AWS 배포를 완료한 이후,  
**네트워크 및 보안 설정이 코드로 남지 않는 문제**를 명확히 인식했고  
이를 해결하기 위해 기존 리소스를 정리한 뒤  
Terraform을 통해 네트워크 인프라를 다시 구성

### ✔ 4-1) Account / Region 정보

| 항목 | 값 |
|------|--------------------|
| Cloud | AWS |
| Region | ap-northeast-2 (Seoul) |

<img src="./docs/aws-account-region.png" width="700">

### ✔ 4-2) VPC 구성 (Terraform)

| 항목 | 값 |
|------|--------------------|
| VPC Name | matcha-vpc |
| CIDR | 10.0.0.0/16 |

<img src="./docs/aws-vpc-list.png" width="700">

### ✔ 4-3) Subnet 구성

| Subnet | AZ | CIDR | 역할 |
|------|----|------|------|
| Public Subnet | a / c | 10.0.1.0/24, 10.0.2.0/24 | ALB, NAT Gateway |
| Private Subnet | a / c | 10.0.11.0/24, 10.0.12.0/24 | ECS Fargate, RDS |

<img src="./docs/aws-subnet-list.png" width="700">

#### 🔹 설계 포인트
- 외부 트래픽은 **ALB만 Public Subnet**에 배치
- 애플리케이션(ECS Task)과 DB는 **Private Subnet**에 배치
- 외부 직접 접근 완전 차단

### ✔ 4-4) Security Group 구성

| Security Group   | 역할           | 설명                            |
| ---------------- | ------------ | ----------------------------- |
| `alb-sg`         | ALB 전용       | 인터넷(80/443) → ALB 인바운드 허용     |
| `ecs-sg`         | ECS 공통       | ECS Task 기본 보안 그룹             |
| `ecs-backend-sg` | Backend 전용   | ALB → ECS Backend (8080) 허용   |
| `rds-sg`         | DB 전용        | ECS Task → RDS(3306) 내부 통신 허용 |
| `vpce-sg`        | VPC Endpoint | VPC Endpoint 접근 제어            |
| `default`        | 기본           | 사용하지 않음 (의도적 미사용)             |

<img src="./docs/aws-security-group.png" width="700">

- ECS Backend는 ALB를 통해서만 접근 가능
- DB(RDS)는 ECS Task Security Group에서만 접근 허용
- 외부 → ECS / DB 직접 접근 완전 차단

## 5. GitHub Actions 기반 CI/CD 구성

애플리케이션 배포는 GitHub Actions를 통해 자동화

<img src="./docs/github-secrets-aws.png" width="700">

<img src="./docs/aws-ecr-images.png" width="700">

### ✔ CI/CD 역할 분리

| 구분 | 담당 |
|----|----|
| Infrastructure | Terraform |
| Application Build | GitHub Actions |
| Image Registry | Amazon ECR |
| Runtime 배포 | ECS Fargate |

### ✔ CI/CD 전체 흐름

1. Source Push
2. GitHub Actions Workflow
3. Docker Build & Push
4. Image reference update

```yaml
name: Backend CI/CD to AWS ECS

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:
```

## 6. ECS Fargate 기반 애플리케이션 구성

본 프로젝트에서는 EC2를 직접 관리하지 않고,  
**Amazon ECS Fargate(Serverless Container)** 를 사용해 Backend 애플리케이션을 운영

Fargate를 선택함으로써  
- 서버 프로비저닝 및 OS 관리 부담 제거  
- 애플리케이션과 인프라 책임 영역 분리  
- 배포 자동화에 집중할 수 있는 환경  
을 구성

### ✔ 6-1) 리소스 구성 요약

| 컴포넌트 | 구성 | 외부 공개 | 설명 |
|--------|------|----------|------|
| Backend | ECS Fargate Service | ✅ (ALB) | Spring Boot API |
| Database | RDS MariaDB | ❌ | Private Subnet 내부 통신 |

- Backend는 **ALB 단일 진입점**을 통해서만 접근 가능
- Database는 **Private Subnet**에 배치하여 외부 접근 차단

### ✔ 6-2) ECS Cluster / Service 구성

- **ECS Cluster**: `matcha-cluster`
- **ECS Service**: `matcha-backend-service`
- Launch Type: **Fargate**
- Network Mode: `awsvpc`

<img src="./docs/aws-ecs-cluster.png" width="700">

<img src="./docs/aws-ecs-service-running.png" width="700">

ECS Service는 다음 역할을 담당

- Task 수 관리 (`desiredCount`)
- ALB Target Group 자동 등록
- Health Check 결과 기반 트래픽 제어
- Rolling Update 전략 적용

ECS Cluster는 논리적인 그룹이며,  
실제 네트워크에 배치되는 단위는 **ECS Task** 

### ✔ 6-3) ECS Task Definition 구성

ECS Task Definition은 Backend 애플리케이션 실행 단위

- Container Image: **Amazon ECR**
- Container Port: `8080`
- Health Check Path: `/actuator/health`
- Logging: **CloudWatch Logs 연동**

<img src="./docs/aws-task-definition.png" width="700">

Task Definition은 **Revision 단위로 관리**되며,  
배포 실패 시 이전 Revision으로 즉시 롤백할 수 있도록 구성

### ✔ 6-4) 무중단 배포 (Rolling Update) 전략

ECS Service는 **Rolling Update 방식**으로 배포

1. GitHub Actions에서 새로운 Docker Image를 ECR로 Push
2. ECS Service Update 실행
3. 새로운 Task 생성
4. ALB Health Check 통과 후 트래픽 전달
5. 기존 Task Drain 후 종료

<img src="./docs/aws-alb-target-group.png" width="700">

<img src="./docs/aws-alb-healthcheck.png" width="700">

Health Check 실패 시  
기존 Task로 트래픽이 유지되어 **서비스 중단 없이 배포 실패를 감지**

## 7. 서비스 결과 및 검증

GitHub Actions 기반 CI/CD 파이프라인을 통해  
실제 서비스가 정상적으로 운영됨을 확인

- **Frontend (CloudFront)**  

  https://matchaworld.shop

<img src="./docs/aws-result-frontend.png" width="700">

- **Backend API (CloudWatch)**  

<img src="./docs/aws-result-backend-api.png" width="700">

사용자 요청은 다음 흐름으로 처리

Client → Route53 → CloudFront → ALB → ECS Task → RDS

<img src="./docs/aws-result-login.png" width="700">

<img src="./docs/aws-result-admin.png" width="700">

## 8. 전체 프로젝트 구조

```text
PORTFOLIO
 ├── Deploy
 │   ├── AWS
 │   │   ├── docs
 │   │   └── README.md
 │   ├── GCP
 │   └── NCP
 ├── Terraform
 │   └── AWS
 │       ├── main.tf
 │       └── .terraform.lock.hcl
 ├── Matcha
 │   ├── frontend
 │   └── backend
 └── README.md
```

## 핵심 요약

✔ ECS Fargate 기반 Serverless Container 운영 경험
✔ GitHub Actions 기반 CI/CD 자동화 구축
✔ IaC 부재를 문제로 인식하고 Terraform으로 인프라 재설계
✔ 콘솔 중심 AWS 환경의 한계를 이해하고 구조적으로 개선
✔ 운영 · 보안 · 재현성을 고려한 DevOps 관점 아키텍처 설계


