FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

ENV MAVEN_OPTS="-Xmx512m -Xms256m -XX:+UseSerialGC"

COPY pom.xml .
COPY common/pom.xml common/
COPY service-a/pom.xml service-a/
COPY service-b/pom.xml service-b/
COPY service-c/pom.xml service-c/

RUN mvn dependency:go-offline -pl common,service-c -am -q 2>/dev/null || true

COPY common/src common/src
COPY service-c/src service-c/src

RUN mvn clean package -pl common,service-c -am -DskipTests -q

FROM eclipse-temurin:17-jre AS runtime

RUN apt-get update && apt-get install -y --no-install-recommends curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY --from=builder /workspace/service-c/target/service-c-1.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8083 9093

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
