# 前台 & 后台页面设计文档（Tasks 13-17）

**日期：** 2026-03-04
**范围：** Tasks 13-14（前台页面）+ Tasks 15-17（后台页面）
**方案：** 方案 A — 逐页直接实现，以现有计划为基线，优化 UI/UX

---

## 架构概述

- **前端框架：** Vue 3 Composition API + Element Plus + md-editor-v3 + ECharts
- **布局：** 双布局系统（FrontLayout / AdminLayout），路由已配置完毕
- **API 层：** 所有接口封装已完成（article.js / admin.js / user.js / comment.js / category.js）
- **缺失文件：** `src/api/tag.js`（需新建）
- **状态管理：** localStorage（accessToken、refreshToken、userInfo、userRole）

---

## 前台页面设计（Tasks 13-14）

### 1. HomeView.vue — `/`

**布局：**
- 顶部：分类 Tab（全部 + 各分类），`el-tabs` 实现，点击更新 `categoryId` 查询参数
- 右侧：标签云（`el-tag` 列表），点击更新 `tagId` 查询参数
- 主体：2 列卡片网格（`display: grid; grid-template-columns: repeat(2, 1fr)`）
- 底部：`el-pagination` 分页

**文章卡片内容：**
- 封面图（`cover_url`，无图则显示渐变色占位块）
- 标题（最多 2 行截断）
- 摘要（最多 3 行截断）
- 作者头像 + 昵称
- 发布时间（格式：`YYYY-MM-DD`）
- 浏览量图标 + 数字

**加载态：** `el-skeleton` 骨架屏（6 个卡片占位）
**空状态：** `el-empty` + 文字"暂无文章"

**API 调用：**
- `listArticles({ page, size, categoryId, tagId })` — 文章列表
- `listCategories()` — 分类 Tab 数据（来自 `category.js`）
- `listTags()` — 标签云数据（来自 `tag.js`）

---

### 2. ArticleDetail.vue — `/article/:id`

**布局：** 左右两栏（左 70% 正文 + 右 30% 侧边栏）

**左侧正文区：**
- 文章标题（`h1`）
- 元信息：作者头像+昵称（可跳转个人主页）、发布时间、浏览量
- `MdPreview` 组件渲染 Markdown 内容
- 点赞按钮：`el-button`（Heart 图标），已登录可点，切换选中状态，显示点赞数
- 评论区：
  - 评论列表（用户头像、昵称、时间、内容）
  - 已登录：`el-input` 输入框 + 发表按钮
  - 未登录：提示"登录后参与评论"，点击跳转 `/login`

**右侧侧边栏（`position: sticky; top: 80px`）：**
- 作者卡片：头像、昵称、文章数
- 目录 TOC：解析正文 `h2/h3` 标题，点击平滑滚动到对应位置

**API 调用：**
- `getArticleDetail(id)` — 文章详情 + 浏览量+1
- `listComments(articleId)` — 评论列表（`comment.js`）
- `addComment(data)` — 发表评论
- `toggleLike(id)` — 点赞/取消（需补充到 `article.js`）
- `checkLiked(id)` — 查询是否已点赞（需补充到 `article.js`）

---

### 3. SearchResult.vue — `/search?keyword=`

**布局：**
- 顶部：显示搜索词 + 结果总数（如"搜索 "Vue" 共 12 篇"）
- 列表：与 HomeView 卡片同款，关键词用 `<mark>` 标签高亮
- 无结果：`el-empty` + "返回首页"按钮

**API 调用：**
- `searchArticles({ keyword, page, size })`
- 监听 `route.query.keyword` 变化触发重新搜索

---

### 4. PublishArticle.vue — `/publish` & `/edit/:id`

**布局：**
- 顶部：标题输入框（`el-input`，大号字体）
- 主体：`MdEditor` 编辑器（高度 `calc(100vh - 200px)`）
- 右侧设置抽屉（`el-drawer`，点击"发布设置"按开）：
  - 分类下拉（`el-select`）
  - 标签多选（`el-select multiple`）
  - 封面图上传（`el-upload` 拖拽模式，调用 `/api/file/upload`）
  - 摘要 textarea（`el-input type=textarea`）
- 底部操作栏：保存草稿（status=0）+ 发布（status=1）

**编辑模式：**
- `route.params.id` 存在时为编辑模式
- `onMounted` 调用 `getArticleDetail(id)` 回填所有字段
- 提交时调用 `updateArticle(data)`

**API 调用：**
- `publishArticle(data)` / `updateArticle(data)`
- `listCategories()` — 分类选项
- `listTags()` — 标签选项
- `uploadFile(formData)` — 封面上传（`file.js` 或直接 `user.js`）

---

### 5. UserProfile.vue — `/user/:id`

**布局：**
- 顶部 Banner：大头像（`el-avatar size=80`）、昵称、文章数、获赞总数、注册时间
- 若当前用户查看自己：显示"编辑资料"按钮（跳转 `/admin/profile`）
- 主体：该用户已发布的文章列表（复用 HomeView 卡片样式）

**API 调用：**
- `getUserProfile(userId)` — 用户公开信息（`user.js`）
- `listMyArticles({ page, size })` / `listArticles({ userId })` — 用户文章列表

