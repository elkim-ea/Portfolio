# 1. AWS를 사용하겠다고 선언 (Provider)
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

# 3. Public Subnet 추가 (VPC 안에 속함)
resource "aws_subnet" "public_subnet" {
  vpc_id     = aws_vpc.main.id # 위에서 만든 VPC의 ID를 자동으로 참조합니다.
  cidr_block = "10.0.1.0/24"
  availability_zone = "ap-northeast-2a"

  tags = {
    Name = "matcha-public-subnet"
  }
}

# 4. Security Group (보안 설정: 443, 8080 포트 개방)
resource "aws_security_group" "alb_sg" {
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ECS Backend Security Group
resource "aws_security_group" "ecs_backend_sg" {
  name   = "ecs-backend-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
