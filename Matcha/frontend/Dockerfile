# --- 1) 최상단 ARG 선언
ARG VITE_API_BASE_URL
ARG VITE_IMG_BASE_URL

# --- 2) builder 단계 ---
FROM node:20-alpine AS builder
WORKDIR /app

# ARG 재선언 (필수)
ARG VITE_API_BASE_URL
ARG VITE_IMG_BASE_URL

# ENV로 변환
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_IMG_BASE_URL=${VITE_IMG_BASE_URL}

# 먼저 package.json 복사 후 install
COPY package.json package-lock.json* ./
RUN npm install

# 전체 소스 복사
COPY . .

# esbuild 재컴파일 → 빌드
RUN npm rebuild esbuild
RUN npm run build

# --- 3) nginx ---
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
