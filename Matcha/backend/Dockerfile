# ✅ Java 21 버전 (bookworm은 slim 후속)
FROM openjdk:21-ea-21-jdk-slim

RUN apt-get update && apt-get install -y curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
    
# ✅ tzdata 비대화형 설치 및 한국 시간대 설정
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Seoul
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    dpkg-reconfigure -f noninteractive tzdata && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


# 작업 디렉토리 설정
WORKDIR /app

# ✅ 정확한 jar 파일명 지정
COPY ./build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# uploads 폴더 복사 (없으면 자동 생성)
COPY ./uploads ./uploads

# ✅ 포트 개방
EXPOSE 8080

# ✅ JVM 타임존 KST로 강제
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
