# 技术博客系统 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在现有登录模块基础上，构建完整的技术博客社区系统（前台用户端 + 后台管理端）。

**Architecture:** 前后端分离，后端 Spring Boot 3 提供 RESTful API，前端 Vue 3 通过 Vue Router 区分前台（`/`）和后台（`/admin`）两套布局。用户角色通过 `sys_user.role` 字段区分，JWT token 中携带角色信息，后端接口通过自定义注解或手动校验控制权限。

**Tech Stack:** Vue 3 + Vite + Element Plus + md-editor-v3 + ECharts (前端), Spring Boot 3 + MyBatis-Plus + Spring Security + JWT (后端), MySQL 8.0

---

## Phase 1: 数据库与后端基础

### Task 1: 数据库表结构更新

**Files:**
- Modify: `sql/init.sql`

**Step 1: 更新 init.sql，增加所有新表和 sys_user 新字段**

在现有 `sys_user` 建表语句后追加：

```sql
-- Alter sys_user: add nickname, avatar, role
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS nickname VARCHAR(50) DEFAULT NULL COMMENT 'Nickname';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS avatar VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS role VARCHAR(10) NOT NULL DEFAULT 'user' COMMENT 'Role: user/admin';

-- Update preset admin account role
UPDATE sys_user SET role = 'admin' WHERE user_name = 'admin';

-- Category table
CREATE TABLE IF NOT EXISTS category (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    name        VARCHAR(50)  NOT NULL COMMENT 'Category name',
    sort        INT          NOT NULL DEFAULT 0 COMMENT 'Sort order',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 0=disabled, 1=active',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article category';

-- Tag table
CREATE TABLE IF NOT EXISTS tag (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    name        VARCHAR(30)  NOT NULL COMMENT 'Tag name',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article tag';

-- Article table
CREATE TABLE IF NOT EXISTS article (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    title       VARCHAR(200) NOT NULL COMMENT 'Title',
    content     LONGTEXT     NOT NULL COMMENT 'Markdown content',
    summary     VARCHAR(500) DEFAULT NULL COMMENT 'Summary',
    cover_url   VARCHAR(255) DEFAULT NULL COMMENT 'Cover image URL',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=draft, 1=published',
    view_count  INT          NOT NULL DEFAULT 0 COMMENT 'View count',
    user_id     BIGINT       NOT NULL COMMENT 'Author ID',
    category_id BIGINT       DEFAULT NULL COMMENT 'Category ID',
    is_top      TINYINT      NOT NULL DEFAULT 0 COMMENT 'Pinned: 0=no, 1=yes',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article';

-- Article-Tag relation table
CREATE TABLE IF NOT EXISTS article_tag (
    article_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article-Tag relation';

-- Comment table
CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT       NOT NULL COMMENT 'Primary key (snowflake)',
    content     VARCHAR(1000) NOT NULL COMMENT 'Comment content',
    article_id  BIGINT       NOT NULL COMMENT 'Article ID',
    user_id     BIGINT       NOT NULL COMMENT 'Commenter ID',
    parent_id   BIGINT       DEFAULT NULL COMMENT 'Parent comment ID (for replies)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment';

-- Article like table
CREATE TABLE IF NOT EXISTS article_like (
    id          BIGINT   NOT NULL COMMENT 'Primary key (snowflake)',
    article_id  BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_user (article_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Article like';

-- Preset categories
INSERT IGNORE INTO category (id, name, sort) VALUES
(1, '技术分享', 1),
(2, '学习笔记', 2),
(3, '项目实战', 3),
(4, '生活随笔', 4);

-- Preset tags
INSERT IGNORE INTO tag (id, name) VALUES
(1, 'Java'), (2, 'Spring Boot'), (3, 'Vue'), (4, 'MySQL'),
(5, 'JavaScript'), (6, '前端'), (7, '后端'), (8, '数据库');
```

**Step 2: 执行 SQL**

Run: `mysql -u root -proot < sql/init.sql`

**Step 3: Commit**

```bash
git add sql/init.sql
git commit -m "feat: add blog system tables (article, category, tag, comment, like)"
```

---

### Task 2: 后端实体类

**Files:**
- Modify: `backend/src/main/java/com/login/entity/SysUser.java` — 增加 nickname, avatar, role 字段
- Create: `backend/src/main/java/com/login/entity/Article.java`
- Create: `backend/src/main/java/com/login/entity/Category.java`
- Create: `backend/src/main/java/com/login/entity/Tag.java`
- Create: `backend/src/main/java/com/login/entity/ArticleTag.java`
- Create: `backend/src/main/java/com/login/entity/Comment.java`
- Create: `backend/src/main/java/com/login/entity/ArticleLike.java`

