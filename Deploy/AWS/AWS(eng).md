## AWS-Based ECS Fargate CI/CD + Terraform (IaC) Implementation

Spring Boot + MariaDB + Docker + GitHub Actions + Amazon ECR  
ECS Fargate + ALB + Route 53 + CloudFront + S3 + NAT Gateway + Terraform

This project is a **hands-on DevOps project** operated on **Amazon Web Services (AWS)**,  
running Frontend (S3 + CloudFront) + Backend (Spring Boot) + DB (MariaDB)  
using an **ECS Fargate–based container architecture**.

Initially, the infrastructure was built and deployed **primarily through the AWS Console**,  
but during operation, the following limitations became clear:

- Infrastructure configuration was not stored as code, making **reproducibility difficult**
- No way to track configuration change history
- Rebuilding environments required repeated manual work

As a result, existing AWS resources were **cleaned up**,  
and **Terraform** was introduced to redesign and rebuild the AWS infrastructure  
as **Infrastructure as Code (IaC)**.

This project goes beyond simple deployment experience and represents  
a DevOps-oriented portfolio covering  
**“problem recognition → structural improvement → re-architecture.”**


## 1. Overall Architecture Overview

Below is an overview of the system’s complete **CI/CD and runtime flow**.  
(Dashed lines = CI/CD, Solid lines = Runtime traffic/operations)

**Final ECS Fargate Architecture Diagram**

<img src="./docs/architecture-ecs-final.png" width="800">

### Architecture Design Principles

- **Core Infrastructure**
  - VPC / Subnet / Security Group
  - Managed as IaC using Terraform
- **Application / Runtime Layer**
  - ECS Fargate / ALB / RDS
  - Operated using GitHub Actions + AWS Console

Terraform is applied to **low-change, high-reproducibility infrastructure components**,  
while resources requiring frequent operational changes are managed through CI/CD.

## 2. CI/CD + Runtime End-to-End Flow

### ✔ End-to-End Flow (by sequence number)

| No. | Operation |
|------|-------------------------------------------------------------|
| 1 | Source push occurs (GitHub Repository) |
| 2 | GitHub Actions workflow is triggered |
| 3 | Docker build executed in GitHub Actions |
| 4 | Docker image is pushed to Amazon ECR |
| 5 | ECS Fargate performs **runtime image pull** |
| 6 | User → Route 53 → CloudFront → S3 (Frontend) |
| 7 | Client/Admin → Route 53 → ALB → Backend |
| 8 | ALB → ECS Task (Spring Boot API) |
| 9 | ECS Task → RDS MariaDB (Private) |
| 10 | ECS Task → NAT Gateway → Internet (Outbound Only) |

GitHub Actions Execution Result

<img src="./docs/github-actions-runs.png" width="700">

## 3. Terraform-Based Infrastructure as Code (IaC)

### ✔ Background for Adopting Terraform

After completing AWS deployment, it became clear that  
**infrastructure without IaC has critical limitations** from an operational perspective:

- Identical environments cannot be recreated
- Configuration changes cannot be tracked
- Weak foundation for collaboration and scalability

Therefore, existing AWS resources were removed,  
and the infrastructure was **rebuilt from scratch using Terraform**.

### ✔ Terraform Scope Definition

Rather than managing all resources with Terraform,  
the scope was clearly defined based on the following criteria:

| Category | Management Approach |
|----|----|
| VPC / Subnet | Terraform |
| Security Group | Terraform |
| ECS / ALB / RDS | Managed operationally (Console / CI/CD), not fully IaC |
| Application Deployment | GitHub Actions |

This design balances **IaC benefits with operational flexibility**.

### ✔ Terraform Resource Summary

| Resource | Description |
|---------|-------------|
| Provider | AWS (ap-northeast-2) |
| VPC | matcha-vpc (10.0.0.0/16) |
| Public Subnet | 10.0.1.0/24 |
| Security Group (ALB) | Allow inbound HTTPS (443) from Internet |
| Security Group (ECS) | Allow inbound 8080 from ALB only |
| Tags | Name, Environment |