---

## 后台页面设计（Tasks 15-17）

### 6. Dashboard.vue — `/admin`

**布局：** 响应式网格

**顶部统计卡片（4 个，`el-card` + `el-statistic`）：**
- 文章总数（Book 图标，蓝色）
- 已发布数（Check 图标，绿色）
- 评论总数（Chat 图标，橙色）
- 总浏览量（View 图标，紫色）

**中部图表（两列）：**
- 左：ECharts 柱状图 — 各分类文章数量（`categoryChart` 数组）
- 右：ECharts 折线图 — 最近 7 天发布趋势（`trend` 数组）

**底部：**
- 最近 5 篇文章简表（`el-table`，标题+状态+时间）

**API 调用：**
- `getDashboard()` — 返回 `DashboardVO`（totalArticles, publishedCount, commentCount, totalViews, categoryChart[], trend[], recentArticles[]）

---

### 7. AdminArticle.vue — `/admin/article`

**布局：** 搜索栏 + 表格 + 分页

**搜索栏：** 关键词输入 + 查询按钮 + 重置按钮

**表格列：**
- 标题（可点击跳转文章详情）
- 作者
- 分类
- 状态（`el-tag`：草稿=info，已发布=success）
- 置顶（`el-switch`）
- 浏览量
- 创建时间
- 操作：审核/下架切换 + 删除（`el-popconfirm` 二次确认）

**API 调用：**
- `adminListArticles({ page, size, keyword })`
- `adminUpdateArticleStatus(id, status)`
- `adminToggleTop(id)`
- `adminDeleteArticle(id)`

---

### 8. AdminCategory.vue — `/admin/category`

**布局：** 右上角新增按钮 + 表格 + `el-dialog` 表单

**表格列：** 分类名、排序值、状态（`el-tag`）、创建时间、操作（编辑/删除）

**Dialog 表单：** 分类名（必填）+ 排序值（数字输入）+ 状态（`el-switch`）
- 新增/编辑复用同一 dialog，通过 `isEdit` 标志区分

**API 调用：**
- `adminListCategories()` / `adminAddCategory(data)` / `adminUpdateCategory(id, data)` / `adminDeleteCategory(id)`

---

### 9. AdminTag.vue — `/admin/tag`

**布局：** 顶部快速添加 + 标签云

**顶部：** `el-input`（标签名）+ 添加按钮（行内，回车也可触发）

**主体：** `el-tag`（可关闭）展示所有标签，点击 × 触发 `el-popconfirm` 二次确认删除

**API 调用：**
- `adminListTags()` / `adminAddTag(name)` / `adminDeleteTag(id)`

---

### 10. AdminUser.vue — `/admin/user`

**表格列：** 用户名、昵称、邮箱、角色（`el-tag`：admin=danger，user=info）、状态（启用=success/禁用=warning）、注册时间、操作

**操作：** 启用/禁用按钮（当前用户自身禁用此按钮）

**API 调用：**
- `adminListUsers({ page, size })` / `adminUpdateUserStatus(id, status)`

---

### 11. AdminComment.vue — `/admin/comment`

**表格列：** 评论内容（截断 50 字）、所属文章（可点击跳转）、评论者、评论时间、操作（删除 + 二次确认）

**支持：** 关键词搜索 + 分页

**API 调用：**
- `adminListComments({ page, size, keyword })` / `adminDeleteComment(id)`

---

### 12. AdminProfile.vue — `/admin/profile`

**布局：** `el-tabs` 三个 Tab

- **基本信息 Tab：** 昵称输入 + 邮箱输入 + 保存按钮
- **修改密码 Tab：** 旧密码 + 新密码 + 确认新密码 + 保存按钮（前端校验新密码一致性）
- **头像 Tab：** 当前头像大图预览 + `el-upload` 拖拽上传 + 保存按钮

**API 调用：**
- `getUserInfo()` — 回填当前用户信息
- `updateProfile(data)` — 更新昵称/邮箱
- `updatePassword(data)` — 修改密码
- `updateAvatar(url)` — 更新头像
- `uploadFile(formData)` — 头像文件上传

---

## 需新建/补充的文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/api/tag.js` | 新建 | 前台标签列表：`listTags()` |
| `src/api/article.js` | 补充 | 添加 `toggleLike(id)`、`checkLiked(id)` |
| `src/api/comment.js` | 确认 | 确认已有 `listComments`、`addComment` |
| `src/api/user.js` | 确认 | 确认已有 `getUserProfile`、`updateProfile`、`updatePassword`、`updateAvatar` |

---

## 错误处理原则

- 所有 API 调用使用 `try/catch`，失败时 `ElMessage.error(错误信息)`
- 删除操作统一用 `el-popconfirm` 二次确认
- 表单提交时禁用提交按钮防止重复点击（`loading` 状态）
- 未登录访问需认证路由 → 自动跳转 `/login?redirect=当前路径`

---

## 不在本次范围内

- 评论回复（树形嵌套）— 后端支持 `parent_id`，但 UI 上简化为平铺列表
- 文章收藏功能 — 数据库无此表
- 富文本编辑器 — 统一使用 Markdown
- 响应式移动端适配 — 桌面端优先
