#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/matcha"

cd "$APP_DIR"

# .env 파일은 서버에 미리 만들어두거나 GitHub Secrets로 넣어야 함
if [ ! -f ".env" ]; then
    echo "❌ ERROR: .env 파일이 없습니다. NCP 서버에 /opt/matcha/.env 생성 필요!"
    exit 1
fi

echo "🚀 Pulling latest images..."
docker compose pull || true

echo "🚀 Starting services..."
docker compose up -d --remove-orphans

docker ps