### ✔ Terraform Code Example

```hcl
provider "aws" {
  region = "ap-northeast-2"
}

resource "aws_vpc" "main" {
  cidr_block       = "10.0.0.0/16"
  instance_tenancy = "default"

  tags = {
    Name = "matcha-vpc"
    Environment = "Portfolio"
  }
}
```

Terraform Apply Success Screen

<img src="./docs/terraform-apply-success.png" width="700">

Terraform File Structure

<img src="./docs/terraform-aws-tree.png" width="700">


## 4. AWS Network Architecture (Terraform Redesign)

After completing the AWS deployment,  
it became clear that network and security configurations were not preserved as code.  
To address this issue, existing resources were cleaned up, and  
the network infrastructure was rebuilt using Terraform.

### ✔ 4-1) Account / Region Information

| Item | Value |
|------|--------------------|
| Cloud | AWS |
| Region | ap-northeast-2 (Seoul) |

<img src="./docs/aws-account-region.png" width="700">

### ✔ 4-2) VPC Configuration (Terraform)

| Item | Value |
|------|--------------------|
| VPC Name | matcha-vpc |
| CIDR | 10.0.0.0/16 |

<img src="./docs/aws-vpc-list.png" width="700">

### ✔ 4-3) Subnet Configuration

| Subnet | AZ | CIDR | Role |
|------|----|------|------|
| Public Subnet | a / c | 10.0.1.0/24, 10.0.2.0/24 | ALB, NAT Gateway |
| Private Subnet | a / c | 10.0.11.0/24, 10.0.12.0/24 | ECS Fargate, RDS |

<img src="./docs/aws-subnet-list.png" width="700">

#### 🔹 Design Points

- External traffic enters **only through ALB** in Public Subnets  
- Application workloads (ECS Tasks) and the database are placed in **Private Subnets**  
- **Direct external access is completely blocked**

### ✔ 4-4) Security Group Configuration

| Security Group | Role | Description |
|---------------|------|-------------|
| alb-sg | ALB only | Allow inbound from Internet (80/443) |
| ecs-sg | ECS common | Base security group for ECS Tasks |
| ecs-backend-sg | Backend only | Allow ALB → ECS Backend (8080) |
| rds-sg | DB only | Allow ECS Task → RDS (3306) |
| vpce-sg | VPC Endpoint | Control VPC Endpoint access |
| default | Default | Intentionally unused |

<img src="./docs/aws-security-group.png" width="700">

- Backend ECS is accessible **only through ALB**
- RDS is accessible **only from ECS Task Security Groups**
- Direct external access to ECS / DB is completely blocked

### ✔ 4-5) Cost Optimization Strategy
- **NAT Gateway Consolidation**: To minimize operational costs in the development environment, a single NAT Gateway was utilized across multiple Availability Zones instead of one per AZ.
- **VPC Endpoints (PrivateLink)**: Implemented Interface Endpoints for ECR and S3 Gateway Endpoints. This significantly reduced NAT Gateway data processing fees by keeping internal AWS traffic within the private network.

## 5. GitHub Actions–Based CI/CD Pipeline

Application deployment is fully automated using GitHub Actions.

<img src="./docs/github-secrets-aws.png" width="700">
<img src="./docs/aws-ecr-images.png" width="700">

### ✔ CI/CD Responsibility Separation

| Category | Responsibility |
|---------|---------------|
| Infrastructure | Terraform |
| Application Build | GitHub Actions |
| Image Registry | Amazon ECR |
| Runtime Deployment | ECS Fargate |

### ✔ CI/CD Overall Flow

1. Source Push  
2. GitHub Actions Workflow  
3. Docker Build & Push  
4. ECS Service updates Task Definition to reference new image

```yaml
name: Backend CI/CD to AWS ECS

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:
```

## 6. ECS Fargate–Based Application Architecture

In this project, EC2 instances are not managed directly.  
Instead, the backend application is operated using  
**Amazon ECS Fargate (Serverless Containers)**.

