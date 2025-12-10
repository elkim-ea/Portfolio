# --- 1) builder 단계 ---
FROM node:20-alpine AS builder
WORKDIR /app

# --- 환경변수 ARG 먼저 선언 ---
ARG VITE_API_BASE_URL
ARG VITE_IMG_BASE_URL

# --- Vite가 읽을 수 있도록 ENV로 변환 ---
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_IMG_BASE_URL=$VITE_IMG_BASE_URL

# 패키지 설치
COPY package.json package-lock.json* ./
RUN npm install

# 전체 소스 복사
COPY . .

# esbuild fix
RUN npm rebuild esbuild

# --- 🔥 여기서 드디어 환경변수 적용된 채로 빌드됨 ---
RUN npm run build

# --- 2) nginx ---
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