每个实体使用 Lombok `@Data`，MyBatis-Plus `@TableName`，`@TableId(type = IdType.ASSIGN_ID)` 雪花算法，`@TableLogic` 逻辑删除（适用的表），`@TableField(fill = FieldFill.INSERT)` 自动填充时间。

**Step 1: 修改 SysUser.java 增加字段**

增加：
```java
private String nickname;
private String avatar;
private String role;
```

**Step 2: 创建所有新实体类**

每个实体类参照 SysUser 的模式，使用 Lombok + MyBatis-Plus 注解。

**Step 3: Commit**

```bash
git add backend/src/main/java/com/login/entity/
git commit -m "feat: add entity classes for blog system"
```

---

### Task 3: 后端 Mapper 层

**Files:**
- Create: `backend/src/main/java/com/login/mapper/ArticleMapper.java`
- Create: `backend/src/main/java/com/login/mapper/CategoryMapper.java`
- Create: `backend/src/main/java/com/login/mapper/TagMapper.java`
- Create: `backend/src/main/java/com/login/mapper/ArticleTagMapper.java`
- Create: `backend/src/main/java/com/login/mapper/CommentMapper.java`
- Create: `backend/src/main/java/com/login/mapper/ArticleLikeMapper.java`

每个 Mapper 继承 `BaseMapper<Entity>`，加 `@Mapper` 注解。

**Step 1: 创建所有 Mapper 接口**

**Step 2: Commit**

```bash
git add backend/src/main/java/com/login/mapper/
git commit -m "feat: add mapper interfaces for blog system"
```

---

### Task 4: 后端 DTO/VO 类

**Files:**
- Create: `backend/src/main/java/com/login/common/ArticleRequest.java` — 发布/编辑文章请求体（title, content, summary, coverUrl, status, categoryId, tagIds[]）
- Create: `backend/src/main/java/com/login/common/ArticleVO.java` — 文章列表/详情返回体（含作者昵称、分类名、标签列表、点赞数、评论数）
- Create: `backend/src/main/java/com/login/common/CommentRequest.java` — 评论请求体
- Create: `backend/src/main/java/com/login/common/CommentVO.java` — 评论返回体（含用户昵称头像、子评论）
- Create: `backend/src/main/java/com/login/common/UserProfileVO.java` — 用户公开信息
- Create: `backend/src/main/java/com/login/common/DashboardVO.java` — Dashboard 统计数据
- Create: `backend/src/main/java/com/login/common/PageResult.java` — 通用分页返回体

**Step 1: 创建所有 DTO/VO 类**

**Step 2: Commit**

```bash
git add backend/src/main/java/com/login/common/
git commit -m "feat: add DTOs and VOs for blog system"
```

---

### Task 5: 后端 Service 层 — 分类与标签

**Files:**
- Create: `backend/src/main/java/com/login/service/CategoryService.java`
- Create: `backend/src/main/java/com/login/service/impl/CategoryServiceImpl.java`
- Create: `backend/src/main/java/com/login/service/TagService.java`
- Create: `backend/src/main/java/com/login/service/impl/TagServiceImpl.java`

分类 Service：list（查询所有启用分类）、add、update、delete
标签 Service：list（查询所有标签）、add、delete

**Step 1: 实现 CategoryService**
**Step 2: 实现 TagService**
**Step 3: Commit**

```bash
git add backend/src/main/java/com/login/service/
git commit -m "feat: add category and tag service layer"
```

---

### Task 6: 后端 Service 层 — 文章核心

**Files:**
- Create: `backend/src/main/java/com/login/service/ArticleService.java`
- Create: `backend/src/main/java/com/login/service/impl/ArticleServiceImpl.java`

功能：
- `publishArticle(ArticleRequest, userId)` — 发布文章 + 保存标签关联
- `updateArticle(ArticleRequest, userId)` — 更新文章 + 更新标签（先删后插）
- `deleteArticle(id, userId)` — 逻辑删除（校验作者）
- `getArticleDetail(id)` — 文章详情 + 浏览量+1 + 组装 VO（作者、分类、标签、点赞数、评论数）
- `listArticles(page, size, categoryId, tagId, keyword)` — 分页查询已发布文章
- `listMyArticles(page, size, userId)` — 查询自己的文章（含草稿）
- `searchArticles(keyword, page, size)` — 关键词搜索

**Step 1: 实现 ArticleService**
**Step 2: Commit**

```bash
git add backend/src/main/java/com/login/service/
git commit -m "feat: add article service with CRUD and search"
```

---

