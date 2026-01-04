# ☁️ Multi-Cloud DevOps Comparison Portfolio  
**Deploying the Same Service on NCP / GCP / AWS**

> 📂 **Note:**  
> Detailed deployment portfolio for each cloud is available in:  
> - `PORTFOLIO/Deploy/AWS/`  
> - `PORTFOLIO/Deploy/GCP/`  
> - `PORTFOLIO/Deploy/NCP/` 


## 1. Common Project Overview

I deployed the **same application** to three different cloud providers.

- **Common Application**
  - Frontend: React + Vite (+ Nginx static hosting)
  - Backend: Spring Boot (Gradle, JAR)
  - Database: MariaDB
  - Container: Docker

- **Shared DevOps Concepts**
  - VPC / Subnet design (Public / Private separation)
  - NAT-based outbound-only internet access
  - Managing external entry points with Load Balancer / CloudFront / Service(LB)
  - CI/CD pipelines for automated build & deployment

> The key point is that I designed **different infrastructure and service combinations for the same app**.


## 2. Architecture Summary by Cloud

### 2.1 NCP – Jenkins + VM + Docker Compose

**Keywords:** `Jenkins`, `VM`, `docker-compose`, `NAT Gateway`, `NCP Load Balancer`

- **Deployment Flow**
  - GitHub Webhook → Jenkins build trigger
  - Jenkins:
    - Builds Docker images for backend/frontend
    - Exports images as tar file and transfers to Deploy server using `scp`
  - On the Deploy server (Private Subnet), containers are started with `docker-compose up -d`

- **Runtime Architecture**
  - Jenkins server: Public Subnet (build/management)
  - Deploy server: Private Subnet
    - Backend, Frontend, and MariaDB all run via docker-compose
  - Only the backend is registered to the NCP Load Balancer Target Group (public access)
  - Frontend serves on port 80 inside the Deploy server (internal only)

- **Network Design**
  - VPC with multiple subnets:
    - Public Subnet (internet entry point)
    - Private Subnet (application/DB)
    - NAT Subnet + NAT Gateway (outbound-only)
    - CI/CD Subnet (Jenkins)
  - ACG (Access Control Group) used to control ports and source IPs
  - LB Health Check: `/actuator/health`


### 2.2 GCP – GKE Autopilot + Artifact Registry + GitHub Actions

**Keywords:** `GKE Autopilot`, `Kubernetes`, `Artifact Registry (GAR)`, `Cloud NAT`, `GitHub Actions`

- **Deployment Flow**
  - GitHub Actions:
    - Builds Docker images for backend/frontend
    - Pushes images to Artifact Registry (GAR)
    - Applies Kubernetes manifests (`kubectl apply`) to GKE Autopilot (Deployment / Service)
  - When Pods are created, GKE pulls images from GAR at runtime

- **Runtime Architecture**
  - GKE Autopilot Cluster:
    - Frontend Deployment + Service (type: LoadBalancer)
    - Backend Deployment + Service (type: LoadBalancer)
    - MariaDB Pod (ClusterIP – internal-only)
  - Each Service exposes an External IP for accessing FE/BE

- **Network Design**
  - Custom VPC:
    - Public / Private Subnets
    - GKE Subnet + Pod Secondary Range (Pod CIDR)
  - Cloud NAT handles outbound traffic from the cluster
  - Inbound traffic enters via GKE Service(type: LoadBalancer)


### 2.3 AWS – ECS Fargate + ALB + S3/CloudFront + RDS

**Keywords:** `ECS Fargate`, `ALB`, `ECR`, `S3 + CloudFront`, `RDS`, `GitHub Actions`

- **Deployment Flow**
  - GitHub Actions:
    - Builds backend Docker image
    - Pushes the image to Amazon ECR
    - Calls `aws ecs update-service` to update the ECS Service (rolling deployment with new Tasks)
  - Frontend:
    - Builds Vite static files
    - Uploads build artifacts to S3
    - CloudFront uses S3 as its origin

- **Runtime Architecture**
  - Backend: ECS Fargate Tasks behind an ALB
  - Frontend: S3 static hosting + CloudFront (with custom domain, e.g. `https://matchaworld.shop`)
  - Database: RDS MariaDB (Private Subnet, Managed Service)

- **Network Design**
  - VPC (e.g. `10.0.0.0/16`)
    - Public Subnets: ALB, NAT Gateway
    - Private Subnets: ECS Fargate, RDS
  - Security Group design:
    - ALB SG → ECS SG (port 8080)
    - ECS SG → RDS SG (port 3306)
  - ALB Health Check: `/actuator/health` for target status


## 3. Key Comparison (Table View)

### 3.1 Infra / Orchestration

| Item               | NCP                                     | GCP                                           | AWS                                             |
| ------------------ | --------------------------------------- | --------------------------------------------- | ----------------------------------------------- |
| Orchestration      | None (VM + docker-compose)             | **GKE Autopilot (Kubernetes)**                | **ECS Fargate (Serverless Container)**          |
| Execution unit     | Containers (docker-compose)            | Pod / Deployment                              | Task / Service                                  |
| Node management    | I manage VMs manually                  | Autopilot manages nodes                       | Fargate (no node management, task-level only)   |
| Scaling model      | Manual server/container scaling        | Change replica count, HPA possible            | Change desiredCount / Auto Scaling              |


### 3.2 Frontend Deployment Patterns

| Item           | NCP                                             | GCP                                                 | AWS                                               |
| -------------- | ----------------------------------------------- | --------------------------------------------------- | ------------------------------------------------- |
| Deployment     | Container on Deploy server in Private Subnet   | GKE Deployment + Service(type: LoadBalancer)        | **S3 static hosting + CloudFront**               |
| Public access  | ❌ Internal only                               | ✅ External IP via Service(LB)                      | ✅ Custom domain + HTTPS                          |
| Characteristics| Simple setup via docker-compose with backend   | Frontend also runs as a container in Kubernetes     | Common production pattern using S3 + CloudFront   |


