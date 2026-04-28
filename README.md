# hy-nacos-demo — 微服务 Demo（Spring Boot + Dubbo Triple + Nacos）

## 技术栈

- **JDK**: 17
- **Spring Boot**: 2.7.18
- **Dubbo**: 3.0.15（Triple 协议，应用级注册）
- **Nacos**: 2.2.3
- **Spring Cloud Alibaba**: 2021.0.6.2

## 项目结构

```
hy-nacos-demo/
├── common/                   # 共享 Protobuf、Dubbo Filter、自定义 SPI 扩展
│   └── metadata/EnvMetadataCustomizer.java   # 应用级注册 Nacos metadata 注入
├── service-a/                # 服务A (HTTP:8081, Dubbo:20881)
│   └── Dockerfile
├── service-b/                # 服务B (HTTP:8082, Dubbo:20882)
│   └── Dockerfile
├── service-c/                # 服务C (HTTP:8083, Dubbo:20883)
│   └── Dockerfile
├── k8s/                      # Kubernetes 部署 manifests
├── docker-compose.yml        # 完整服务栈（Nacos + 三个服务）
└── pom.xml
```

## 前置依赖

- JDK 17
- Maven 3.8+
- Docker 20.10+

---

## 方式一：本地直接运行

### 1. 启动 Nacos

```bash
docker-compose up -d nacos
```

### 2. 编译

```bash
mvn clean package -DskipTests
```

### 3. 启动服务（分三个终端，按 C → B → A 顺序）

```bash
# 终端1 - Service C（叶子服务，先启动）
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 \
  java -jar service-c/target/service-c-1.0-SNAPSHOT.jar

# 终端2 - Service B
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 \
  java -jar service-b/target/service-b-1.0-SNAPSHOT.jar

# 终端3 - Service A（入口服务，最后启动）
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 \
  java -jar service-a/target/service-a-1.0-SNAPSHOT.jar
```

---

## 方式二：Docker 镜像构建

三个服务打包到**同一镜像** `nacos-service`，通过容器启动时传入的 args 决定运行哪个 jar。

### 一键构建脚本（推荐）

```bash
bash build.sh
```

脚本会自动完成：用 Docker 容器编译 jar → 构建统一镜像 `nacos-service:0.1`。

---

### 手动分步执行

#### 步骤1：用 Docker 容器编译 jar（不依赖宿主机 JDK 版本）

```bash
docker run --rm \
  --security-opt seccomp=unconfined \
  -v "$(pwd)":/workspace \
  -v "$HOME/.m2":/root/.m2 \
  -w /workspace \
  -e MAVEN_OPTS="-Xmx512m -Xms256m -XX:+UseSerialGC" \
  maven:3.9-eclipse-temurin-17 \
  mvn clean package -DskipTests -q
```

> `-v "$HOME/.m2":/root/.m2` 挂载本地 Maven 仓库缓存，避免每次重复下载依赖。

编译完成后各服务的 jar 位于：

```
service-a/target/service-a-1.0-SNAPSHOT.jar
service-b/target/service-b-1.0-SNAPSHOT.jar
service-c/target/service-c-1.0-SNAPSHOT.jar
```

#### 步骤2：构建统一镜像

```bash
docker build -t nacos-service:0.1 .
```

#### 步骤3：启动容器（通过 args 选择服务）

```bash
# 启动 Service C
docker run -e ROUTING_UNIT=unit-1 -e NACOS_ADDR=<nacos-ip>:8848 \
  -p 8083:8083 -p 20883:20883 nacos-service:0.1 service-c.jar

# 启动 Service B
docker run -e ROUTING_UNIT=unit-1 -e NACOS_ADDR=<nacos-ip>:8848 \
  -p 8082:8082 -p 20882:20882 nacos-service:0.1 service-b.jar

# 启动 Service A
docker run -e ROUTING_UNIT=unit-1 -e NACOS_ADDR=<nacos-ip>:8848 \
  -p 8081:8081 -p 20881:20881 nacos-service:0.1 service-a.jar
```

---

### 一键启动完整服务栈（docker-compose）

```bash
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 \
  docker-compose up
```

或先 build 再 up：

```bash
docker-compose build
docker-compose up
```

---

## 测试验证

### 正常调用（完整调用链 A → B → C）

