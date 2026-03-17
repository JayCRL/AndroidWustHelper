# mywust_basic (基础服务核心)

## 📖 简介
`mywust_basic` 是整个 WustProject 生态系统的核心基础服务模块。它主要负责用户认证（JWT）、基础信息管理、文件上传（OSS）以及作为微服务网关的上游服务提供者。

## ⚙️ 快速配置

### 1. 端口与环境
*   **Port**: `8082`
*   **Context Path**: `/`
*   **Java Version**: JDK 17

### 2. 依赖服务
*   **MySQL**: 数据库 `wust_basic`
*   **Redis**: 用于缓存和 Token 存储
*   **Nacos**: 服务注册 (localhost:8848)
*   **Aliyun OSS**: 图片/文件上传

### 3. 核心配置 (`application.properties`)
位于 `src/main/resources/application.properties`。
**注意**: 请检查数据库账号密码及 OSS Key 是否正确。

```properties
server.port=8082
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/wust_basic?...
# Redis
spring.data.redis.host=localhost
# OSS (需要替换为你的真实Key)
jspv.oss.access-key-id=YOUR_ID
jspv.oss.access-key-secret=YOUR_SECRET
```

## 🏗️ 代码目录详细说明

```text
src/main/java/com/linghang/backend/mywust_basic/
├── Config/               # 核心配置类
│   ├── SecurityConfig    # Spring Security 权限配置
│   └── MybatisPlusConfig # 分页插件及自动填充配置
├── Controller/           # API 接口层
│   ├── LoginController   # 登录认证接口
│   ├── NoticeController  # 系统公告管理接口
│   └── PictureController # 图片/文件上传接口
├── Dao/                  # 实体层 (Entity/POJO)
│   ├── Manager           # 管理员实体类
│   ├── Notice            # 公告实体类
│   └── Picture           # 图片资源实体类
├── Mapper/               # MyBatis Plus Mapper 接口
├── Service/              # 业务逻辑接口层
│   ├── NoticeService
│   └── PictureService
├── ServiceImpl/          # 业务逻辑实现层
└── Utils/                # 工具类
    ├── AliyunOSSUtil     # 阿里云 OSS 操作工具
    ├── JwtUtils          # JWT Token 生成与解析
    └── Result            # 统一结果返回包装类
```

### 核心包功能：
- **Config**: 定义了系统的行为。例如 `SecurityConfig` 决定了哪些接口需要登录（如上传图片），哪些接口公开（如公告查看）。
- **Dao (Entity)**: 映射数据库表。使用了 MyBatis Plus 注解，如 `@TableId` 和 `@TableName`。
- **Utils**: 核心基础设施。`AliyunOSSUtil` 封装了上传逻辑，支持获取图片的 URL；`JwtUtils` 负责跨服务的身份验证。

## 💡 优化建议

1.  **命名规范**: 包名 `Dao` 存放实体类容易引起混淆，建议重构。
2.  **安全性**: OSS 的 AccessKey 目前明文写在配置文件中，建议使用 Nacos 配置加密存储或环境变量。
3.  **异常处理**: 建议添加全局异常处理器 (`@ControllerAdvice`) 来统一 API 的错误返回格式。