### Task 7: 后端 Service 层 — 评论与点赞

**Files:**
- Create: `backend/src/main/java/com/login/service/CommentService.java`
- Create: `backend/src/main/java/com/login/service/impl/CommentServiceImpl.java`
- Create: `backend/src/main/java/com/login/service/ArticleLikeService.java`
- Create: `backend/src/main/java/com/login/service/impl/ArticleLikeServiceImpl.java`

评论 Service：addComment、listByArticleId（树形结构）、deleteComment
点赞 Service：toggleLike（点赞/取消）、isLiked、countByArticleId

**Step 1: 实现 CommentService**
**Step 2: 实现 ArticleLikeService**
**Step 3: Commit**

```bash
git add backend/src/main/java/com/login/service/
git commit -m "feat: add comment and like services"
```

---

### Task 8: 后端 Service 层 — 用户扩展 & Dashboard

**Files:**
- Modify: `backend/src/main/java/com/login/service/SysUserService.java` — 增加方法
- Modify: `backend/src/main/java/com/login/service/impl/SysUserServiceImpl.java` — 实现

新增方法：
- `getUserProfile(userId)` — 获取用户公开信息（昵称、头像、文章数、获赞数）
- `updateProfile(userId, nickname, email)` — 更新个人信息
- `updatePassword(userId, oldPwd, newPwd)` — 修改密码
- `updateAvatar(userId, avatarUrl)` — 更新头像
- `listUsers(page, size)` — 用户列表（管理员）
- `updateUserStatus(userId, status)` — 启用/禁用用户（管理员）
- `getDashboardStats()` — Dashboard 统计数据

**Step 1: 扩展 SysUserService 接口和实现**
**Step 2: Commit**

```bash
git add backend/src/main/java/com/login/service/
git commit -m "feat: extend user service with profile, dashboard"
```

---

### Task 9: 后端 Controller 层 — 公开接口 & 文件上传

**Files:**
- Create: `backend/src/main/java/com/login/controller/ArticleController.java`
- Create: `backend/src/main/java/com/login/controller/CategoryController.java`
- Create: `backend/src/main/java/com/login/controller/TagController.java`
- Create: `backend/src/main/java/com/login/controller/CommentController.java`
- Create: `backend/src/main/java/com/login/controller/FileController.java`
- Modify: `backend/src/main/java/com/login/controller/UserController.java` — 增加 profile 接口

FileController 处理封面图和头像上传，保存到 `uploads/` 目录，返回可访问 URL。

需要在 `application.yml` 配置上传路径和静态资源映射。

**Step 1: 创建所有 Controller**
**Step 2: 配置文件上传**

在 `application.yml` 添加：
```yaml
file:
  upload-path: ./uploads/

spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB
```

创建 `WebMvcConfig.java` 配置静态资源映射 `/uploads/**`。

**Step 3: Commit**

```bash
git add backend/src/main/java/com/login/controller/ backend/src/main/java/com/login/config/ backend/src/main/resources/
git commit -m "feat: add all controllers and file upload config"
```

---

### Task 10: 后端 Controller 层 — 管理员接口

**Files:**
- Create: `backend/src/main/java/com/login/controller/AdminArticleController.java`
- Create: `backend/src/main/java/com/login/controller/AdminCategoryController.java`
- Create: `backend/src/main/java/com/login/controller/AdminTagController.java`
- Create: `backend/src/main/java/com/login/controller/AdminUserController.java`
- Create: `backend/src/main/java/com/login/controller/AdminCommentController.java`
- Create: `backend/src/main/java/com/login/controller/AdminDashboardController.java`

所有 Admin Controller 路径以 `/api/admin/` 开头，在 SecurityConfig 或 JwtAuthenticationFilter 中校验 role=admin。

**Step 1: 创建管理员 Controller**
**Step 2: 修改 SecurityConfig 和 JwtAuthenticationFilter 支持角色校验**

在 JwtUtil 中生成 token 时加入 role，解析时取出 role。
在 JwtAuthenticationFilter 中将 role 设置到 SecurityContext 的 authorities 中。
在 SecurityConfig 中配置 `/api/admin/**` 需要 ROLE_ADMIN。

**Step 3: Commit**

```bash
git add backend/src/main/java/com/login/
git commit -m "feat: add admin controllers with role-based access"
```

---

## Phase 2: 前端系统

### Task 11: 前端依赖安装与路由重构

**Files:**
- Modify: `frontend/package.json` — 增加 md-editor-v3, echarts 依赖
- Modify: `frontend/src/router/index.js` — 重构路由（前台布局 + 后台布局）
- Create: `frontend/src/layout/FrontLayout.vue` — 前台布局（顶部导航 + 内容区）
- Create: `frontend/src/layout/AdminLayout.vue` — 后台布局（顶栏 + 侧边栏 + 内容区）

