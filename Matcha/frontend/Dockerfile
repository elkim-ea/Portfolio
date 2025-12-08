### --- 1) 최상단 ARG (필수) ---
ARG VITE_API_BASE_URL
ARG VITE_IMG_BASE_URL

### --- 2) builder 단계 시작 ---
FROM node:20-alpine AS builder
WORKDIR /app

# 여기서 다시 ARG 선언!
ARG VITE_API_BASE_URL
ARG VITE_IMG_BASE_URL

# 이제 ENV로 변환
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_IMG_BASE_URL=${VITE_IMG_BASE_URL}

COPY package.json .
RUN npm install
COPY . .
RUN npm rebuild esbuild


# Vite는 빌드 시점에 ENV 값을 읽음!
RUN npm run build

### --- 3) nginx ---
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
