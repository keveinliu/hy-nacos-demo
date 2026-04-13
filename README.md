# hy-nacos-demo — 微服务Demo（SpringBoot + gRPC + Nacos）

## 技术栈

- **JDK**: 17
- **SpringBoot**: 2.7.18
- **gRPC**: Latest
- **Nacos**: 2.2.3
- **Spring Cloud Alibaba**: 2021.0.6.2

## 项目结构

```
hy-nacos-demo/
├── common/                   # 共享protobuf定义和工具类
├── service-a/               # 服务A (HTTP:8081, gRPC:9091)
├── service-b/               # 服务B (HTTP:8082, gRPC:9092)
├── service-c/               # 服务C (HTTP:8083, gRPC:9093)
├── k8s/                      # Kubernetes部署manifests
│   ├── namespace.yaml
│   ├── nacos-deployment.yaml
│   ├── service-a-*.yaml
│   ├── service-b-*.yaml
│   └── service-c-*.yaml
├── docker-compose.yml        # Nacos本地启动配置
└── pom.xml                   # 主项目POM
```

## 快速启动

### 1. 启动 Nacos (Docker Compose)

```bash
docker-compose up -d
```

### 2. 编译项目

```bash
mvn clean package -DskipTests
```

### 3. 启动服务 (分三个终端)

**终端1 - 启动Service C:**
```bash
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 ROUTING_USER=user-1 java -jar service-c/target/service-c-1.0-SNAPSHOT.jar
```

**终端2 - 启动Service B:**
```bash
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 ROUTING_USER=user-1 java -jar service-b/target/service-b-1.0-SNAPSHOT.jar
```

**终端3 - 启动Service A:**
```bash
ROUTING_UNIT=unit-1 ROUTING_IDC=idc-1 ROUTING_USER=user-1 java -jar service-a/target/service-a-1.0-SNAPSHOT.jar
```

## 测试验证

### 正常调用 (带routing headers)

```bash
curl -H "x-routing-unit: unit-1" \
     -H "x-routing-idc: idc-1" \
     -H "x-routing-user: user-1" \
     "http://localhost:8081/api/greeting?name=test"
```

### 单元拒绝测试 (unit-2 vs service unit-1)

```bash
curl -H "x-routing-unit: unit-2" \
     "http://localhost:8081/api/greeting?name=test"
```

### 无header调用 (允许通过)

```bash
curl "http://localhost:8081/api/greeting?name=test"
```

### 查看Nacos注册信息

```bash
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=service-a"
```

## 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| ROUTING_UNIT | 单元标签 | 空 |
| ROUTING_IDC | 机房标签 | 空 |
| ROUTING_USER | 用户标签 | 空 |
| NACOS_ADDR | Nacos 地址 | 127.0.0.1:8848 |
| HTTP_PORT | HTTP 端口 | 8081/8082/8083 |
| GRPC_PORT | gRPC 端口 | 9091/9092/9093 |

## 端口说明

| 服务 | HTTP 端口 | gRPC 端口 |
|------|-----------|-----------|
| service-a | 8081 | 9091 |
| service-b | 8082 | 9092 |
| service-c | 8083 | 9093 |
| Nacos | 8848 | 9848 |

## Kubernetes 部署

### 部署所有资源

```bash
kubectl apply -f k8s/
```

### 验证部署

```bash
# 查看命名空间
kubectl get ns

# 查看pods
kubectl get pods -n demo

# 查看services
kubectl get svc -n demo

# 查看deployments
kubectl get deployments -n demo
```

### 清理

```bash
kubectl delete -f k8s/
```