### 3.3 Backend / DB Design

| Item      | NCP                                                  | GCP                                             | AWS                                                      |
| --------- | ---------------------------------------------------- | ----------------------------------------------- | -------------------------------------------------------- |
| Backend   | Docker container on Deploy server + NCP Load Balancer| GKE Deployment + Service(type: LoadBalancer)    | ECS Fargate + ALB                                        |
| DB        | MariaDB container in docker-compose                  | MariaDB Pod (ClusterIP, in-cluster only)        | **RDS MariaDB (Managed Service)**                        |
| HealthCheck | LB: `/actuator/health`                            | Service / Pod + `/actuator/health`             | ALB Health Check: `/actuator/health`                     |


### 3.4 CI/CD Pipeline Comparison

| Item          | NCP (Jenkins)                                                   | GCP (GitHub Actions + GKE)                          | AWS (GitHub Actions + ECS)                                 |
| ------------- | --------------------------------------------------------------- | --------------------------------------------------- | ---------------------------------------------------------- |
| Trigger       | GitHub Webhook → Jenkins                                        | GitHub push → Actions                               | GitHub push → Actions                                      |
| Build location| Jenkins server (VM)                                             | GitHub Actions Runner                               | GitHub Actions Runner                                      |
| Image registry| Local Docker + tar (transferred to Deploy server)               | Artifact Registry (GAR)                             | Amazon ECR                                                 |
| Deployment    | `ssh` / `scp` + `docker-compose up -d`                          | `kubectl apply` (Deployment & Service)              | `aws ecs update-service` (rolling update with new Tasks)   |
| Key point     | On-premise style CI/CD with Jenkins                             | Container-native CI/CD and Kubernetes deployment    | ECR + ECS + ALB with blue/green-style rolling deployments  |


### 3.5 Network / Security Architecture

| Item           | NCP                                                         | GCP                                                   | AWS                                                              |
| -------------- | ----------------------------------------------------------- | ----------------------------------------------------- | ---------------------------------------------------------------- |
| VPC/Subnet     | Designed Public / Private / NAT / CI/CD Subnets            | Custom VPC + GKE Subnet + Pod CIDR                    | VPC with Public/Private Subnets across AZs                       |
| Outbound       | NAT Gateway                                                 | Cloud NAT                                             | NAT Gateway                                                      |
| Inbound        | NCP Load Balancer → Backend container                       | Service(type: LoadBalancer) → GKE Pod                 | Route53 → (ALB / CloudFront) → ECS or S3                         |
| Security       | ACG-based port & IP control                                | VPC + GKE security + Service/Ingress policies         | SG reference chain (ALB SG → ECS SG → RDS SG)                    |


## 4. Design Rationale (Why This Architecture?)

### 4.1 NCP – DevOps & Network Fundamentals

- Goal: get **hands-on experience from scratch**
- Installed and configured Jenkins on my own, set up builds, exported Docker images as tar files, and deployed them via `scp` and `ssh`
- Designed VPC/Subnets/NAT/LB/ACG end-to-end on NCP
- Focused on understanding:
  - How CI/CD works under the hood
  - How traffic flows between public/private subnets and through a Load Balancer

> **Mindset:** “Before relying on managed DevOps tools, I wanted to really understand how servers and networks work.”

### 4.2 GCP – Cloud-Native with Kubernetes at the Center

- Goal: run the **same app** on Kubernetes
- Built a cloud-native CI/CD flow:
  - GitHub Actions → GAR → GKE Autopilot
- Learned how Kubernetes resources work together:
  - Deployment, Pod, Service(type: LoadBalancer), ClusterIP
- Designed Cloud NAT for GKE so that:
  - Pods can pull images from GAR
  - Outbound traffic is controlled centrally

> This taught me how real services are structured on Kubernetes, and how CI/CD integrates into that workflow.

### 4.3 AWS – Production-Like 3-Tier Architecture

- Goal: build an architecture that looks close to **real production**:
  - FE: S3 + CloudFront (+ Route53 custom domain, HTTPS)
  - BE: ECS Fargate + ALB
  - DB: RDS MariaDB
- Practiced **zero-downtime deployments**:
  - ECS rolling updates
  - ALB health checks ensuring only healthy tasks receive traffic
- Designed a secure DB layer:
  - RDS in private subnets
  - Only ECS tasks can access DB (via SG references)
  - No direct DB access from the public internet

> This project reflects the kind of architecture commonly used in real companies for production workloads.


## 5. Overall Reflection

- I deployed **the same full-stack application** using:
  - NCP: Jenkins + VM + docker-compose
  - GCP: GKE Autopilot + GitHub Actions + GAR
  - AWS: ECS Fargate + ALB + S3/CloudFront + RDS  
  and experienced the differences in abstraction level and operations.

- Across all three:
  - I built VPCs, subnets, NAT, load balancers, domains, HTTPS, and CI/CD pipelines myself.
  - I realized that regardless of tools or cloud:
    - The core DevOps flow is always  
      **Build → Create image → Push to registry → Runtime pulls → Shift traffic safely**.


## 6. One-Line Portfolio Summary

> I deployed a single ESG-themed full-stack application to  
> **NCP (Jenkins + VM), GCP (GKE Autopilot), and AWS (ECS Fargate + S3/CloudFront + RDS)**,  
> and through this, I practiced everything from basic network design to Kubernetes,  
> serverless containers, managed databases, CI/CD automation, and zero-downtime deployments,  
> using three different cloud-native architectures.
