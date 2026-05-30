# Adventure Picture Backend 🖼️

**智能云图库后端** — 基于 Spring Boot 3 的图片管理与协作平台后端系统。

支持图片上传（文件 / URL / 批量）、图片审核、**以图搜图**、**以颜色搜图**、**AI 智能扩图**、空间管理（公共 / 私有 / 团队）、**WebSocket 实时协同编辑**、多维度空间分析统计等丰富功能。

---

## 📋 目录

- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
  - [环境准备](#环境准备)
  - [本地运行](#本地运行)
  - [Docker 部署](#docker-部署)
- [配置说明](#-配置说明)
- [核心功能](#-核心功能)
- [API 概览](#-api-概览)
- [权限体系](#-权限体系)
- [架构设计](#-架构设计)
- [开发指南](#-开发指南)
- [常见问题](#-常见问题)

---

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **语言** | Java | 17 |
| **框架** | Spring Boot | 3.3.10 |
| **ORM** | MyBatis-Flex | 1.10.9 |
| **权限认证** | Sa-Token | 1.40.0 |
| **数据库** | MySQL | 8.0 |
| **缓存** | Redis + Caffeine | 7 / 3.1.8 |
| **对象存储** | 腾讯云 COS | 5.6.227 |
| **实时通信** | WebSocket + LMAX Disruptor | 3.4.2 |
| **AI 能力** | 阿里云通义万相（AI 扩图） | — |
| **文档** | Knife4j (Swagger) | 4.4.0 |
| **工具库** | Hutool、Lombok、Jsoup | — |
| **构建** | Maven | — |
| **部署** | Docker / docker-compose | — |

---

## 📁 项目结构

```
adventure-picture-backend/
├── mysql-init/                          # 数据库初始化脚本
│   └── adventure-pucture.sql
├── src/
│   ├── Dockerfile                       # Docker 构建文件
│   ├── docker-compose.yml               # Docker Compose 编排
│   ├── main/
│   │   ├── java/com/adventure/picturebackend/
│   │   │   ├── AdventurePictureApplication.java    # 启动入口
│   │   │   │
│   │   │   ├── aop/
│   │   │   │   ├── AuthInterceptor.java            # 权限认证 AOP 切面
│   │   │   │   └── annotation/AuthCheck.java       # 权限校验注解
│   │   │   │
│   │   │   ├── api/                                 # 外部 API 集成
│   │   │   │   ├── aliyun/                          # 阿里云 AI 扩图 API
│   │   │   │   │   ├── AliYunAiApi.java
│   │   │   │   │   ├── CreateOutPaintingTaskRequest.java
│   │   │   │   │   ├── CreateOutPaintingTaskResponse.java
│   │   │   │   │   └── GetOutPaintingTaskResponse.java
│   │   │   │   ├── imagesearch/                     # 以图搜图模型
│   │   │   │   └── sub/                             # 以图搜图门面 & 子 API
│   │   │   │       ├── ImageSearchApiFacade.java
│   │   │   │       ├── GetImagePageUrlApi.java
│   │   │   │       ├── GetImageFirstUrlApi.java
│   │   │   │       └── GetImageListApi.java
│   │   │   │
│   │   │   ├── common/                              # 公共模块
│   │   │   │   ├── constant/                        # 常量
│   │   │   │   ├── exception/                       # 异常体系 & 全局处理器
│   │   │   │   ├── req/                             # 公共请求参数
│   │   │   │   ├── resp/                            # 统一响应封装
│   │   │   │   └── utils/                           # 工具类
│   │   │   │       ├── ColorSimilarUtils.java       # 颜色相似度计算
│   │   │   │       ├── IpHelperUtils.java           # IP 地址工具
│   │   │   │       ├── PageParamUtils.java          # 分页参数校验
│   │   │   │       └── ResultUtils.java             # 响应构建工具
│   │   │   │
│   │   │   ├── config/                              # 配置类
│   │   │   │   ├── AsyncConfig.java                 # 异步线程池
│   │   │   │   ├── CorsConfig.java                  # 跨域配置
│   │   │   │   ├── CosClientConfig.java             # 腾讯云 COS 客户端
│   │   │   │   └── SpaceCapacityConfig.java         # 空间容量配置
│   │   │   │
│   │   │   ├── controller/                          # REST 控制器
│   │   │   │   ├── PictureController.java           # 图片管理
│   │   │   │   ├── UserController.java              # 用户管理
│   │   │   │   ├── SpaceController.java             # 空间管理
│   │   │   │   ├── SpaceAnalyzeController.java      # 空间分析
│   │   │   │   ├── SpaceUserController.java         # 空间成员管理
│   │   │   │   └── test/                            # 测试控制器
│   │   │   │
│   │   │   ├── manager/                             # 核心管理层
│   │   │   │   ├── CosManager.java                  # COS 对象存储操作
│   │   │   │   ├── FileManager.java                 # 文件管理（已废弃）
│   │   │   │   ├── upload/                          # 图片上传（模板方法模式）
│   │   │   │   │   ├── FileUploadTemplate.java      # 上传抽象模板
│   │   │   │   │   ├── FilePictureUpload.java       # 本地文件上传
│   │   │   │   │   └── UrlPictureUpload.java        # URL 图片上传
│   │   │   │   ├── auth/                            # 权限认证体系
│   │   │   │   │   ├── SpaceUserAuthManager.java    # 空间权限管理器
│   │   │   │   │   ├── SpaceUserAuthConfig.java     # 权限配置模型
│   │   │   │   │   ├── SpaceUserPermission.java     # 权限模型
│   │   │   │   │   ├── SpaceUserPermissionConstant.java # 权限常量
│   │   │   │   │   ├── SpaceUserRole.java           # 角色模型
│   │   │   │   │   ├── SpaceUserAuthContext.java    # 权限上下文
│   │   │   │   │   ├── StpInterfaceImpl.java        # Sa-Token 自定义权限
│   │   │   │   │   ├── StpKits.java                 # Sa-Token 多账号体系
│   │   │   │   │   ├── HttpRequestWrapperFilter.java # 请求包装过滤器
│   │   │   │   │   ├── RequestWrapper.java          # 可重复读取的 Request
│   │   │   │   │   ├── SaTokenConfigure.java        # Sa-Token 配置
│   │   │   │   │   └── anno/SaSpaceCheckPermission.java # 空间权限注解
│   │   │   │   └── websocket/                       # WebSocket 协同编辑
│   │   │   │       ├── PictureEditHandler.java      # 图片编辑处理器
│   │   │   │       ├── WebSocketConfig.java         # WebSocket 配置
│   │   │   │       ├── WsHandshakeInterceptor.java  # 握手拦截器
│   │   │   │       ├── disruptor/                   # Disruptor 高性能队列
│   │   │   │       │   ├── PictureEditEvent.java
│   │   │   │       │   ├── PictureEditEventDisruptorConfig.java
│   │   │   │       │   ├── PictureEditEventProducer.java
│   │   │   │       │   └── PictureEditEventWorkHandler.java
│   │   │   │       └── model/                       # WebSocket 消息模型
│   │   │   │           ├── PictureEditActionEnum.java
│   │   │   │           ├── PictureEditMessageTypeEnum.java
│   │   │   │           ├── PictureEditRequestMessage.java
│   │   │   │           └── PictureEditResponseMessage.java
│   │   │   │
│   │   │   ├── mapper/                              # MyBatis-Flex Mapper
│   │   │   │   ├── PictureMapper.java
│   │   │   │   ├── SpaceMapper.java
│   │   │   │   ├── SpaceUserMapper.java
│   │   │   │   └── UserMapper.java
│   │   │   │
│   │   │   ├── model/                               # 数据模型
│   │   │   │   ├── dto/                             # 请求 DTO
│   │   │   │   ├── entity/                          # 实体类
│   │   │   │   │   ├── User.java                    # 用户
│   │   │   │   │   ├── Picture.java                 # 图片
│   │   │   │   │   ├── Space.java                   # 空间
│   │   │   │   │   ├── SpaceUser.java               # 空间-用户关联
│   │   │   │   │   └── PictureTagCategory.java      # 图片标签分类
│   │   │   │   ├── enums/                           # 枚举
│   │   │   │   │   ├── UserRoleEnum.java
│   │   │   │   │   ├── PictureReviewStatusEnum.java
│   │   │   │   │   ├── SpaceLevelEnum.java
│   │   │   │   │   ├── SpaceTypeEnum.java
│   │   │   │   │   └── SpaceRoleEnum.java
│   │   │   │   └── vo/                              # 视图对象（脱敏）
│   │   │   │       ├── LoginUserVO.java
│   │   │   │       ├── PictureVO.java
│   │   │   │       ├── UserVO.java
│   │   │   │       └── space/                       # 空间相关 VO
│   │   │   │
│   │   │   └── service/                             # 业务服务层
│   │   │       ├── PictureService.java
│   │   │       ├── UserService.java
│   │   │       ├── SpaceService.java
│   │   │       ├── SpaceAnalyzeService.java
│   │   │       ├── SpaceUserService.java
│   │   │       └── impl/                            # 服务实现
│   │   │
│   │   └── resources/                               # 配置文件
│   │       ├── application.yml                      # 主配置
│   │       ├── application-dev.yml                  # 开发环境
│   │       ├── application-prod.yml                 # 生产环境（Docker）
│   │       ├── application-local.yml                # 本地环境
│   │       └── logback-spring.xml                   # 日志配置
│   │
│   └── test/                                        # 测试
│       └── java/com/adventure/picturebackend/
│
├── .gitignore
├── pom.xml
└── README.md
```

---

## 🚀 快速开始

### 环境准备

| 依赖 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 17+ | 编译运行 |
| Maven | 3.8+ | 构建 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 6.0+ | 会话 & 缓存 |
| Docker | 20.10+ | 容器化部署（可选） |

### 本地运行

#### 1. 克隆并初始化数据库

```bash
# 导入数据库脚本
mysql -u root -p < mysql-init/adventure-pucture.sql
```

#### 2. 修改配置

编辑 `application-local.yml`，填入你的配置：

```yaml
mysql:
  host: 127.0.0.1
  port: 3306
  database: mp
  username: root
  password: your-password

redis:
  host: 127.0.0.1
  port: 6379
  database: 3

cos:
  client:
    bucket: your-bucket
    host: your-cos-host
    region: ap-shanghai
    secret-id: your-secret-id
    secret-key: your-secret-key

aliYunAi:
  apiKey: your-aliyun-api-key   # AI 扩图（可选）
```

#### 3. 启动

```bash
# 本地开发（激活 local 环境）
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 或打包后运行
mvn clean package -DskipTests
java -jar target/adventure-picture-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

#### 4. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:8081 | API 服务 |
| http://localhost:8081/doc.html | Knife4j 接口文档（推荐） |
| http://localhost:8081/swagger-ui.html | Swagger 原生文档 |
| http://localhost:8081/v3/api-docs | OpenAPI 3.0 JSON |

### Docker 部署

项目已预置 Dockerfile 和 docker-compose.yml，支持一键部署。

```bash
# 1. 先在项目根目录打包
mvn clean package -DskipTests

# 2. 复制 jar 包到 src/ 目录（docker-compose.yml 所在目录）
cp target/*.jar src/

# 3. 进入 src/ 目录启动
cd src/
docker-compose up -d

# 查看日志
docker-compose logs -f app
```

**docker-compose 服务说明：**

| 服务 | 容器名 | 端口 | 说明 |
|------|--------|------|------|
| `mysql` | mysql-db | 3306 | MySQL 8.0 数据库 |
| `redis` | redis-cache | 6379 | Redis 7 缓存 |
| `app` | springboot-app | 8081 | Spring Boot 应用 |
| `nginx` | nginx | 5173 | Nginx 反向代理（可选） |

> **生产环境**: `application-prod.yml` 在 Docker 网络中使用服务名称（`mysql`、`redis`）作为主机名连接。Spring Boot 启动时通过环境变量 `SPRING_DATASOURCE_URL` 覆盖数据库配置。

> **数据库初始化**: 首次启动 MySQL 容器时，会自动执行 `mysql-init/` 目录下的初始化 SQL 脚本，自动创建表结构。

---

## 🔧 配置说明

### 多环境配置

| Profile | 文件 | 用途 |
|---------|------|------|
| `prod`（默认） | `application-prod.yml` | Docker 生产环境 |
| `dev` | `application-dev.yml` | 开发环境 |
| `local` | `application-local.yml` | 本地开发环境 |

通过在启动参数或 `application.yml` 中修改 `spring.profiles.active` 切换环境。

### 核心配置项

**应用配置**：

```yaml
server:
  port: 8081                           # 服务端口

spring:
  session:
    store-type: redis                  # Session 存储在 Redis
    timeout: 2592000                   # Session 过期时间（30 天）

  servlet:
    multipart:
      max-file-size: 10MB              # 单文件最大 10MB
      max-request-size: 10MB           # 请求最大 10MB
```

**空间容量等级**（可在配置中调整）:

| 等级 | 最大图片数 | 最大容量 |
|------|-----------|---------|
| 普通版（Ordinary） | 100 | 100 MB |
| 专业版（Professional） | 1,000 | 1 GB |
| 旗舰版（Flagship） | 10,000 | 10 GB |

**Sa-Token 认证**：

```yaml
sa-token:
  timeout: 2592000                   # Token 有效期（30 天）
  jwt-secret-key: xxx                # JWT 签名密钥
  is-concurrent: false               # 不允许同一账号并发登录
  max-login-count: 5                 # 同一账号最大登录设备数
```

---

## 🎯 核心功能

### 1. 👤 用户管理

- **注册**: 账号密码注册，非法字符校验，账号脱敏返回
- **登录/登出**: 基于 Sa-Token 的会话管理，Redis 存储
- **权限分级**: `user`（普通用户）、`admin`（管理员）
- **积分系统**: 图片审核通过后可获得积分

### 2. 🖼️ 图片管理

- **上传方式**:
  - 📁 **本地文件上传** — 直接上传 MultipartFile
  - 🔗 **URL 上传** — 从网络地址抓取图片
  - 📦 **批量上传** — 管理员从网站批量抓取（基于 Jsoup 解析）
- **自动图片处理**:
  - 计算图片宽高比（`picScale`）
  - 提取图片主色调（`picColor`）
  - 自动生成 WebP 压缩版本
  - 自动生成 128×128 缩略图（仅对 >20KB 图片）
- **审核机制**: 管理员审核（待审核 / 通过 / 拒绝）
- **以图搜图**: 三步调用门面 — 获取图片页面 → 获取大图 → 搜索结果
- **以颜色搜图**: 基于欧氏距离的颜色相似度算法，空间内搜索相近颜色图片
- **AI 扩图**: 阿里云通义万相 API 实现图片智能扩展（异步任务模式）

### 3. 📦 空间管理

- **空间类型**:
  - 🌍 **公共空间** — 公开图库，无需登录即可查看
  - 🏠 **私有空间** — 个人专属，仅本人和管理员可操作
  - 👥 **团队空间** — 多人协作，支持角色权限管理
- **空间等级**（容量规划）:
  - 普通版（Ordinary）
  - 专业版（Professional）
  - 旗舰版（Flagship）
- **空间分析**:
  - 使用状态分析（已用 / 总量）
  - 图片分类分布统计
  - 图片大小区间分布
  - 标签统计分析
  - 用户活跃度分析
  - 空间排行榜

### 4. 👥 空间成员管理（团队空间）

- **角色划分**:

| 角色 | 权限 |
|------|------|
| 浏览者（Viewer） | 仅查看图片 |
| 编辑者（Editor） | 查看 + 上传 + 编辑 + 删除 |
| 管理员（Admin） | 全部权限 + 空间成员管理 |

- 一个用户可加入多个团队空间
- 基于 Sa-Token 多账号体系实现空间级权限隔离

### 5. 🔄 WebSocket 协同编辑

- 基于 **WebSocket** + **LMAX Disruptor** 高性能无锁队列实现
- 实时广播进入 / 编辑 / 退出事件
- 同一时间仅允许单人编辑，防冲突
- 编辑锁（`ConcurrentHashMap`）管理编辑状态

**消息流向**: `客户端 → WebSocket → Disruptor Producer → RingBuffer → Worker Handler → PictureEditHandler → 广播`

### 6. 🤖 AI 智能扩图

- 集成阿里云通义万相 `image2image/out-painting` API
- 异步任务创建 + 任务状态轮询
- 图片上下文自然扩展

---

## 📡 API 概览

### 用户模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/user/register` | 用户注册 | 公开 |
| POST | `/user/login` | 用户登录 | 公开 |
| POST | `/user/logout` | 用户登出 | 登录 |
| POST | `/user/add` | 创建用户（管理员） | Admin |
| POST | `/user/delete` | 删除用户 | Admin |
| POST | `/user/update` | 更新用户 | Admin |
| POST | `/user/list/page/vo` | 分页查询用户 | Admin |
| GET | `/user/get` | 获取用户详情 | Admin |
| GET | `/user/get/vo` | 获取脱敏用户信息 | 公开 |
| GET | `/user/get/login` | 获取当前登录用户 | 登录 |

### 图片模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/picture/upload` | 上传图片（文件） | Admin |
| POST | `/picture/upload/url` | URL 上传图片 | 空间上传权限 |
| POST | `/picture/upload/batch` | 批量抓取图片 | Admin |
| POST | `/picture/review` | 图片审核 | Admin |
| POST | `/picture/edit` | 编辑图片 | 空间编辑权限 |
| POST | `/picture/edit/batch` | 批量编辑图片 | 空间编辑权限 |
| DELETE | `/picture/delete` | 删除图片 | 空间删除权限 |
| PUT | `/picture/update` | 更新图片（Admin） | Admin |
| POST | `/picture/list/page` | 分页查询（未脱敏） | Admin |
| POST | `/picture/list/page/vo` | 脱敏分页查询 | 公开 |
| GET | `/picture/{id}` | 图片详情（脱敏） | 公开 |
| GET | `/picture/getInfo/{id}` | 图片详情（未脱敏） | Admin |
| GET | `/picture/tag_category` | 标签和分类列表 | 公开 |
| POST | `/picture/search/picture` | 以图搜图 | 空间查看权限 |
| POST | `/picture/search/color` | 以颜色搜图 | 空间查看权限 |
| POST | `/picture/out_painting/create_task` | 创建 AI 扩图任务 | 登录 |
| GET | `/picture/out_painting/get_task` | 查询扩图任务 | 公开 |

### 空间模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/space/save` | 创建空间 | 登录 |
| POST | `/space/update` | 更新空间 | Admin |
| POST | `/space/list/page` | 分页查询（脱敏） | 公开 |
| GET | `/space/{id}` | 空间详情 | 登录 |
| GET | `/space/list/level` | 空间等级列表 | 公开 |

### 空间分析模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/space/analyze/usage` | 空间使用状态分析 |
| POST | `/space/analyze/category` | 图片分类分析 |
| POST | `/space/analyze/size` | 图片大小分布分析 |
| POST | `/space/analyze/tag` | 图片标签分析 |
| POST | `/space/analyze/user` | 用户活跃度分析 |
| POST | `/space/analyze/rank` | 空间排行榜 |

### 空间成员模块

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/spaceUser/save` | 添加成员 | 空间成员管理权限 |
| POST | `/spaceUser/delete` | 移除成员 | 空间成员管理权限 |
| PUT | `/spaceUser/update` | 修改角色 | 空间成员管理权限 |
| GET | `/spaceUser/list` | 成员列表 | 空间成员管理权限 |
| POST | `/spaceUser/get` | 成员详情 | 空间成员管理权限 |
| POST | `/spaceUser/list/myTeamSpace` | 我的团队空间列表 | 登录 |

### WebSocket

| 路径 | 说明 |
|------|------|
| `ws://host:8081/ws/picture/edit` | 图片协同编辑 WebSocket 端点 |

---

## 🔐 权限体系

本系统采用 **双层权限校验** 设计：

### 1. 全局管理权限（`@AuthCheck`）

通过 `@AuthCheck(mustRole = "admin")` 注解标记，由 `AuthInterceptor` AOP 切面拦截，校验当前登录用户是否为管理员角色。

### 2. 空间级权限（Sa-Token 多账号体系）

使用 Sa-Token 的 **多账号体系**，为空间认证创建独立的 `StpLogic`（`StpKits.SPACE`），与用户登录体系完全隔离，实现空间间的权限隔离。

**权限校验流程：**

```
请求 → @SaSpaceCheckPermission → StpInterfaceImpl.getPermissionList
  → 解析请求上下文（SpaceUserAuthContext）
    → 公共图库：管理员=全部权限，普通用户=仅查看
    → 私有空间：本人或管理员=全部权限，其他=无权限
    → 团队空间 → 查询 SpaceUser → 获取角色权限
      → Admin：全部权限
      → Editor：查看+上传+编辑+删除
      → Viewer：仅查看
```

**权限常量**（`SpaceUserPermissionConstant`）：

| 常量 | 值 | 说明 |
|------|-----|------|
| `SPACE_USER_MANAGE` | `spaceUser:manage` | 空间成员管理 |
| `PICTURE_VIEW` | `picture:view` | 图片查看 |
| `PICTURE_UPLOAD` | `picture:upload` | 图片上传 |
| `PICTURE_EDIT` | `picture:edit` | 图片编辑 |
| `PICTURE_DELETE` | `picture:delete` | 图片删除 |

---

## 🏗️ 架构设计

### 总体架构

```
┌─────────────┐     ┌──────────────┐     ┌───────────┐
│  客户端      │     │   Nginx      │     │  Knife4j  │
│  (Web/App)   │ ──▶ │  (反向代理)   │ ──▶ │  接口文档  │
└──────┬──────┘     └──────────────┘     └───────────┘
       │
       ▼
┌──────────────────────────────────────────────────────┐
│              Spring Boot 应用                          │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Controller 层                                    │ │
│  │  ┌──────┐ ┌────────┐ ┌──────┐ ┌─────────────┐   │ │
│  │  │User  │ │Picture │ │Space │ │SpaceAnalyze  │   │ │
│  │  └──┬───┘ └───┬────┘ └──┬───┘ └──────┬──────┘   │ │
│  │     │          │         │             │          │ │
│  │     └──── AOP ─┼─────────┼─────────────┘          │ │
│  │          (AuthInterceptor)  │                     │ │
│  │     ┌──────────┼───────────┼──────────────┐       │ │
│  │     │  Service 层         │               │       │ │
│  │     │  ┌──────────┐ ┌─────┴──────┐ ┌──────┴───┐  │ │
│  │     │  │Picture   │ │Space       │ │SpaceAna  │  │ │
│  │     │  └────┬─────┘ └─────┬──────┘ └──────┬───┘  │ │
│  │     ├───────┼─────────────┼─────────────────┤     │ │
│  │     │  Manager 层          │                  │     │ │
│  │     │  ┌──────┐ ┌───────┐ ┌──────┐ ┌──────┐ │     │ │
│  │     │  │COS   │ │Upload │ │Auth  │ │WS    │ │     │ │
│  │     │  └──┬───┘ └──┬────┘ └──┬───┘ └──┬───┘ │     │ │
│  │     ├─────┼─────────┼────────┼─────────┼─────┤     │ │
│  │     │  Mapper 层 + MyBatis-Flex ORM    │     │     │ │
│  │     └─────┼─────────┼────────┼─────────┼─────┘     │ │
│  └───────────┼─────────┼────────┼─────────┼───────────┘ │
└──────────────┼─────────┼────────┼─────────┼─────────────┘
               │         │        │         │
          ┌────▼───┐ ┌──▼──┐ ┌───▼────┐ ┌──▼──────┐
          │ MySQL  │ │Redis│ │腾讯云   │ │ 阿里云   │
          │   8.0  │ │  7  │ │ COS    │ │DashScope│
          └────────┘ └─────┘ └────────┘ └─────────┘
```

### 设计模式应用

| 模式 | 使用位置 | 说明 |
|------|---------|------|
| **模板方法** | `FileUploadTemplate` | 定义图片上传流程骨架，子类实现文件/URL 上传细节 |
| **门面模式** | `ImageSearchApiFacade` | 封装以图搜图三步 API 调用 |
| **生产者-消费者** | Disruptor | WebSocket 消息异步处理，削峰填谷 |
| **AOP 切面** | `AuthInterceptor` | 权限校验横切关注点 |

### 图片上传流程

```
上传请求 → 校验（大小/格式/内容）→ 创建临时文件
  → 上传至腾讯云 COS → COS 图片处理（CI）
    → 生成 WebP 压缩图
    → 生成缩略图（128×128）
    → 提取图片信息（宽/高/格式/主色调）
  → 保存图片信息到数据库
  → 清理临时文件 → 返回 PictureVO
```

### WebSocket 协同编辑设计

```
用户A ──▶ WebSocket ──▶ Disruptor Producer ──┐
用户B ──▶ WebSocket ──▶ Disruptor Producer ──┤
用户C ──▶ WebSocket ──▶ Disruptor Producer ──┤
                                              │
                                              ▼
                                     Disruptor RingBuffer
                                      （256K slots, 无锁）
                                              │
                                              ▼
                                     Disruptor Worker Handler
                                              │
                                              ▼
                                     PictureEditHandler
                                     （单线程按序处理）
                                              │
                                              ▼
                              ┌───────────────┼───────────────┐
                              ▼               ▼               ▼
                           用户A            用户B            用户C
                       （广播, 排除发送者）
```

---

## 💻 开发指南

### 代码生成

项目集成了 MyBatis-Flex 代码生成器，位于 `src/test/java/com/adventure/picturebackend/genernate/Codegen.java`，可根据数据库表自动生成 Entity、Mapper、Service、Controller 全量代码。

### 日志

- 基于 Logback，配置文件：`logback-spring.xml`
- 日志根目录：`${user.dir}/adventurePicture-logs/`
- 按天滚动，保留 7 天，单文件最大 100MB
- 控制台彩色输出（Spring Boot 风格）

### 异步支持

通过 `@EnableAsync` + `AsyncConfig` 线程池配置：

```java
@Bean("asyncExecutor")
public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    return executor;
}
```

### 缓存策略

| 层次 | 技术 | 用途 |
|------|------|------|
| 本地缓存 | Caffeine | 图片分页列表缓存 |
| 分布式缓存 | Redis | Session 共享、分布式锁、热点数据 |
| 会话管理 | Spring Session + Redis | 分布式环境下的 Session 一致性 |

### 事务管理

使用 Spring 声明式事务（`@Transactional`）+ `TransactionTemplate` 编程式事务，确保关键业务操作（如图片空间容量扣减）的原子性。

---

## ❓ 常见问题

### Q: 数据库连接失败

**本地开发**: 检查 `application-local.yml` 中 `mysql.host` 是否为 `127.0.0.1`，端口、用户名密码是否正确。  
**Docker 环境**: 确保 `application-prod.yml` 中 `mysql.host` 为 `mysql`（容器服务名）。

### Q: 图片上传失败

1. 检查腾讯云 COS 配置（secret-id、secret-key、bucket、region）
2. 确认 COS Bucket 已设置跨域规则（CORS）
3. 检查文件大小不超过 10MB（服务端限制），且为支持的图片格式
4. 检查当前用户对目标空间是否有 `picture:upload` 权限

### Q: WebSocket 连接失败

1. 前端连接地址应为 `ws://host:8081/ws/picture/edit`
2. 握手拦截器会校验用户登录状态，确保已先登录获取 Session
3. 检查网络防火墙是否拦截 WebSocket 升级请求

### Q: AI 扩图不生效

1. 检查 `aliYunAi.apiKey` 是否配置正确
2. 确认阿里云账户已开通「通义万相」服务
3. AI 扩图为异步任务，创建后需调用查询接口轮询处理结果

### Q: 以图搜图不返回结果

该功能基于第三方图片搜索引擎，请求量过大可能会被限制。建议合理控制调用频率。可通过 `ImageSearchApiFacade` 的 `main` 方法单独测试。



