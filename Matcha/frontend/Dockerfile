# --- 1) builder 단계 ---
FROM node:20-alpine AS builder
WORKDIR /app

# 패키지 설치
COPY package.json package-lock.json* ./ 

RUN npm install

# 전체 소스 복사 후 빌드
COPY . .
RUN npm rebuild esbuild
RUN npm run build

# --- 2) nginx ---
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
