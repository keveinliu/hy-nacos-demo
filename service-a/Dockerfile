# ======================================================
# Stage 1: Build
#   使用 eclipse-temurin:17-jdk-alpine 编译打包
#   包含 Maven Wrapper 或直接内置 mvn
# ======================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# 安装 Maven（alpine 包体积最小）
RUN apk add --no-cache maven

# 先复制 pom 文件，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
COPY common/pom.xml common/
COPY service-a/pom.xml service-a/
COPY service-b/pom.xml service-b/
COPY service-c/pom.xml service-c/

# 预下载所有依赖（仅 pom，不含源码）
RUN mvn dependency:go-offline -pl common,service-a -am -q 2>/dev/null || true

# 复制源码
COPY common/src common/src
COPY service-a/src service-a/src

# 编译 & 打包（跳过测试，仅构建 service-a 及其依赖 common）
RUN mvn clean package -pl common,service-a -am -DskipTests -q

# ======================================================
# Stage 2: Runtime
#   使用最小 JRE Alpine 镜像
# ======================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# 安装 curl（健康检查用）
RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

# 创建非 root 用户，遵循最小权限原则
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 从 builder 阶段复制 fat jar
COPY --from=builder /workspace/service-a/target/service-a-1.0-SNAPSHOT.jar app.jar

# 切换到非 root 用户
USER appuser

# 暴露 HTTP 和 gRPC 端口
EXPOSE 8081 9091

# JVM 调优：适配容器内存限制
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
