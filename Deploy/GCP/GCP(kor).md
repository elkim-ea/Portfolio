## GCP 기반 Kubernetes (GKE Autopilot) CI/CD 배포 구축

React(Vite) + Spring Boot + MariaDB + Docker + GitHub Actions + Artifact Registry(GAR) + GKE Autopilot + External LoadBalancer + Cloud NAT

본 프로젝트는 **Google Cloud Platform(GCP)** 환경에서  
Frontend(React/Vite) + Backend(Spring Boot) + DB(MariaDB)를 **GKE Autopilot(Kubernetes)** 로 운영하고,  
GitHub Actions로 **자동 빌드/배포(CI/CD)** 까지 구성한 실전형 DevOps

- Source Push 발생 → GitHub Actions 트리거  
- Docker 이미지 빌드 → Artifact Registry(GAR) Push  
- GKE Autopilot 배포 적용 → **Runtime에서 이미지 Pull**  
- Frontend/Backend는 각각 **Service type LoadBalancer** 로 외부 공개  
- 클러스터의 Outbound 트래픽은 **Cloud NAT** 기반으로 처리(Private egress)


## 1. 전체 아키텍처 개요

아래는 시스템의 전체 CI/CD 및 Runtime 흐름을 요약한 아키텍처         
(점선 = CI/CD, 실선 = Runtime 트래픽/동작)

**GKE Autopilot 최종 아키텍처 다이어그램**  

<img src="./docs/architecture-gke-final.png" width="800">


## 2. CI/CD + Runtime 전체 동작 흐름

### ✔ 전체 동작 흐름 (번호 기준)

| 번호 | 동작 |
|------|-------------------------------------------------------------|
| 1 | Source Push 발생 (GitHub Repository) |
| 2 | GitHub Actions Workflow 트리거 |
| 3 | GitHub Actions에서 Docker Build (Frontend/Backend) |
| 4 | Docker 이미지를 Artifact Registry(GAR)로 Push |
| 5 | GKE Autopilot에서 **Runtime Image Pull** (Pod 생성/재시작/롤링업데이트 시) |
| 6 | Client → Frontend External LoadBalancer로 접속 |
| 7 | Client/Admin → Backend External LoadBalancer로 API 접근 |
| 8 | Frontend Pod → Backend Pod (Internal API 호출) |
| 9 | Backend Pod → DB Pod (DB Internal) |
| 10 | Pod → Cloud NAT → Internet (Outbound Only) |

> 포인트: **Image Pull은 CI/CD가 아니라 Runtime 동작**이므로, 점선이 아니라 **실선**으로 표현


## 3. GCP 리소스 구성 (Project / Network / Registry / GKE)

### ✔ 3-1) Project 정보

| 항목 | 값 |
|------|--------------------|
| Project Name | matcha |
| Project ID | matcha-480312 |
| Region | asia-northeast3 |

Project 정보 이미지  

<img src="./docs/gcp-project-info.png" width="700">

### ✔ 3-2) VPC / Subnet 구성

| 항목 | 값 |
|------|--------------------|
| VPC | matcha-vpc (custom) |
| Region | asia-northeast3 |

#### ✔ Subnet 구성
| Subnet | CIDR | 역할 |
|--------|------|------|
| matcha-public-subnet | 10.0.1.0/24 | External 진입 지점(LB External IP 개념) |
| matcha-private-subnet | 10.0.2.0/24 | (Reserved) 향후 Cloud SQL/VM 등 Private 리소스 확장 |
| matcha-gke-subnet | 10.0.3.0/24 | GKE 노드/서비스 네트워크(클러스터 통신) |

#### ✔ Secondary Range (Pod CIDR)
| Range | CIDR | 역할 |
|------|------|------|
| GKE Pod Secondary Range | 10.219.0.0/17 | Pod IP 전용 |

Subnet / Secondary Range UI 이미지  

<img src="./docs/gcp-subnet-list.png" width="700">

### ✔ 3-3) Cloud NAT (Outbound 전용)

| 항목 | 값 |
|------|--------------------|
| Router | matcha-router |
| NAT | matcha-nat |
| 역할 | 클러스터(Private Egress)의 외부 통신 Outbound 처리 |

Cloud NAT 설정/고정 IP 이미지  

<img src="./docs/gcp-cloud-nat.png" width="700">

### ✔ 3-4) Artifact Registry (Docker Images 저장소)

Artifact Registry는 **VPC/Subnet 내부가 아닌 GCP 관리형 서비스**이며,  
GitHub Actions가 빌드한 이미지를 저장하고 GKE가 Runtime에 Pull

| 항목 | 값 |
|------|--------------------|
| 서비스 | Artifact Registry (GAR) |
| Repo | matcha-repo |
| 내용 | Frontend / Backend Docker Images |
| 흐름 | Actions/Docker → GAR(push), GKE → GAR(pull) |

Artifact Registry 이미지 목록 이미지

<img src="./docs/gar-images.png" width="700">


## 4. GitHub Actions CI/CD 구성

### ✔ 4-1) Actions 실행 결과 (성공 증명)

Actions Runs 이미지

