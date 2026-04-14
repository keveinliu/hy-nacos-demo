#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "==> Step 1: Compile with Maven in Docker (requires Docker)"
docker run --rm \
  --security-opt seccomp=unconfined \
  -v "$SCRIPT_DIR":/workspace \
  -v "$HOME/.m2":/root/.m2 \
  -w /workspace \
  -e MAVEN_OPTS="-Xmx512m -Xms256m -XX:+UseSerialGC" \
  maven:3.9-eclipse-temurin-17 \
  mvn clean package -DskipTests -q

echo "==> Step 2: Build unified Docker image"
docker build -t nacos-service:0.1 .

echo "==> Done."
docker images nacos-service
