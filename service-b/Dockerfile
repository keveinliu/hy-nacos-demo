FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY common/pom.xml common/
COPY service-a/pom.xml service-a/
COPY service-b/pom.xml service-b/
COPY service-c/pom.xml service-c/

RUN mvn dependency:go-offline -pl common,service-b -am -q 2>/dev/null || true

COPY common/src common/src
COPY service-b/src service-b/src

RUN mvn clean package -pl common,service-b -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /workspace/service-b/target/service-b-1.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8082 9092

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
