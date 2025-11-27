# React 앱 빌드
# Node.js 20 버전을 사용하여 빌드 환경을 구성
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json .
RUN npm install
RUN npm rebuild esbuild
COPY . .
RUN npm run build

# 빌드된 결과물을 웹서버로 서비스
# 가벼운 Nginx 웹서버를 사용
FROM nginx:stable-alpine

# 빌드 단계에서 생성된 결과물을 Nginx의 기본 폴더로 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 위 builder 단계에서 생성된 빌드 결과물(dist 폴더)을 Nginx의 기본 웹 폴더로 복사합니다.
COPY --from=builder /app/dist /usr/share/nginx/html

# 80번 포트를 외부에 노출
EXPOSE 80

# Nginx 서버를 실행
CMD ["nginx", "-g", "daemon off;"]
