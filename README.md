# hy-nacos-demo — 微服务Demo（SpringBoot + gRPC + Nacos）

## 技术栈

- **JDK**: 17
- **SpringBoot**: 2.7.18
- **gRPC**: 1.58.0 (via grpc-spring-boot-starter 2.15.0)
- **Nacos**: 2.2.3
- **Spring Cloud Alibaba**: 2021.0.6.2

## 项目结构

```
hy-nacos-demo/
├── common/                   # 共享 protobuf 定义、gRPC 拦截器
├── service-a/                # 服务A (HTTP:8081, gRPC:9091)
│   └── Dockerfile
├── service-b/                # 服务B (HTTP:8082, gRPC:9092)
│   └── Dockerfile
├── service-c/                # 服务C (HTTP:8083, gRPC:9093)
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
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 ROUTING_USER=user-1 \
  java -jar service-c/target/service-c-1.0-SNAPSHOT.jar

# 终端2 - Service B
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 ROUTING_USER=user-1 \
  java -jar service-b/target/service-b-1.0-SNAPSHOT.jar

# 终端3 - Service A（入口服务，最后启动）
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 ROUTING_USER=user-1 \
  java -jar service-a/target/service-a-1.0-SNAPSHOT.jar
```

---

## 方式二：Docker 镜像构建

Dockerfile 采用**宿主机编译 + 容器打包**方式：编译在宿主机完成，Docker 只负责将 jar 打入镜像。这样可以避免部分 Linux 环境下 Docker builder 对 JVM 线程创建的 seccomp 限制。

### 步骤1：宿主机编译 jar

```bash
mvn clean package -DskipTests
```

编译完成后各服务的 jar 位于：

```
service-a/target/service-a-1.0-SNAPSHOT.jar
service-b/target/service-b-1.0-SNAPSHOT.jar
service-c/target/service-c-1.0-SNAPSHOT.jar
```

### 步骤2：构建 Docker 镜像

**注意**：`docker build` 的 context 必须是项目根目录（`.`），因为 Dockerfile 中的 COPY 路径以根目录为基准。

```bash
# 构建三个服务镜像
docker build -t service-a:0.1 -f service-a/Dockerfile .
docker build -t service-b:0.1 -f service-b/Dockerfile .
docker build -t service-c:0.1 -f service-c/Dockerfile .
```

### 步骤3：一键启动完整服务栈

```bash
# 使用 docker-compose 启动（会自动 build 镜像）
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 ROUTING_USER=user-1 \
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
     -H "x-routing-user: user-1" \
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

### 查看 Nacos 注册信息（验证 unit/idc/user metadata）

```bash
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=service-a"
```

---

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `ROUTING_UNIT` | 单元标签，用于流量拒绝匹配 | 空（不校验） |
| `ROUTING_IDC` | 机房标签，仅透传不校验 | 空 |
| `ROUTING_USER` | 用户标签，仅透传不校验 | 空 |
| `NACOS_ADDR` | Nacos 服务地址 | `127.0.0.1:8848` |
| `HTTP_PORT` | HTTP 监听端口 | `8081` / `8082` / `8083` |
| `GRPC_PORT` | gRPC 监听端口 | `9091` / `9092` / `9093` |

## 端口说明

| 服务 | HTTP 端口 | gRPC 端口 |
|------|-----------|-----------|
| service-a | 8081 | 9091 |
| service-b | 8082 | 9092 |
| service-c | 8083 | 9093 |
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
