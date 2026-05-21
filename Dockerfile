FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends curl tzdata net-tools \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && rm -rf /var/lib/apt/lists/* || true

# Install grpcurl for gRPC debugging
ARG GRPCURL_VERSION=1.9.1
RUN curl -sSL "https://github.com/fullstorydev/grpcurl/releases/download/v${GRPCURL_VERSION}/grpcurl_${GRPCURL_VERSION}_linux_x86_64.tar.gz" \
    -o /tmp/grpcurl.tar.gz \
    && tar -xzf /tmp/grpcurl.tar.gz -C /usr/local/bin grpcurl \
    && rm /tmp/grpcurl.tar.gz \
    && chmod +x /usr/local/bin/grpcurl

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

RUN mkdir -p /home/appuser/.dubbo /home/appuser/nacos/naming && \
    chown -R appuser:appgroup /home/appuser/.dubbo /home/appuser/nacos

WORKDIR /app

COPY service-a/target/service-a-1.0-SNAPSHOT.jar service-a.jar
COPY service-b/target/service-b-1.0-SNAPSHOT.jar service-b.jar
COPY service-c/target/service-c-1.0-SNAPSHOT.jar service-c.jar

USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom \
               --add-opens java.base/java.lang=ALL-UNNAMED \
               --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
               --add-opens java.base/java.util=ALL-UNNAMED \
               --add-opens java.base/java.math=ALL-UNNAMED \
               --add-opens java.base/java.net=ALL-UNNAMED \
               --add-opens java.base/java.io=ALL-UNNAMED \
               --add-opens java.base/java.nio=ALL-UNNAMED \
               --add-opens java.base/java.time=ALL-UNNAMED \
               --add-opens java.base/java.text=ALL-UNNAMED \
               --add-opens java.base/java.util.concurrent=ALL-UNNAMED"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar $0 \"$@\""]
