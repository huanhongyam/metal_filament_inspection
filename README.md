# 金属丝材检测后端系统

> Spring Boot 3.4.5 · Java 17 · 金属丝材质量检测与智能评估平台

## 项目概述

金属丝材检测后端系统是一个面向金属丝材生产线的质量检测与智能评估平台。系统集成华为云 IoT 设备通信、YOLO 表面缺陷检测、AI 质量评估与问答，为金属丝材生产提供从数据采集、缺陷识别到智能分析的全链路数字化解决方案。

### 核心功能

- **IoT 设备接入** — 通过华为云 IoTDA AMQP 协议实时接收产线设备上报的丝材属性数据
- **YOLO 缺陷检测** — 基于 ONNX Runtime + OpenCV 的表面缺陷检测，支持划痕、块状缺陷、簇状缺陷、金属毛刺、擦伤五类缺陷识别
- **AI 质量评估** — 调用 Agent4j Python 服务对丝材进行质量评估与风险预警
- **智能问答** — 支持同步与 SSE 流式响应的 AI 问答，面向产线人员
- **用户体系** — 邮箱验证码注册/登录 + JWT 鉴权，区分普通用户与管理员角色
- **数据管理** — 丝材批次管理、检测记录、设备管理、应用场景配置
- **云端存储** — 检测标注图片自动上传七牛云

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.4.5 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis (Lettuce) | — |
| 消息队列 | RabbitMQ | — |
| 认证 | JJWT | 0.12.7 |
| IoT | Huawei Cloud IoTDA SDK + Qpid JMS | — |
| AI 推理 | ONNX Runtime + OpenCV | 1.18.0 / 4.5.5 |
| 对象存储 | 七牛云 SDK | 7.13.0 |
| API 文档 | SpringDoc + Knife4j | 2.8.4 / 4.3.0 |
| 工具 | Lombok · Hutool · Commons Lang3 | — |

## 项目结构

```
src/main/java/com/kunpeng/metal_filament_inspection/
├── MetalFilamentInspectionApplication.java   # 启动类
├── amqp/listener/                            # 华为云 IoT AMQP 消息监听器
│   ├── IoTAmqpAddListener.java               #   丝材属性上报
│   ├── IoTAmqpQuestionListener.java          #   硬件 AI 提问
│   └── IoTAmqpSurfaceListener.java           #   表面数据请求
├── annotation/                               # 自定义注解
│   └── RequireAdmin.java                     #   管理员鉴权注解
├── aop/                                      # AOP 切面
│   ├── AdminAuthAspect.java                  #   管理员权限校验
│   └── WMMetaObjectHandler.java              #   MyBatis-Plus 字段自动填充
├── config/                                   # 配置类
│   ├── AmqpClientConfig.java                 #   华为 AMQP 客户端配置
│   ├── AsyncConfig.java                      #   异步线程池（SSE 流式）
│   ├── MqConfig.java                         #   RabbitMQ 交换机/队列/绑定
│   ├── WebConfig.java                        #   JWT 拦截器注册
│   └── YoloConfig.java                       #   YOLO 模型参数配置
├── controller/                               # REST 控制器
│   ├── UserController.java                   #   /api/user/**
│   ├── WireMaterialController.java           #   /api/wire-material/**
│   ├── DeviceController.java                 #   /api/device/**
│   ├── DetectionBatchController.java         #   /api/detection-batch/**
│   ├── ApplicationScenarioController.java    #   /api/scenario/**
│   └── QuestionController.java               #   /api/question/**
├── domain/                                   # 数据对象
│   ├── dto/                                  #   数据传输对象
│   ├── entity/                               #   数据库实体
│   └── vo/                                   #   视图对象
├── interceptor/
│   └── JwtTokenInterceptor.java              # JWT 鉴权拦截器
├── mapper/                                   # MyBatis-Plus Mapper
├── mq/consumer/                              # RabbitMQ 消费者
│   ├── IoTYOLOConsumer.java                  #   YOLO 检测任务
│   ├── AgentTriggerConsumer.java             #   AI 评估触发
│   └── IoTSendDownConsumer.java              #   IoT 下行消息
├── service/                                  # 服务接口与实现
└── utils/                                    # 工具类
    ├── JwtUtil.java                          #   JWT 生成/校验
    ├── YoloUtil.java                         #   ONNX YOLO 推理
    ├── QiniuUploadUtil.java                  #   七牛云上传
    ├── HuaWeiIoTSentDownUtil.java            #   华为 IoT 下行
    ├── EmailUtil.java                        #   邮件发送
    ├── VerificationCodeUtil.java             #   邮箱验证码（Redis）
    ├── IdWorker.java                         #   Redis 雪花 ID 生成
    └── GlobalExceptionHandler.java           #   全局异常处理
```

## 系统架构