**Step 1: 安装依赖**

Run: `cd frontend && npm install md-editor-v3 echarts`

**Step 2: 创建两套布局组件**

FrontLayout.vue: 顶部导航栏（Logo、首页、搜索框、登录/用户下拉），底部 footer，中间 `<router-view>`
AdminLayout.vue: Element Plus el-container 布局，侧边栏菜单，顶栏用户信息，`<router-view>`

**Step 3: 重构路由**

```javascript
const routes = [
  // 登录注册（无布局）
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/forgot-password', component: ForgotPassword },
  // 前台（FrontLayout）
  {
    path: '/',
    component: FrontLayout,
    children: [
      { path: '', component: HomeView },
      { path: 'article/:id', component: ArticleDetail },
      { path: 'publish', component: PublishArticle, meta: { requireAuth: true } },
      { path: 'edit/:id', component: EditArticle, meta: { requireAuth: true } },
      { path: 'user/:id', component: UserProfile },
      { path: 'search', component: SearchResult },
    ]
  },
  // 后台（AdminLayout）
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      { path: '', component: Dashboard },
      { path: 'article', component: AdminArticle },
      { path: 'category', component: AdminCategory },
      { path: 'tag', component: AdminTag },
      { path: 'user', component: AdminUser },
      { path: 'comment', component: AdminComment },
      { path: 'profile', component: AdminProfile },
    ]
  }
]
```

路由守卫：检查 token 和 role，未登录跳 login，非 admin 访问 /admin 跳首页。

**Step 4: Commit**

```bash
git add frontend/
git commit -m "feat: add layouts, restructure routes for front/admin"
```

---

### Task 12: 前端 API 层

**Files:**
- Create: `frontend/src/api/article.js`
- Create: `frontend/src/api/category.js`
- Create: `frontend/src/api/tag.js`
- Create: `frontend/src/api/comment.js`
- Create: `frontend/src/api/user.js`
- Create: `frontend/src/api/admin.js`

每个文件封装对应后端 API 的 axios 请求。

**Step 1: 创建所有 API 文件**
**Step 2: Commit**

```bash
git add frontend/src/api/
git commit -m "feat: add frontend API layer for all modules"
```

---

### Task 13: 前台 — 首页 & 文章详情

**Files:**
- Create: `frontend/src/views/front/HomeView.vue` — 文章卡片列表 + 分页 + 分类标签筛选
- Create: `frontend/src/views/front/ArticleDetail.vue` — Markdown 渲染 + 作者信息 + 点赞 + 评论区
- Create: `frontend/src/views/front/SearchResult.vue` — 搜索结果

首页：文章卡片网格（封面图、标题、摘要、作者头像昵称、时间、浏览量、点赞数），分类 tab 切换，标签筛选，分页。

文章详情：md-editor-v3 的 MdPreview 组件渲染 Markdown，作者卡片，点赞按钮，评论列表（支持回复），发表评论输入框。

**Step 1: 实现 HomeView.vue**
**Step 2: 实现 ArticleDetail.vue**
**Step 3: 实现 SearchResult.vue**
**Step 4: Commit**

```bash
git add frontend/src/views/front/
git commit -m "feat: add front-end home, article detail, search pages"
```

---

### Task 14: 前台 — 发布文章 & 个人主页

**Files:**
- Create: `frontend/src/views/front/PublishArticle.vue` — Markdown 编辑器 + 表单
- Create: `frontend/src/views/front/UserProfile.vue` — 用户公开主页

发布文章：md-editor-v3 编辑器，标题输入，分类下拉，标签多选（el-select multiple），封面图上传（el-upload），摘要 textarea，草稿/发布按钮。编辑文章复用此页面，通过路由参数区分。

个人主页：用户头像 + 昵称 + 文章数 + 获赞数 + 该用户的文章列表。

**Step 1: 实现 PublishArticle.vue**
**Step 2: 实现 UserProfile.vue**
**Step 3: Commit**

```bash
git add frontend/src/views/front/
git commit -m "feat: add publish article and user profile pages"
```

---

### Task 15: 后台 — Dashboard

**Files:**
- Create: `frontend/src/views/admin/Dashboard.vue`

统计卡片（4 个）：文章总数、已发布数、评论数、总浏览量。使用 Element Plus 的 el-card + el-statistic。

ECharts 图表：分类文章数量柱状图、最近 7 天文章发布趋势折线图。

最近文章列表：最新 5 篇文章。