```bash
curl -H "x-routing-unit: unit-1" \
     -H "x-routing-idc: idc-1" \
     "http://localhost:8081/api/greeting?name=test"
```

### 单元拒绝（unit-2 请求被 unit-1 服务拒绝）

```bash
curl -H "x-routing-unit: unit-2" \
     "http://localhost:8081/api/greeting?name=test"
# 预期返回 403，包含 "Unit mismatch" 错误信息
```

### 无 header（允许通过）

```bash
curl "http://localhost:8081/api/greeting?name=test"
# 预期返回 200，无 unit 校验
```

### gRPC 直接调用（Triple 协议）

由于本项目使用 Dubbo 3 Triple 的 **Java Interface Mode**，gRPC 服务名和方法名与 Java 接口保持一致，调用时需要提供 proto 文件以便 grpcurl 解析消息格式。

```bash
# 调用 Service A（完整链路 A → B → C）
grpcurl -plaintext \
  -proto ./common/src/main/proto/service.proto \
  -d '{"name": "test"}' \
  -H 'x-routing-unit: unit1' \
  -H 'x-routing-idc: idc1' \
  <service-a-pod-ip>:9091 \
  com.example.demo.common.api.ServiceAApi/greeting

# 调用 Service B
grpcurl -plaintext \
  -proto ./common/src/main/proto/service.proto \
  -d '{"name": "test"}' \
  -H 'x-routing-unit: unit1' \
  -H 'x-routing-idc: idc1' \
  <service-b-pod-ip>:9092 \
  com.example.demo.common.api.ServiceBApi/process

# 调用 Service C
grpcurl -plaintext \
  -proto ./common/src/main/proto/service.proto \
  -d '{"name": "test"}' \
  -H 'x-routing-unit: unit1' \
  -H 'x-routing-idc: idc1' \
  <service-c-pod-ip>:9093 \
  com.example.demo.common.api.ServiceCApi/process
```

> **注意**：Dubbo Triple 在此模式下不将业务服务注册到标准 gRPC Server Reflection，因此 `grpcurl list` 只能看到 `grpc.health.v1.Health` 和 `grpc.reflection.v1alpha.ServerReflection`，看不到具体业务服务。必须通过 `-proto` 参数提供 proto 文件。

### 查看 Nacos 注册信息（验证 unit/idc metadata）

```bash
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=service-a"
```

在返回的 JSON 中，`hosts[0].metadata` 应包含：

```json
{
  "unit": "unit-1",
  "idc": "idc-1",
  "protocol": "grpc",
  "dubbo.endpoints": "[{\"port\":20881,\"protocol\":\"tri\"}]"
}
```

---

## 应用级注册与 Nacos Metadata

本 Demo 采用 **Dubbo 3 应用级注册（Service Discovery）**。在此模式下，`dubbo.provider.parameters` 中的参数仅作用于接口级 URL，**不会自动写入 Nacos 实例的 metadata**。

为此，项目通过 **Dubbo SPI 扩展 `ServiceInstanceCustomizer`** 实现了 `EnvMetadataCustomizer`（位于 `common` 模块），在服务实例注册到 Nacos 之前，将环境变量 `ROUTING_UNIT`、`ROUTING_IDC` 以及协议信息注入到实例 metadata 中。

如需查看实现细节，参见：

```
common/src/main/java/com/example/demo/common/metadata/EnvMetadataCustomizer.java
```

---

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `ROUTING_UNIT` | 单元标签，用于流量拒绝匹配 | 空（不校验） |
| `ROUTING_IDC` | 机房标签，仅透传不校验 | 空 |
| `NACOS_ADDR` | Nacos 服务地址 | `127.0.0.1:8848` |
| `HTTP_PORT` | HTTP 监听端口 | `8081` / `8082` / `8083` |
| `DUBBO_PORT` | Dubbo Triple 监听端口 | `20881` / `20882` / `20883` |

## 端口说明

| 服务 | HTTP 端口 | Dubbo Triple 端口 |
|------|-----------|-------------------|
| service-a | 8081 | 20881 |
| service-b | 8082 | 20882 |
| service-c | 8083 | 20883 |
| Nacos | 8848 | 9848 |

---

## Kubernetes 部署

```bash
# 部署
kubectl apply -f k8s/

# 查看状态
kubectl get pods,svc -n demo

# 清理
kubectl delete -f k8s/
```
