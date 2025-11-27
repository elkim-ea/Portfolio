# Matcha World – IT 기획서

---

## 프로젝트 개요
- **프로젝트 명:** ESG 개인 실천 플랫폼  
- **개발 목적:** 개인이 생활 속에서 ESG 활동을 기록하고, 데이터 기반으로 시각화·공유할 수 있는 서비스 구축  
- **주요 기능 요약:**  
  - 소셜 로그인 기능 및 메일 인증 기능  
  - ESG 퀘스트 인증(AI 분석 기반)  
  - 활동 기록·랭킹 시스템  
  - 기업 ESG 활동 사례 열람  
  - 관리자 계정의 퀘스트/사용자 관리  

---

## 기술 스택
- **프론트엔드:** React (Vite), TypeScript, CSS3, Chart.js  
- **백엔드:** Spring Boot (JWT 인증, REST API, DB 연동)  
- **AI:** FastAPI, Hugging Face, Spring AI Open API (이미지 인증 분석)  
- **데이터베이스:** MariaDB  
- **인프라:** Docker, Naver Cloud, Kubernetes  
- **협업 도구:** Discord, GitHub, Swagger (OpenAPI)  

---

## 시스템 아키텍처
- **구성:**  
  - frontend: React 기반 UI  
  - backend: Spring Boot API 서버  
  - ml: FastAPI 기반 인증 이미지 분석 서비스, Hugging Face 파인 튜닝, Spring AI 오픈 API  
  - db: MariaDB  

- **인프라:**  
  Docker 컨테이너 → NaverCloud 배포 → Kubernetes 확장 고려  

---

## API 설계 개요
- `POST /auth/signup` → 회원가입  
- `POST /auth/login` → 로그인 (JWT)  
- `POST /quest/upload` → 퀘스트 인증 이미지 업로드  
- `GET /records/me` → 내 ESG 활동 기록 조회  
- `GET /ranking/global` → 글로벌 랭킹 조회  
- `GET /ranking/me` → 내 순위 조회  
- `GET /company/list` → 기업별 ESG 활동 사례 조회  
- `POST /quest/manage` → 관리자 퀘스트 등록·수정·삭제  

---

## 개발 일정
1. **설계:** 아키텍처, ERD, 화면 정의  
2. **개발:** 프론트엔드, 백엔드 API, AI 이미지 분석 모델 연결  
3. **테스트:** 단위/통합 테스트, 사용자 피드백 반영  
4. **배포:** Docker → Naver Cloud → Kubernetes  

---

## 역할 분담
- **김태영:** 기록 텍스트 AI 분석(E/S 분류), 퀘스트 이미지 분석 모델 구축 및 연동, 요구사항 명세서 제작  
- **전유리:** 회원가입·로그인·소셜 로그인, 마이페이지, 관리자 페이지 구현, DB 및 ERD 제작  
- **조이슬:** 퀘스트 페이지 및 기업 보기 기능 개발, 날씨 API 연동, 피그마 제작, API 명세서 초안 제작  
- **김은애:** 기록 CRUD 및 활동·랭킹 기능 개발, 그래프/점수 UI 구현, 피그마 제작, Use case, Activity, Architecture 다이어그램 제작  
- **김한재:** 기획서 제작, 프로젝트 산출물 및 발표자료 정리, QA 및 테스트 관리  

---

## 향후 확장
- 배지·레벨 시스템 추가  
- 기업-개인 간 ESG 캠페인 협업 기능  
- 앱/웹 크로스 플랫폼 지원  