**Step 1: 实现 Dashboard.vue**
**Step 2: Commit**

```bash
git add frontend/src/views/admin/Dashboard.vue
git commit -m "feat: add admin dashboard with charts"
```

---

### Task 16: 后台 — 文章管理 & 分类标签管理

**Files:**
- Create: `frontend/src/views/admin/AdminArticle.vue`
- Create: `frontend/src/views/admin/AdminCategory.vue`
- Create: `frontend/src/views/admin/AdminTag.vue`

文章管理：el-table 表格（标题、作者、分类、状态、浏览量、创建时间），搜索栏，分页，操作按钮（审核/下架/删除/置顶）。

分类管理：el-table + el-dialog 表单 CRUD。
标签管理：标签列表 + 新增/删除。

**Step 1: 实现 AdminArticle.vue**
**Step 2: 实现 AdminCategory.vue**
**Step 3: 实现 AdminTag.vue**
**Step 4: Commit**

```bash
git add frontend/src/views/admin/
git commit -m "feat: add admin article, category, tag management"
```

---

### Task 17: 后台 — 用户管理 & 评论管理 & 个人中心

**Files:**
- Create: `frontend/src/views/admin/AdminUser.vue`
- Create: `frontend/src/views/admin/AdminComment.vue`
- Create: `frontend/src/views/admin/AdminProfile.vue`

用户管理：el-table 用户列表（用户名、昵称、邮箱、角色、状态、注册时间），启用/禁用按钮。
评论管理：el-table 评论列表（内容、文章标题、评论者、时间），删除按钮。
个人中心：修改昵称、邮箱、密码（旧密码+新密码），头像上传。

**Step 1: 实现 AdminUser.vue**
**Step 2: 实现 AdminComment.vue**
**Step 3: 实现 AdminProfile.vue**
**Step 4: Commit**

```bash
git add frontend/src/views/admin/
git commit -m "feat: add admin user, comment management and profile"
```

---

## Phase 3: 集成与收尾

### Task 18: JWT 角色支持 & 安全配置

**Files:**
- Modify: `backend/src/main/java/com/login/util/JwtUtil.java` — token 中加入 role
- Modify: `backend/src/main/java/com/login/security/JwtAuthenticationFilter.java` — 解析 role 设置权限
- Modify: `backend/src/main/java/com/login/config/SecurityConfig.java` — 配置 admin 路径权限
- Modify: `backend/src/main/java/com/login/service/impl/SysUserServiceImpl.java` — login 返回 role
- Modify: `frontend/src/utils/request.js` — token 过期处理优化

**Step 1: 修改 JwtUtil 支持 role**
**Step 2: 修改 Filter 和 SecurityConfig**
**Step 3: 修改登录接口返回 role**
**Step 4: Commit**

```bash
git add backend/src/main/java/com/login/
git commit -m "feat: add role to JWT and configure admin access control"
```

---

### Task 19: 修改现有登录注册页面

**Files:**
- Modify: `frontend/src/views/Login.vue` — 登录成功后根据 role 跳转（admin→/admin, user→/）
- Modify: `frontend/src/views/Register.vue` — 注册成功后跳转到前台首页
- Modify: `frontend/src/views/Home.vue` — 此文件改为旧版，可删除或重定向

**Step 1: 修改登录跳转逻辑**
**Step 2: 修改注册跳转**
**Step 3: Commit**

```bash
git add frontend/src/views/
git commit -m "feat: update login/register to redirect based on role"
```

---

### Task 20: 前端请求拦截 & 全局状态

**Files:**
- Modify: `frontend/src/utils/request.js` — 完善拦截器
- Create: `frontend/src/utils/auth.js` — token/role 存取工具函数（localStorage）

auth.js 提供：getToken, setToken, removeToken, getRole, setRole, getUserInfo, setUserInfo

router/index.js 的导航守卫使用 auth.js 判断权限。

**Step 1: 创建 auth.js**
**Step 2: 完善 request.js 拦截器**
**Step 3: Commit**

```bash
git add frontend/src/utils/ frontend/src/router/
git commit -m "feat: add auth utils and improve request interceptor"
```

---

### Task 21: 最终测试 & 收尾

**Step 1: 重新执行 init.sql 更新数据库**
**Step 2: 重启后端**
**Step 3: 重启前端**
**Step 4: 测试所有流程**
- 前台浏览、搜索
- 注册新用户、登录、发布文章、评论、点赞
- admin 登录后台、Dashboard、文章管理、用户管理
**Step 5: 最终 Commit**

```bash
git add .
git commit -m "feat: complete blog system with front and admin panels"
```