```
┌──────────────┐     AMQP      ┌──────────────────┐
│  华为云 IoTDA │ ────────────→ │ IoT Listener     │
│  (设备数据)    │               │ (属性/问题/表面)  │
└──────────────┘               └────────┬─────────┘
                                        │
                              ┌─────────▼─────────┐
                              │    RabbitMQ        │
                              │  ┌───────────────┐ │
                              │  │ detect.queue   │ │──→ IoTYOLOConsumer
                              │  │ senddown.queue │ │──→ IoTSendDownConsumer
                              │  │ triggerEval…   │ │──→ AgentTriggerConsumer
                              │  │ delay.queue    │ │──→ (10s 延迟 → 触发评估)
                              │  └───────────────┘ │
                              └────────────────────┘
                                        │
              ┌─────────────────────────┼─────────────────────────┐
              │                         │                         │
    ┌─────────▼─────────┐   ┌──────────▼──────────┐   ┌──────────▼──────────┐
    │ YOLO (ONNX)       │   │ Agent4j (Python)    │   │ 七牛云              │
    │ 缺陷检测 + 图片标注 │   │ 质量评估 / AI 问答   │   │ 检测图片存储         │
    └───────────────────┘   └─────────────────────┘   └─────────────────────┘
```

### 消息流程

1. 设备数据通过华为云 IoTDA AMQP 进入系统
2. `IoTAmqpAddListener` 解析丝材属性，写入 MySQL，然后发布两条 RabbitMQ 消息：
   - `detect.task` → `IoTYOLOConsumer` 执行 YOLO 缺陷检测，结果存入数据库，图片上传七牛云
   - 经 `delay.queue`（10s TTL 死信）延迟后 → `AgentTriggerConsumer` 调用 Agent4j 进行 AI 质量评估
3. `IoTAmqpQuestionListener` 接收硬件提问，同步调用 Agent4j 并下行回复
4. `IoTAmqpSurfaceListener` 触发检测数据聚合下行至串口屏

## 数据库表

| 表名 | 说明 |
|------|------|
| `user` | 用户（邮箱 + BCrypt 密码，角色区分） |
| `wire_material` | 丝材批次（属性、AI 评估结果） |
| `device` | 产线设备 |
| `detection_batch` | 检测记录（五类缺陷计数、标注图片 URL） |
| `application_scenario` | 应用场景（丝材类型 + 参数阈值） |
| `question` | AI 问答记录 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0
- Redis
- RabbitMQ（需创建 vhost `/wirematerial`）
- OpenCV 4.x native library
- [Agent4j](https://github.com/your-org/agent4j) Python 服务（AI 评估与问答）

### 本地运行

```bash
# 克隆项目
git clone <repo-url>
cd metal_filament_inspection_backend

# 修改 application.yaml 中的数据库、Redis、RabbitMQ 连接信息

# 编译运行
mvn clean package -DskipTests
java -jar target/metal_filament_inspection-0.0.1-SNAPSHOT.jar
```

服务启动后访问：
- API 接口：`http://localhost:8080`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`
- API Docs (JSON)：`http://localhost:8080/v3/api-docs`

### 外部依赖配置

编辑 `src/main/resources/application.yaml`：

```yaml
# 数据库
spring.datasource.url: jdbc:mysql://<your-host>:3306/metal_filament_inspection
spring.datasource.username: <your-username>
spring.datasource.password: <your-password>

# Redis
spring.data.redis.host: <your-redis-host>

# RabbitMQ
spring.rabbitmq.host: <your-rabbitmq-host>
spring.rabbitmq.virtual-host: /wirematerial

# 七牛云（图片存储）
qiniu:
  access-key: <your-access-key>
  secret-key: <your-secret-key>
  bucket: <your-bucket>

# 华为云 IoT（设备通信）
huawei:
  iot:
    amqp:
      host: amqps://<iotda-endpoint>:5671
    app:
      ak: <your-ak>
      sk: <your-sk>
      project-id: <your-project-id>

# YOLO 模型
yolo:
  model-path: src/main/resources/models/best.onnx
  confidence-threshold: 0.5

# Agent4j AI 服务
agent4j:
  base-url: http://localhost:8081
```

## API 认证

所有 `/api/**` 接口（除登录、注册外）需在请求头中携带 JWT：

```
Authorization: Bearer <token>
```

管理员接口额外需要 `@RequireAdmin` 注解校验。

Agent 服务调用使用专用 Token：

```
pass4agent: <agent-token>
```

### 主要 API 概览

| 模块 | 路径 | 说明 |
|------|------|------|
| 用户 | `POST /api/user/login` | 用户名密码登录 |
| 用户 | `POST /api/user/login-email` | 邮箱验证码登录 |
| 用户 | `POST /api/user/register-user` | 邮箱验证码注册 |
| 丝材 | `GET /api/wire-material/page` | 分页查询丝材 |
| 丝材 | `POST /api/wire-material/{batchNumber}/trigger-evaluation` | 触发 AI 评估 |
| 丝材 | `GET /api/wire-material/early-warning-stats` | 预警统计 |
| 检测 | `GET /api/detection-batch/list` | 缺陷记录列表 |
| 设备 | `GET /api/device/list` | 设备列表 |
| 问答 | `POST /api/question/ask/stream` | SSE 流式 AI 问答 |
| 场景 | `GET /api/scenario/list` | 应用场景列表 |

## 许可证

内部项目，保留所有权利。
