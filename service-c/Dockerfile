FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && rm -rf /var/lib/apt/lists/* || true

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY service-c/target/service-c-1.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8083 9093

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
