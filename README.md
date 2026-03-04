# 用户登录模块

Vue 3 + Spring Boot 3 全栈用户登录系统。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Vue Router + Axios + Element Plus |
| 后端 | Spring Boot 3 + Spring Security + MyBatis-Plus |
| 认证 | JWT（双 Token 机制：Access Token + Refresh Token） |
| 数据库 | MySQL 8.0 |

## 功能

- 用户注册（含表单校验）
- 用户登录（含图形验证码）
- 记住我（Refresh Token 自动续期）
- 忘记密码（邮箱重置，模拟实现）
- 登录后用户信息展示
- 逻辑删除（MyBatis-Plus `@TableLogic`）
- 雪花算法主键（全局唯一 ID）

## 项目结构

```
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/login/
│       ├── LoginApplication.java
│       ├── common/             # 通用类（Result、Request DTO）
│       ├── config/             # 配置（Security、MyBatis-Plus、全局异常）
│       ├── controller/         # 控制器
│       ├── entity/             # 实体类
│       ├── mapper/             # MyBatis Mapper
│       ├── security/           # JWT 过滤器
│       ├── service/            # 业务层
│       └── util/               # 工具类（JwtUtil）
├── frontend/                   # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/                # API 请求
│       ├── router/             # 路由配置
│       ├── utils/              # Axios 封装
│       └── views/              # 页面组件
└── sql/
    └── init.sql                # 建表脚本
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 启动后端

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库连接信息
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`

## API 接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/auth/captcha` | 获取图形验证码 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/forgot-password` | 忘记密码 | 否 |
| POST | `/api/auth/refresh-token` | 刷新 Token | 否 |
| GET | `/api/user/info` | 获取用户信息 | 是 |

## 页面风格

紫色渐变背景 + 毛玻璃效果卡片，现代 SaaS 风格设计。