By choosing Fargate:

- Server provisioning and OS management overhead is eliminated  
- Clear separation between application and infrastructure responsibility  
- Greater focus on deployment automation  

### ✔ 6-1) Resource Summary

| Component | Configuration | Public Access | Description |
|----------|---------------|---------------|-------------|
| Backend | ECS Fargate Service | ✅ (via ALB) | Spring Boot API |
| Database | RDS MariaDB | ❌ | Private subnet communication only |

- Backend is accessible **only through a single ALB entry point**
- Database is placed in **Private Subnets**, blocking external access

### ✔ 6-2) ECS Cluster / Service Configuration

- **ECS Cluster**: `matcha-cluster`
- **ECS Service**: `matcha-backend-service`
- Launch Type: **Fargate**
- Network Mode: `awsvpc`

<img src="./docs/aws-ecs-cluster.png" width="700">
<img src="./docs/aws-ecs-service-running.png" width="700">

The ECS Service handles:

- Task count management (`desiredCount`)
- Automatic ALB Target Group registration
- Traffic control based on health checks
- Rolling update strategy

The ECS Cluster is a logical grouping,  
while the actual deployed unit is the **ECS Task**.

### ✔ 6-3) ECS Task Definition Configuration

The ECS Task Definition represents the execution unit for the backend application.

- Container Image: Amazon ECR  
- Container Port: `8080`  
- Health Check Path: `/actuator/health`  
- Logging: Integrated with CloudWatch Logs  

<img src="./docs/aws-task-definition.png" width="700">

Task Definitions are managed by **revision**,  
allowing immediate rollback to a previous revision in case of deployment failure.

### ✔ 6-4) Zero-Downtime Deployment (Rolling Update) Strategy

The ECS Service uses a **rolling update strategy**:

1. GitHub Actions pushes a new Docker image to ECR  
2. ECS Service update is triggered  
3. New tasks are created  
4. Traffic is routed after ALB health checks pass  
5. Existing tasks are drained and terminated  

<img src="./docs/aws-alb-target-group.png" width="700">
<img src="./docs/aws-alb-healthcheck.png" width="700">

If a health check fails,  
traffic remains on the existing tasks, ensuring **no service interruption**.

### ✔ 6-5) Secret Management & Security Roadmap
To protect sensitive information, a tiered security approach for credentials was implemented.

Current Implementation: Database credentials and AWS API keys are managed using GitHub Actions Secrets. These are injected into the ECS Task environment variables during the CI/CD pipeline.

Security Roadmap: To achieve enterprise-grade security, there is a plan to migrate to AWS Secrets Manager. This will allow for automatic secret rotation and centralized auditing, further reducing the risk of credential exposure.

<img src="./docs/aws-iam.png" width="700">

## 7. Service Results and Validation

Using the GitHub Actions–based CI/CD pipeline,  
the service was verified to be operating correctly in production.

### Frontend (CloudFront)

https://matchaworld.shop

<img src="./docs/aws-result-frontend.png" width="700">

### Backend API (CloudWatch)

<img src="./docs/aws-result-backend-api.png" width="700">

### Request Flow

Frontend: Client → Route53 → CloudFront → S3
Backend:  Client → Route53 → ALB → ECS Task → RDS

<img src="./docs/aws-result-login.png" width="700">
<img src="./docs/aws-result-admin.png" width="700">

DNS records are intentionally excluded from CI/CD automation,
as Route 53 serves as a stable traffic entry point rather than a deployment target.

External traffic enters the AWS environment through Route 53,
which serves as the first managed entry point.

## 8. Overall Project Structure

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

## Key Summary

✔ Experience operating serverless containers with ECS Fargate
✔ CI/CD automation using GitHub Actions
✔ Recognized the absence of IaC as a problem and redesigned infrastructure using Terraform
✔ Understood the limitations of console-driven AWS environments and improved them structurally
✔ Designed a DevOps-oriented architecture with operations, security, and reproducibility in mind