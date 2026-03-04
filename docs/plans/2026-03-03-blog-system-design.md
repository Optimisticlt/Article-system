# 技术博客系统设计

## 概述

在现有用户登录模块基础上，扩展为完整的技术博客社区系统。包含前台（用户端）和后台（管理端）两套界面。

## 系统架构

```
前台（用户端）— 简洁博客风格（白色 + 卡片布局）
  首页 / 文章详情 / 发布文章 / 个人主页 / 搜索

后台（管理端）— 后台管理布局（侧边栏 + 顶栏）
  Dashboard / 文章管理 / 分类标签 / 用户管理 / 评论管理

Spring Boot 后端 API — 统一 JWT 认证 + role 字段区分角色

MySQL 数据库
```

## 角色设计

在 `sys_user` 表增加 `role` 字段（VARCHAR(10), 默认 'user'）。

| 角色 | 前台权限 | 后台权限 |
|------|---------|---------|
| 游客 | 浏览文章、搜索 | 无 |
| user | 浏览 + 发文 + 评论 + 点赞 | 无 |
| admin | 同 user | 全部后台管理功能 |

## 数据模型

### 修改现有表

`sys_user` 增加字段：
- `nickname` VARCHAR(50) — 昵称
- `avatar` VARCHAR(255) — 头像 URL
- `role` VARCHAR(10) DEFAULT 'user' — 角色

### 新增表

**article** — 文章表
- id (BIGINT, 雪花), title, content (LONGTEXT), summary, cover_url, status (0草稿/1已发布), view_count, user_id, category_id, create_time, update_time, is_deleted

**category** — 分类表
- id (BIGINT, 雪花), name, sort, status, create_time, update_time, is_deleted

**tag** — 标签表
- id (BIGINT, 雪花), name, create_time

**article_tag** — 文章标签关联表（多对多）
- article_id, tag_id

**comment** — 评论表
- id (BIGINT, 雪花), content, article_id, user_id, parent_id (支持回复), create_time, is_deleted

**article_like** — 点赞表
- id (BIGINT, 雪花), article_id, user_id, create_time

## 前台页面

| 页面 | 路由 | 功能 |
|------|------|------|
| 首页 | `/` | 文章卡片列表（封面+标题+摘要+作者+时间），分页，分类/标签筛选 |
| 文章详情 | `/article/:id` | Markdown 渲染、作者信息、点赞/收藏、评论区 |
| 发布文章 | `/publish` | Markdown 编辑器、分类、标签、封面上传（需登录） |
| 编辑文章 | `/edit/:id` | 同发布页，回填数据（仅作者可编辑） |
| 个人主页 | `/user/:id` | 用户文章列表、获赞数、文章数 |
| 搜索 | `/search` | 关键词搜索结果 |
| 登录/注册 | `/login`, `/register` | 复用现有页面 |

顶部导航栏：Logo、首页、搜索框、登录/头像下拉（我的主页、发布文章、退出）

## 后台页面

| 页面 | 路由 | 功能 |
|------|------|------|
| Dashboard | `/admin` | 统计卡片 + ECharts 图表 |
| 文章管理 | `/admin/article` | 全部文章列表，审核/删除/置顶 |
| 分类管理 | `/admin/category` | CRUD |
| 标签管理 | `/admin/tag` | CRUD |
| 用户管理 | `/admin/user` | 用户列表，启用/禁用 |
| 评论管理 | `/admin/comment` | 查看/删除评论 |
| 个人中心 | `/admin/profile` | 修改昵称、邮箱、密码、头像 |

后台布局：顶部导航 + 左侧菜单 + 内容区域

## API 设计

### 公开接口（无需认证）
- GET `/api/article/list` — 分页查询已发布文章
- GET `/api/article/{id}` — 文章详情（浏览量+1）
- GET `/api/category/list` — 分类列表
- GET `/api/tag/list` — 标签列表
- GET `/api/article/search?keyword=` — 搜索文章
- GET `/api/user/{id}/profile` — 用户公开信息

### 用户接口（需登录）
- POST `/api/article` — 发布文章
- PUT `/api/article` — 更新文章（仅作者）
- DELETE `/api/article/{id}` — 删除自己的文章
- POST `/api/article/upload` — 上传封面图
- POST `/api/comment` — 发表评论
- POST `/api/article/{id}/like` — 点赞/取消点赞
- GET `/api/user/info` — 获取当前用户信息（已有）
- PUT `/api/user/profile` — 更新个人信息
- PUT `/api/user/password` — 修改密码
- POST `/api/user/avatar` — 上传头像

### 管理员接口（需 admin 角色）
- GET `/api/admin/article/list` — 全部文章（含草稿）
- PUT `/api/admin/article/status` — 审核/下架文章
- DELETE `/api/admin/article/{id}` — 删除任意文章
- POST/PUT/DELETE `/api/admin/category` — 分类管理
- POST/DELETE `/api/admin/tag` — 标签管理
- GET `/api/admin/user/list` — 用户列表
- PUT `/api/admin/user/status` — 启用/禁用用户
- GET `/api/admin/comment/list` — 评论列表
- DELETE `/api/admin/comment/{id}` — 删除评论
- GET `/api/admin/dashboard` — Dashboard 统计数据

## 技术选型

| 项 | 选择 |
|----|------|
| Markdown 编辑器 | md-editor-v3 |
| 图表 | ECharts |
| 文件上传 | Spring Boot 本地存储 uploads/ |
| 前后台路由 | Vue Router, `/` 前台布局, `/admin` 后台布局 |
| 分页 | MyBatis-Plus Page 插件 |
