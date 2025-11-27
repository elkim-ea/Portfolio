#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/matcha"
COMPOSE_FILE="$APP_DIR/docker-compose.yml"

sudo mkdir -p "$APP_DIR"
cd "$APP_DIR"

# compose 최신 파일이 액션에서 올라온 상태라고 가정
docker compose -f "$COMPOSE_FILE" pull
docker compose -f "$COMPOSE_FILE" up -d --remove-orphans
docker ps