<img src="./docs/github-actions-runs.png" width="700">

### ✔ 4-2) Repository Secrets 구성

| Secret Name | 설명 |
|------------|------|
| GCP_PROJECT_ID | GCP Project ID |
| GCP_SA_KEY | Service Account JSON Key |
| GKE_CLUSTER | GKE Cluster 이름 |
| GKE_LOCATION | GKE Region |
| GAR_REPO | Artifact Registry Repository |
| API_BASE_URL | FE에서 호출할 Backend Base URL |
| IMG_BASE_URL | 업로드 이미지 Base URL |

GitHub Secrets 설정 이미지  

<img src="./docs/github-secrets.png" width="700">

### ✔ 4-3) Workflow 동작 요약

1) Checkout  
2) GCP 인증 (SA Key)  
3) Docker Build  
4) GAR Push  
5) GKE Credentials 획득  
6) kubectl apply로 배포  
7) 롤아웃/상태 확인

```yaml
name: Deploy to GKE Autopilot

on:
  push:
    branches: [ "main" ]

env:
  PROJECT_ID: ${{ secrets.GCP_PROJECT_ID }}
  GAR_LOCATION: asia-northeast3
  GAR_REPO: matcha-repo
  GKE_CLUSTER: ${{ secrets.GKE_CLUSTER }}
  GKE_LOCATION: ${{ secrets.GKE_LOCATION }}

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Authenticate to GCP
        uses: google-github-actions/auth@v2
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}

      - name: Setup gcloud
        uses: google-github-actions/setup-gcloud@v2

      - name: Configure Docker
        run: gcloud auth configure-docker $GAR_LOCATION-docker.pkg.dev --quiet

      - name: Build & Push Images
        run: |
          docker build -t $GAR_LOCATION-docker.pkg.dev/$PROJECT_ID/$GAR_REPO/matcha-frontend:latest ./Matcha/frontend
          docker push $GAR_LOCATION-docker.pkg.dev/$PROJECT_ID/$GAR_REPO/matcha-frontend:latest

      - name: Get GKE Credentials
        run: gcloud container clusters get-credentials $GKE_CLUSTER --region $GKE_LOCATION --project $PROJECT_ID

      - name: Deploy Kubernetes
        run: kubectl apply -f Deploy/GCP/k8s/
```

## 5. Kubernetes 리소스 구성 (GKE Autopilot)

GKE Autopilot 환경에서 Frontend / Backend / DB를 각각 Kubernetes 리소스로 분리하여 운영 
외부 공개가 필요한 Frontend/Backend는 **Service type: LoadBalancer**, DB는 **ClusterIP(Internal Only)** 로 구성

### ✔ 5-1) 리소스 구성 요약

| 컴포넌트 | Deployment/Pod | Service Type | 외부 공개 | 설명 |
|---------|-----------------|-------------|----------|------|
| Frontend | Deployment / Pod | LoadBalancer | ✅ | 사용자 UI 제공 (External IP) |
| Backend | Deployment / Pod | LoadBalancer | ✅ | API 제공 (External IP) |
| DB (MariaDB) | Pod(또는 Stateful 구성) | ClusterIP | ❌ | 내부 통신 전용 |

`kubectl get pods & kubectl get svc` (실행 증명 External IP / Service 타입 증명) 

<img src="./docs/kubectl-svc-pods.png" width="700">

- **Service(LB)와 Pod를 분리**해서 “외부 진입 지점”을 명확
- **DB는 외부 노출하지 않고 Cluster 내부 통신(ClusterIP)** 으로만 접근하도록 구성
- Image Pull은 배포 파이프라인이 아니라 **Pod 생성/재시작/롤링업데이트 시점(Runtime)** 에 발생

k8s manifest 폴더 구조 이미지  

<img src="./docs/k8s-yaml-tree.png" width="700">

## 6. 서비스 결과 화면 (실제 동작)

- **Frontend (External LB)**: `http://34.64.88.163/`
- **Backend (External LB)**: `http://34.64.177.36/`

ESG 소개 페이지  

<img src="./docs/result-esg.png" width="700">

로그인 화면 / 기능 동작  

<img src="./docs/result-login.png" width="700">

관리자 페이지(예: 사용자 관리)  

<img src="./docs/result-admin.png" width="700">

## 7. 전체 프로젝트 구조

```text
PORTFOLIO
 ├── Deploy
 │   ├── AWS
 │   ├── GCP
 │   │   ├── k8s
 │   │   ├── docs
 │   │   └── README.md   ← 본 문서
 │   └── NCP
 ├── Matcha              ← ESG FullStack App (Frontend/Backend)
 └── README.md
```

## 8. 핵심 요약

✔ GKE Autopilot 기반 Kubernetes 운영 경험  
✔ GitHub Actions 기반 CI/CD 자동화 구축 (Build → Push → Deploy)  
✔ Artifact Registry를 통한 이미지 저장/버전 관리  
✔ Front/Back을 각각 External LoadBalancer로 외부 공개  
✔ DB는 Cluster 내부 통신(ClusterIP)으로 보안성 유지  
✔ Cloud NAT 기반 Outbound 트래픽 제어(Private egress)
