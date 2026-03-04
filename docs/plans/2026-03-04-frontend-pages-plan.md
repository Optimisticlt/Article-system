# 前台 & 后台页面实施计划（Tasks 13-17）

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现博客系统 12 个 Vue 视图页面（5 个前台 + 7 个后台），完成用户端和管理端的全部交互功能。

**Architecture:** 直接实现各视图文件，自包含，每页通过 Composition API 管理本地状态，调用现有 API 层。API 响应结构：interceptor 返回 `res`（即 `response.data`，包含 `code/message/data` 字段），所以业务数据通过 `res.data` 取得；分页数据结构为 `res.data.records[]` + `res.data.total`。

**Tech Stack:** Vue 3 Composition API + Element Plus + md-editor-v3 (`MdEditor`/`MdPreview`) + ECharts + axios（封装在 `src/utils/request.js`）

> **注意：** 本项目前端无测试框架配置，跳过单元测试，每步在浏览器中验证功能正确性，频繁提交。

---

## Task 1：补全 API 层（tag.js + article.js + file.js）

**Files:**
- Create: `frontend/src/api/tag.js`
- Create: `frontend/src/api/file.js`
- Modify: `frontend/src/api/article.js`

**Step 1: 新建 `frontend/src/api/tag.js`**

```js
import request from '../utils/request'

export function listTags() {
  return request.get('/tag/list')
}
```

**Step 2: 新建 `frontend/src/api/file.js`**

```js
import request from '../utils/request'

export function uploadFile(formData) {
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

**Step 3: 在 `frontend/src/api/article.js` 末尾追加两个函数**

```js
export function toggleLike(id) {
  return request.post(`/article/${id}/like`)
}

export function listMyArticlesByUser(params) {
  return request.get('/article/list', { params })
}
```

> `listMyArticlesByUser` 用于 UserProfile 页面按 userId 过滤公开文章。

**Step 4: 验证**

启动前端开发服务器，确认控制台无编译错误。

**Step 5: Commit**

```bash
git add frontend/src/api/tag.js frontend/src/api/file.js frontend/src/api/article.js
git commit -m "feat: add tag api, file upload api, and toggleLike to article api"
```

---

## Task 2：HomeView.vue — 文章列表首页

**Files:**
- Create: `frontend/src/views/front/HomeView.vue`

**Step 1: 确认目录存在**

确认 `frontend/src/views/front/` 目录存在（路由中已引用此路径）。若不存在，创建该目录。

**Step 2: 创建 `HomeView.vue`**

```vue
<template>
  <div class="home-view">
    <!-- Category Tabs -->
    <el-tabs v-model="activeCategory" class="category-tabs" @tab-change="handleCategoryChange">
      <el-tab-pane label="全部" name="0" />
      <el-tab-pane
        v-for="cat in categories"
        :key="cat.id"
        :label="cat.name"
        :name="String(cat.id)"
      />
    </el-tabs>

    <div class="content-layout">
      <!-- Article Area -->
      <div class="article-section">
        <!-- Skeleton Loading -->
        <div v-if="loading" class="article-grid">
          <el-card v-for="i in 6" :key="i" class="article-card skeleton-card">
            <el-skeleton :rows="4" animated />
          </el-card>
        </div>

        <!-- Article Grid -->
        <div v-else-if="articles.length" class="article-grid">
          <el-card
            v-for="article in articles"
            :key="article.id"
            class="article-card"
            :body-style="{ padding: '0' }"
            shadow="hover"
            @click="$router.push(`/article/${article.id}`)"
          >
            <div
              class="card-cover"
              :style="article.coverUrl ? { backgroundImage: `url(${article.coverUrl})` } : {}"
            >
              <div v-if="!article.coverUrl" class="cover-placeholder" />
            </div>
            <div class="card-body">
              <div v-if="article.isTop" class="top-badge">
                <el-tag type="danger" size="small">置顶</el-tag>
              </div>
              <h3 class="card-title">{{ article.title }}</h3>
              <p class="card-summary">{{ article.summary }}</p>
              <div class="card-footer">
                <div class="author-info">
                  <el-avatar :size="20" :src="article.authorAvatar || ''" />
                  <span class="author-name">{{ article.authorName || article.authorNickname }}</span>
                </div>
                <div class="stats">
                  <span class="date">{{ formatDate(article.createTime) }}</span>
                  <span class="views">
                    <el-icon><View /></el-icon>
                    {{ article.viewCount }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- Empty State -->
        <el-empty v-else description="暂无文章" />

        <!-- Pagination -->
        <div v-if="total > 0" class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="loadArticles"
          />
        </div>
      </div>

      <!-- Tag Sidebar -->
      <aside class="tag-sidebar">
        <el-card>
          <template #header><span>热门标签</span></template>
          <div class="tag-cloud">
            <el-tag
              v-for="tag in tags"
              :key="tag.id"
              :type="activeTag === String(tag.id) ? '' : 'info'"
              class="tag-item"
              style="cursor: pointer"
              @click="handleTagClick(tag.id)"
            >
              {{ tag.name }}
            </el-tag>
            <el-tag
              v-if="activeTag"
              type="warning"
              style="cursor: pointer"
              @click="clearTag"
            >
              × 清除筛选
            </el-tag>
          </div>
        </el-card>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listArticles } from '../../api/article'
import { listCategories } from '../../api/category'
import { listTags } from '../../api/tag'

const articles = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const activeCategory = ref('0')
const activeTag = ref('')

onMounted(async () => {
  await Promise.all([loadCategories(), loadTags()])
  loadArticles()
})

async function loadCategories() {
  try {
    const res = await listCategories()
    categories.value = res.data || []
  } catch {}
}

async function loadTags() {
  try {
    const res = await listTags()
    tags.value = res.data || []
  } catch {}
}

async function loadArticles() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (activeCategory.value !== '0') params.categoryId = activeCategory.value
    if (activeTag.value) params.tagId = activeTag.value
    const res = await listArticles(params)
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

function handleCategoryChange() {
  activeTag.value = ''
  currentPage.value = 1
  loadArticles()
}

function handleTagClick(tagId) {
  activeTag.value = activeTag.value === String(tagId) ? '' : String(tagId)
  activeCategory.value = '0'
  currentPage.value = 1
  loadArticles()
}

function clearTag() {
  activeTag.value = ''
  currentPage.value = 1
  loadArticles()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.home-view {
  width: 100%;
}
.category-tabs {
  margin-bottom: 20px;
}
.content-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.article-section {
  flex: 1;
  min-width: 0;
}
.article-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.article-card {
  cursor: pointer;
  transition: transform 0.2s;
  overflow: hidden;
}
.article-card:hover {
  transform: translateY(-2px);
}
.card-cover {
  height: 180px;
  background-size: cover;
  background-position: center;
  background-color: #f0f2f5;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.15;
}
.card-body {
  padding: 16px;
}
.top-badge {
  margin-bottom: 6px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #1a1a1a;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-summary {
  font-size: 13px;
  color: #666;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}
.stats {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #999;
}
.views {
  display: flex;
  align-items: center;
  gap: 3px;
}
.skeleton-card {
  height: 320px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
.tag-sidebar {
  width: 200px;
  flex-shrink: 0;
}
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag-item {
  cursor: pointer;
}
</style>
```

**Step 3: 在浏览器访问 `http://localhost:5173/`，验证：**
- 文章卡片网格正常渲染
- 分类 Tab 切换过滤生效
- 标签云点击过滤生效
- 分页功能正常

**Step 4: Commit**

```bash
git add frontend/src/views/front/HomeView.vue
git commit -m "feat: add HomeView with card grid, category tabs, tag filter, pagination"
```

---

## Task 3：ArticleDetail.vue — 文章详情页

**Files:**
- Create: `frontend/src/views/front/ArticleDetail.vue`

**Step 1: 创建 `ArticleDetail.vue`**

```vue
<template>
  <div class="article-detail">
    <el-skeleton v-if="loading" :rows="10" animated style="max-width: 860px" />

    <div v-else-if="article" class="detail-layout">
      <!-- Main Content -->
      <div class="main-content">
        <!-- Article Header -->
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <el-avatar :size="32" :src="article.authorAvatar || ''" style="cursor:pointer" @click="$router.push(`/user/${article.userId}`)" />
          <span class="author-name" style="cursor:pointer" @click="$router.push(`/user/${article.userId}`)">
            {{ article.authorName || article.authorNickname }}
          </span>
          <el-divider direction="vertical" />
          <span>{{ formatDate(article.createTime) }}</span>
          <el-divider direction="vertical" />
          <el-icon><View /></el-icon>
          <span>{{ article.viewCount }} 阅读</span>
          <el-tag v-if="article.categoryName" size="small" type="info" style="margin-left: 8px">
            {{ article.categoryName }}
          </el-tag>
          <el-tag
            v-for="tag in article.tags"
            :key="tag.id"
            size="small"
            type="success"
            style="margin-left: 4px"
          >
            {{ tag.name }}
          </el-tag>
        </div>

        <!-- Markdown Content -->
        <div class="markdown-body">
          <MdPreview :modelValue="article.content || ''" :id="'preview'" theme="default" />
        </div>

        <!-- Like Button -->
        <div class="like-section">
          <el-button
            :type="isLiked ? 'primary' : 'default'"
            :icon="isLiked ? StarFilled : Star"
            round
            @click="handleLike"
          >
            {{ isLiked ? '已点赞' : '点赞' }} {{ likeCount }}
          </el-button>
        </div>

        <!-- Comment Section -->
        <div class="comment-section">
          <h3 class="comment-title">评论 {{ comments.length }}</h3>

          <!-- Comment Input -->
          <div v-if="currentUser" class="comment-input">
            <el-avatar :size="36" :src="currentUser.avatar || ''" />
            <div class="input-area">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="说点什么..."
                maxlength="500"
                show-word-limit
              />
              <el-button
                type="primary"
                :loading="submitingComment"
                style="margin-top: 8px"
                @click="submitComment"
              >
                发表评论
              </el-button>
            </div>
          </div>
          <el-alert
            v-else
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          >
            <template #default>
              <span>
                <el-link type="primary" @click="$router.push('/login')">登录</el-link>
                后参与评论
              </span>
            </template>
          </el-alert>

          <!-- Comment List -->
          <div v-if="comments.length" class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="36" :src="comment.userAvatar || ''" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.userNickname || comment.userName }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                  <el-button
                    v-if="currentUser && (currentUser.id === comment.userId || currentUser.role === 'admin')"
                    link
                    type="danger"
                    size="small"
                    @click="handleDeleteComment(comment.id)"
                  >
                    删除
                  </el-button>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无评论，来第一个发言吧" :image-size="80" />
        </div>
      </div>

      <!-- Right Sidebar -->
      <aside class="right-sidebar">
        <!-- Author Card -->
        <el-card class="author-card">
          <div class="author-info" style="cursor:pointer" @click="$router.push(`/user/${article.userId}`)">
            <el-avatar :size="56" :src="article.authorAvatar || ''" />
            <div class="author-detail">
              <div class="author-name">{{ article.authorName || article.authorNickname }}</div>
              <div class="author-stats">{{ article.authorArticleCount || '' }} 篇文章</div>
            </div>
          </div>
        </el-card>

        <!-- TOC -->
        <el-card v-if="toc.length" class="toc-card">
          <template #header>目录</template>
          <ul class="toc-list">
            <li
              v-for="item in toc"
              :key="item.anchor"
              :class="['toc-item', `toc-h${item.level}`]"
              @click="scrollToAnchor(item.anchor)"
            >
              {{ item.text }}
            </li>
          </ul>
        </el-card>
      </aside>
    </div>

    <el-empty v-else description="文章不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { View, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { getArticleDetail, toggleLike } from '../../api/article'
import { listComments, addComment, deleteComment } from '../../api/comment'

const route = useRoute()
const router = useRouter()

const article = ref(null)
const comments = ref([])
const loading = ref(false)
const isLiked = ref(false)
const likeCount = ref(0)
const commentContent = ref('')
const submitingComment = ref(false)
const toc = ref([])

const currentUser = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

onMounted(async () => {
  await loadArticle()
  await loadComments()
})

async function loadArticle() {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
    likeCount.value = res.data?.likeCount || 0
    isLiked.value = res.data?.isLiked || false
    parseToc(res.data?.content || '')
  } catch {
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const res = await listComments(route.params.id)
    comments.value = res.data || []
  } catch {}
}

function parseToc(content) {
  const lines = content.split('\n')
  const items = []
  for (const line of lines) {
    const m = line.match(/^(#{2,3})\s+(.+)/)
    if (m) {
      items.push({
        level: m[1].length,
        text: m[2].trim(),
        anchor: m[2].trim().toLowerCase().replace(/\s+/g, '-')
      })
    }
  }
  toc.value = items
}

function scrollToAnchor(anchor) {
  const el = document.getElementById(anchor)
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

async function handleLike() {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await toggleLike(route.params.id)
    if (isLiked.value) {
      isLiked.value = false
      likeCount.value--
    } else {
      isLiked.value = true
      likeCount.value++
    }
  } catch {}
}

async function submitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitingComment.value = true
  try {
    await addComment({ articleId: route.params.id, content: commentContent.value.trim() })
    ElMessage.success('评论成功')
    commentContent.value = ''
    await loadComments()
  } catch {
    ElMessage.error('评论失败')
  } finally {
    submitingComment.value = false
  }
}

async function handleDeleteComment(commentId) {
  try {
    await ElMessageBox.confirm('确定删除该评论？', '提示', { type: 'warning' })
    await deleteComment(commentId)
    ElMessage.success('删除成功')
    await loadComments()
  } catch {}
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.article-detail {
  width: 100%;
}
.detail-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.main-content {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 32px;
}
.article-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #1a1a1a;
  line-height: 1.4;
}
.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #666;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.author-name {
  font-weight: 500;
  color: #333;
}
.markdown-body {
  line-height: 1.8;
  font-size: 15px;
}
.like-section {
  display: flex;
  justify-content: center;
  margin: 32px 0;
  padding: 24px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-section {
  margin-top: 24px;
}
.comment-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px;
  color: #1a1a1a;
}
.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.input-area {
  flex: 1;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.comment-item {
  display: flex;
  gap: 12px;
}
.comment-content {
  flex: 1;
  background: #f8f9fa;
  border-radius: 6px;
  padding: 12px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.comment-author {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}
.comment-time {
  font-size: 12px;
  color: #999;
  flex: 1;
}
.comment-text {
  margin: 0;
  font-size: 14px;
  color: #444;
  line-height: 1.6;
}
.right-sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}
.author-card {
  margin-bottom: 16px;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.author-detail {
  flex: 1;
}
.author-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}
.author-stats {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
.toc-card {
  margin-bottom: 16px;
}
.toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.toc-item {
  padding: 4px 0;
  font-size: 13px;
  cursor: pointer;
  color: #555;
  line-height: 1.5;
  border-left: 2px solid transparent;
  padding-left: 8px;
}
.toc-item:hover {
  color: #409eff;
  border-left-color: #409eff;
}
.toc-h3 {
  padding-left: 20px;
  font-size: 12px;
  color: #888;
}
</style>
```

**Step 2: 在浏览器访问文章详情页，验证：**
- Markdown 内容正常渲染
- 点赞按钮状态切换
- 评论列表展示和发表
- 右侧作者卡片和目录

**Step 3: Commit**

```bash
git add frontend/src/views/front/ArticleDetail.vue
git commit -m "feat: add ArticleDetail with markdown preview, likes, comments, TOC sidebar"
```

---

## Task 4：SearchResult.vue — 搜索结果页

**Files:**
- Create: `frontend/src/views/front/SearchResult.vue`

**Step 1: 创建 `SearchResult.vue`**

```vue
<template>
  <div class="search-result">
    <div class="search-header">
      <h2 class="search-title">
        搜索 "<mark class="keyword-mark">{{ keyword }}</mark>"
        <span v-if="!loading" class="result-count">共 {{ total }} 篇</span>
      </h2>
    </div>

    <el-skeleton v-if="loading" :rows="4" animated />

    <div v-else-if="articles.length" class="result-list">
      <el-card
        v-for="article in articles"
        :key="article.id"
        class="result-card"
        shadow="hover"
        @click="$router.push(`/article/${article.id}`)"
      >
        <div class="result-card-inner">
          <div
            v-if="article.coverUrl"
            class="result-cover"
            :style="{ backgroundImage: `url(${article.coverUrl})` }"
          />
          <div class="result-content">
            <h3 class="result-title" v-html="highlight(article.title)" />
            <p class="result-summary" v-html="highlight(article.summary)" />
            <div class="result-meta">
              <span>{{ article.authorName || article.authorNickname }}</span>
              <el-divider direction="vertical" />
              <span>{{ formatDate(article.createTime) }}</span>
              <el-divider direction="vertical" />
              <span>{{ article.viewCount }} 阅读</span>
            </div>
          </div>
        </div>
      </el-card>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="doSearch"
        />
      </div>
    </div>

    <div v-else>
      <el-empty description="未找到相关文章">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchArticles } from '../../api/article'

const route = useRoute()
const articles = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')

onMounted(() => {
  keyword.value = route.query.keyword || ''
  doSearch()
})

watch(() => route.query.keyword, (val) => {
  keyword.value = val || ''
  currentPage.value = 1
  doSearch()
})

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    const res = await searchArticles({
      keyword: keyword.value,
      page: currentPage.value,
      size: pageSize.value
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

function highlight(text) {
  if (!text || !keyword.value) return text || ''
  const escaped = keyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(escaped, 'gi'), match => `<mark class="highlight">${match}</mark>`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.search-result {
  width: 100%;
  max-width: 860px;
}
.search-header {
  margin-bottom: 24px;
}
.search-title {
  font-size: 20px;
  color: #333;
  font-weight: 500;
}
.keyword-mark {
  background: #ffeaa7;
  padding: 0 2px;
  border-radius: 2px;
}
.result-count {
  font-size: 14px;
  color: #999;
  font-weight: normal;
  margin-left: 8px;
}
.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.result-card {
  cursor: pointer;
  transition: transform 0.15s;
}
.result-card:hover {
  transform: translateX(4px);
}
.result-card-inner {
  display: flex;
  gap: 16px;
}
.result-cover {
  width: 120px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 4px;
  background-size: cover;
  background-position: center;
}
.result-content {
  flex: 1;
  min-width: 0;
}
.result-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #1a1a1a;
}
.result-summary {
  font-size: 13px;
  color: #666;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.result-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
:deep(.highlight) {
  background: #ffeaa7;
  padding: 0 1px;
  border-radius: 2px;
}
</style>
```

**Step 2: 在浏览器搜索关键词，验证高亮效果和分页**

**Step 3: Commit**

```bash
git add frontend/src/views/front/SearchResult.vue
git commit -m "feat: add SearchResult page with keyword highlight and pagination"
```

---

## Task 5：PublishArticle.vue — 发布 & 编辑文章

**Files:**
- Create: `frontend/src/views/front/PublishArticle.vue`

**Step 1: 创建 `PublishArticle.vue`**

```vue
<template>
  <div class="publish-view">
    <!-- Title Input -->
    <el-input
      v-model="form.title"
      placeholder="请输入文章标题..."
      class="title-input"
      size="large"
      maxlength="200"
    />

    <!-- Editor -->
    <div class="editor-wrap">
      <MdEditor
        v-model="form.content"
        :theme="'default'"
        style="height: calc(100vh - 220px)"
        @onUploadImg="handleEditorImageUpload"
      />
    </div>

    <!-- Bottom Toolbar -->
    <div class="bottom-bar">
      <div class="bar-left">
        <el-tag v-if="isEdit" type="info">编辑模式</el-tag>
      </div>
      <div class="bar-right">
        <el-button @click="openSettings">发布设置</el-button>
        <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="publishNow" :loading="publishing">发布</el-button>
      </div>
    </div>

    <!-- Settings Drawer -->
    <el-drawer v-model="drawerVisible" title="发布设置" direction="rtl" size="380px">
      <el-form :model="form" label-position="top">
        <!-- Category -->
        <el-form-item label="文章分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <!-- Tags -->
        <el-form-item label="文章标签">
          <el-select
            v-model="form.tagIds"
            multiple
            placeholder="选择标签（最多5个）"
            style="width: 100%"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>

        <!-- Cover Image -->
        <el-form-item label="封面图">
          <el-upload
            class="cover-uploader"
            :action="null"
            :http-request="handleCoverUpload"
            :show-file-list="false"
            accept="image/*"
          >
            <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview" />
            <div v-else class="upload-placeholder">
              <el-icon :size="32"><Plus /></el-icon>
              <div>上传封面</div>
            </div>
          </el-upload>
        </el-form-item>

        <!-- Summary -->
        <el-form-item label="文章摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="4"
            placeholder="留空则自动截取正文前200字..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { publishArticle, updateArticle, getArticleDetail } from '../../api/article'
import { listCategories } from '../../api/category'
import { listTags } from '../../api/tag'
import { uploadFile } from '../../api/file'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const drawerVisible = ref(false)
const saving = ref(false)
const publishing = ref(false)
const categories = ref([])
const tags = ref([])

const form = reactive({
  id: null,
  title: '',
  content: '',
  summary: '',
  coverUrl: '',
  categoryId: null,
  tagIds: [],
  status: 0
})

onMounted(async () => {
  await Promise.all([loadCategories(), loadTags()])
  if (isEdit.value) {
    await loadArticle()
  }
})

async function loadCategories() {
  try {
    const res = await listCategories()
    categories.value = res.data || []
  } catch {}
}

async function loadTags() {
  try {
    const res = await listTags()
    tags.value = res.data || []
  } catch {}
}

async function loadArticle() {
  try {
    const res = await getArticleDetail(route.params.id)
    const a = res.data
    form.id = a.id
    form.title = a.title
    form.content = a.content
    form.summary = a.summary || ''
    form.coverUrl = a.coverUrl || ''
    form.categoryId = a.categoryId || null
    form.tagIds = (a.tags || []).map(t => t.id)
    form.status = a.status
  } catch {
    ElMessage.error('加载文章失败')
  }
}

function openSettings() {
  drawerVisible.value = true
}

async function handleCoverUpload({ file }) {
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await uploadFile(fd)
    form.coverUrl = res.data
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('封面上传失败')
  }
}

async function handleEditorImageUpload(files, callback) {
  const urls = []
  for (const file of files) {
    try {
      const fd = new FormData()
      fd.append('file', file)
      const res = await uploadFile(fd)
      urls.push(res.data)
    } catch {}
  }
  callback(urls)
}

async function saveDraft() {
  if (!validate()) return
  saving.value = true
  try {
    await submit(0)
    ElMessage.success('草稿已保存')
  } finally {
    saving.value = false
  }
}

async function publishNow() {
  if (!validate()) return
  publishing.value = true
  try {
    await submit(1)
    ElMessage.success(isEdit.value ? '文章已更新' : '发布成功')
    router.push('/')
  } finally {
    publishing.value = false
  }
}

function validate() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return false
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return false
  }
  return true
}

async function submit(status) {
  const data = {
    ...form,
    status,
    summary: form.summary || form.content.replace(/[#*`>\-!\[\]()]/g, '').slice(0, 200)
  }
  if (isEdit.value) {
    await updateArticle(data)
  } else {
    await publishArticle(data)
  }
}
</script>

<style scoped>
.publish-view {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.title-input :deep(.el-input__inner) {
  font-size: 20px;
  font-weight: 600;
  border: none;
  border-bottom: 2px solid #f0f0f0;
  border-radius: 0;
  padding-left: 0;
}
.title-input :deep(.el-input__inner:focus) {
  border-bottom-color: #409eff;
}
.editor-wrap {
  flex: 1;
}
.bottom-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}
.bar-right {
  display: flex;
  gap: 8px;
}
.cover-uploader {
  width: 100%;
}
.cover-preview {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 4px;
}
.upload-placeholder {
  width: 100%;
  height: 160px;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  gap: 8px;
}
.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
```

**Step 2: 验证发布和编辑功能（需登录后测试）**

**Step 3: Commit**

```bash
git add frontend/src/views/front/PublishArticle.vue
git commit -m "feat: add PublishArticle with MdEditor, cover upload, category/tag selection"
```

---

## Task 6：UserProfile.vue — 用户个人主页

**Files:**
- Create: `frontend/src/views/front/UserProfile.vue`

**Step 1: 创建 `UserProfile.vue`**

```vue
<template>
  <div class="user-profile">
    <!-- Profile Banner -->
    <el-card class="profile-banner">
      <el-skeleton v-if="loading" :rows="2" animated />
      <div v-else-if="profile" class="banner-content">
        <el-avatar :size="80" :src="profile.avatar || ''" />
        <div class="profile-info">
          <h2 class="nickname">{{ profile.nickname || profile.userName }}</h2>
          <div class="profile-stats">
            <span><strong>{{ profile.articleCount || 0 }}</strong> 篇文章</span>
            <el-divider direction="vertical" />
            <span><strong>{{ profile.likeCount || 0 }}</strong> 获赞</span>
            <el-divider direction="vertical" />
            <span>{{ formatDate(profile.createTime) }} 加入</span>
          </div>
        </div>
        <div class="profile-actions">
          <el-button
            v-if="isSelf"
            @click="$router.push('/admin/profile')"
          >
            编辑资料
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Article List -->
    <div class="profile-articles">
      <h3 class="section-title">文章列表</h3>

      <el-skeleton v-if="articlesLoading" :rows="4" animated />

      <div v-else-if="articles.length" class="article-list">
        <el-card
          v-for="article in articles"
          :key="article.id"
          class="article-item"
          shadow="hover"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="article-item-inner">
            <div class="article-item-content">
              <h4 class="article-item-title">{{ article.title }}</h4>
              <p class="article-item-summary">{{ article.summary }}</p>
              <div class="article-item-meta">
                <span>{{ formatDate(article.createTime) }}</span>
                <el-divider direction="vertical" />
                <span>{{ article.viewCount }} 阅读</span>
                <el-tag v-if="article.categoryName" size="small" type="info" style="margin-left: 8px">
                  {{ article.categoryName }}
                </el-tag>
              </div>
            </div>
            <div
              v-if="article.coverUrl"
              class="article-item-cover"
              :style="{ backgroundImage: `url(${article.coverUrl})` }"
            />
          </div>
        </el-card>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="loadArticles"
          />
        </div>
      </div>

      <el-empty v-else description="该用户暂无文章" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile } from '../../api/user'
import { listArticles } from '../../api/article'

const route = useRoute()
const router = useRouter()

const profile = ref(null)
const articles = ref([])
const loading = ref(false)
const articlesLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const currentUser = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : null
})

const isSelf = computed(() => {
  return currentUser.value && String(currentUser.value.id) === String(route.params.id)
})

onMounted(async () => {
  await loadProfile()
  await loadArticles()
})

watch(() => route.params.id, async () => {
  currentPage.value = 1
  await loadProfile()
  await loadArticles()
})

async function loadProfile() {
  loading.value = true
  try {
    const res = await getProfile(route.params.id)
    profile.value = res.data
  } catch {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

async function loadArticles() {
  articlesLoading.value = true
  try {
    const res = await listArticles({
      page: currentPage.value,
      size: pageSize.value,
      userId: route.params.id
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {} finally {
    articlesLoading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.user-profile {
  width: 100%;
  max-width: 860px;
}
.profile-banner {
  margin-bottom: 24px;
}
.banner-content {
  display: flex;
  align-items: center;
  gap: 24px;
}
.profile-info {
  flex: 1;
}
.nickname {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #1a1a1a;
}
.profile-stats {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px;
}
.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.article-item {
  cursor: pointer;
  transition: transform 0.15s;
}
.article-item:hover {
  transform: translateX(4px);
}
.article-item-inner {
  display: flex;
  gap: 16px;
  align-items: center;
}
.article-item-content {
  flex: 1;
  min-width: 0;
}
.article-item-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.article-item-summary {
  font-size: 13px;
  color: #666;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.article-item-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
}
.article-item-cover {
  width: 100px;
  height: 68px;
  flex-shrink: 0;
  border-radius: 4px;
  background-size: cover;
  background-position: center;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
```

**Step 2: 验证个人主页展示和当前用户判断**

**Step 3: Commit**

```bash
git add frontend/src/views/front/UserProfile.vue
git commit -m "feat: add UserProfile page with banner stats and article list"
```

---

## Task 7：Dashboard.vue — 管理后台仪表盘

**Files:**
- Create: `frontend/src/views/admin/Dashboard.vue`

**Step 1: 确认目录存在**

确认 `frontend/src/views/admin/` 目录存在。若不存在，创建该目录。

**Step 2: 创建 `Dashboard.vue`**

```vue
<template>
  <div class="dashboard">
    <!-- Stat Cards -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card stat-blue">
          <el-statistic title="文章总数" :value="stats.totalArticles || 0">
            <template #prefix><el-icon><Document /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-green">
          <el-statistic title="已发布" :value="stats.publishedCount || 0">
            <template #prefix><el-icon><CircleCheck /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-orange">
          <el-statistic title="评论总数" :value="stats.commentCount || 0">
            <template #prefix><el-icon><ChatDotRound /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-purple">
          <el-statistic title="总浏览量" :value="stats.totalViews || 0">
            <template #prefix><el-icon><View /></el-icon></template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>分类文章数量</template>
          <div ref="categoryChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>近7天发布趋势</template>
          <div ref="trendChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Recent Articles -->
    <el-card class="recent-card">
      <template #header>最近文章</template>
      <el-table :data="stats.recentArticles || []" stripe>
        <el-table-column prop="title" label="标题">
          <template #default="{ row }">
            <el-link type="primary" :href="`/article/${row.id}`" target="_blank">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Document, CircleCheck, ChatDotRound, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboard } from '../../api/admin'

const stats = ref({})
const categoryChartRef = ref(null)
const trendChartRef = ref(null)
let categoryChart = null
let trendChart = null

onMounted(async () => {
  await loadDashboard()
})

async function loadDashboard() {
  try {
    const res = await getDashboard()
    stats.value = res.data || {}
    await nextTick()
    renderCharts()
  } catch {
    ElMessage.error('加载统计数据失败')
  }
}

function renderCharts() {
  renderCategoryChart()
  renderTrendChart()
}

function renderCategoryChart() {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  const data = stats.value.categoryChart || []
  categoryChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: data.map(d => d.count),
      itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }
    }],
    grid: { left: 40, right: 20, top: 20, bottom: 60 }
  })
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const data = stats.value.trend || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      boundaryGap: false
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line',
      data: data.map(d => d.count),
      smooth: true,
      areaStyle: { color: 'rgba(64,158,255,0.1)' },
      itemStyle: { color: '#409eff' }
    }],
    grid: { left: 40, right: 20, top: 20, bottom: 30 }
  })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.dashboard {
  width: 100%;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: '';
  position: absolute;
  right: -10px;
  top: -10px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  opacity: 0.1;
}
.stat-blue::after { background: #409eff; }
.stat-green::after { background: #67c23a; }
.stat-orange::after { background: #e6a23c; }
.stat-purple::after { background: #9c27b0; }
.chart-row {
  margin-bottom: 16px;
}
.chart-container {
  height: 280px;
  width: 100%;
}
.recent-card {
  margin-bottom: 16px;
}
</style>
```

**Step 3: 访问 `/admin`，验证统计卡片和图表渲染**

**Step 4: Commit**

```bash
git add frontend/src/views/admin/Dashboard.vue
git commit -m "feat: add admin Dashboard with stat cards and ECharts bar/line charts"
```

---

## Task 8：AdminArticle.vue — 文章管理

**Files:**
- Create: `frontend/src/views/admin/AdminArticle.vue`

**Step 1: 创建 `AdminArticle.vue`**

```vue
<template>
  <div class="admin-article">
    <!-- Search Bar -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="搜索文章标题..." clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="articles"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" :href="`/article/${row.id}`" target="_blank">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isTop === 1"
              @change="handleToggleTop(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="110">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '审核通过' }}
            </el-button>
            <el-popconfirm
              title="确定删除该文章？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="loadArticles"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminListArticles,
  adminUpdateArticleStatus,
  adminToggleTop,
  adminDeleteArticle
} from '../../api/admin'

const articles = ref([])
const loading = ref(false)
const keyword = ref('')
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

onMounted(() => loadArticles())

async function loadArticles() {
  loading.value = true
  try {
    const res = await adminListArticles({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadArticles()
}

function handleReset() {
  keyword.value = ''
  currentPage.value = 1
  loadArticles()
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await adminUpdateArticleStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已审核发布' : '已下架')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleToggleTop(row) {
  try {
    await adminToggleTop(row.id)
    row.isTop = row.isTop === 1 ? 0 : 1
    ElMessage.success(row.isTop === 1 ? '已置顶' : '已取消置顶')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id) {
  try {
    await adminDeleteArticle(id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch {
    ElMessage.error('删除失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.admin-article {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card :deep(.el-form) {
  margin-bottom: 0;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
```

**Step 2: 访问 `/admin/article`，验证表格、搜索、状态切换、置顶、删除**

**Step 3: Commit**

```bash
git add frontend/src/views/admin/AdminArticle.vue
git commit -m "feat: add AdminArticle management with search, status toggle, top pin, delete"
```

---

## Task 9：AdminCategory.vue — 分类管理

**Files:**
- Create: `frontend/src/views/admin/AdminCategory.vue`

**Step 1: 创建 `AdminCategory.vue`**

```vue
<template>
  <div class="admin-category">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类列表</span>
          <el-button type="primary" @click="openDialog(null)">+ 新增分类</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="categories" stripe>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该分类？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="400px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminListCategories,
  adminAddCategory,
  adminUpdateCategory,
  adminDeleteCategory
} from '../../api/admin'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const form = reactive({ name: '', sort: 0, status: 1 })

onMounted(() => loadCategories())

async function loadCategories() {
  loading.value = true
  try {
    const res = await adminListCategories()
    categories.value = res.data || []
  } catch {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.name = row.name
    form.sort = row.sort
    form.status = row.status
  } else {
    isEdit.value = false
    editId.value = null
    form.name = ''
    form.sort = 0
    form.status = 1
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await adminUpdateCategory(editId.value, form)
    } else {
      await adminAddCategory(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    loadCategories()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await adminDeleteCategory(id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch {
    ElMessage.error('删除失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.admin-category { width: 100%; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

**Step 2: 访问 `/admin/category`，验证新增、编辑、删除**

**Step 3: Commit**

```bash
git add frontend/src/views/admin/AdminCategory.vue
git commit -m "feat: add AdminCategory with CRUD dialog"
```

---

## Task 10：AdminTag.vue — 标签管理

**Files:**
- Create: `frontend/src/views/admin/AdminTag.vue`

**Step 1: 创建 `AdminTag.vue`**

```vue
<template>
  <div class="admin-tag">
    <el-card>
      <template #header>标签管理</template>

      <!-- Add Tag -->
      <div class="add-row">
        <el-input
          v-model="newTagName"
          placeholder="输入标签名称..."
          style="width: 220px"
          maxlength="30"
          @keyup.enter="handleAdd"
        />
        <el-button type="primary" :loading="adding" @click="handleAdd">添加</el-button>
      </div>

      <!-- Tag Cloud -->
      <div v-if="tags.length" class="tag-cloud">
        <el-popconfirm
          v-for="tag in tags"
          :key="tag.id"
          :title="`确定删除标签「${tag.name}」？`"
          @confirm="handleDelete(tag.id)"
        >
          <template #reference>
            <el-tag
              class="tag-item"
              closable
              @close.stop="() => {}"
            >
              {{ tag.name }}
            </el-tag>
          </template>
        </el-popconfirm>
      </div>
      <el-empty v-else description="暂无标签" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminListTags, adminAddTag, adminDeleteTag } from '../../api/admin'

const tags = ref([])
const newTagName = ref('')
const adding = ref(false)
const loading = ref(false)

onMounted(() => loadTags())

async function loadTags() {
  loading.value = true
  try {
    const res = await adminListTags()
    tags.value = res.data || []
  } catch {
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  const name = newTagName.value.trim()
  if (!name) {
    ElMessage.warning('请输入标签名称')
    return
  }
  adding.value = true
  try {
    await adminAddTag(name)
    ElMessage.success('添加成功')
    newTagName.value = ''
    loadTags()
  } catch {
    ElMessage.error('添加失败（标签名可能已存在）')
  } finally {
    adding.value = false
  }
}

async function handleDelete(id) {
  try {
    await adminDeleteTag(id)
    ElMessage.success('删除成功')
    loadTags()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.admin-tag { width: 100%; }
.add-row {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tag-item {
  cursor: default;
  font-size: 13px;
}
</style>
```

**Step 2: 访问 `/admin/tag`，验证添加和删除**

**Step 3: Commit**

```bash
git add frontend/src/views/admin/AdminTag.vue
git commit -m "feat: add AdminTag with inline add and tag cloud delete"
```

---

## Task 11：AdminUser.vue — 用户管理

**Files:**
- Create: `frontend/src/views/admin/AdminUser.vue`

**Step 1: 创建 `AdminUser.vue`**

```vue
<template>
  <div class="admin-user">
    <el-card>
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="role" label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="110">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              :disabled="row.role === 'admin'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="loadUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminListUsers, adminUpdateUserStatus } from '../../api/admin'

const users = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminListUsers({ page: currentPage.value, size: pageSize.value })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await adminUpdateUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch {
    ElMessage.error('操作失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.admin-user { width: 100%; }
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
```

**Step 2: 访问 `/admin/user`，验证用户列表和状态切换（admin 行不可操作）**

**Step 3: Commit**

```bash
git add frontend/src/views/admin/AdminUser.vue
git commit -m "feat: add AdminUser with list and enable/disable status"
```

---

## Task 12：AdminComment.vue — 评论管理

**Files:**
- Create: `frontend/src/views/admin/AdminComment.vue`

**Step 1: 创建 `AdminComment.vue`**

```vue
<template>
  <div class="admin-comment">
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="搜索评论内容..." clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table v-loading="loading" :data="comments" stripe>
        <el-table-column prop="content" label="评论内容" min-width="200">
          <template #default="{ row }">
            <span>{{ truncate(row.content, 80) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="articleTitle" label="所属文章" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" :href="`/article/${row.articleId}`" target="_blank">
              {{ truncate(row.articleTitle, 30) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="userNickname" label="评论者" width="120">
          <template #default="{ row }">{{ row.userNickname || row.userName }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="110">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该评论？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="loadComments"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminListComments, adminDeleteComment } from '../../api/admin'

const comments = ref([])
const loading = ref(false)
const keyword = ref('')
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

onMounted(() => loadComments())

async function loadComments() {
  loading.value = true
  try {
    const res = await adminListComments({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined
    })
    comments.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadComments()
}

function handleReset() {
  keyword.value = ''
  currentPage.value = 1
  loadComments()
}

async function handleDelete(id) {
  try {
    await adminDeleteComment(id)
    ElMessage.success('删除成功')
    loadComments()
  } catch {
    ElMessage.error('删除失败')
  }
}

function truncate(str, len) {
  if (!str) return ''
  return str.length > len ? str.slice(0, len) + '...' : str
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return String(dateStr).slice(0, 10)
}
</script>

<style scoped>
.admin-comment {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
```

**Step 2: 访问 `/admin/comment`，验证列表和删除功能**

**Step 3: Commit**

```bash
git add frontend/src/views/admin/AdminComment.vue
git commit -m "feat: add AdminComment management with search and delete"
```

---

## Task 13：AdminProfile.vue — 个人中心

**Files:**
- Create: `frontend/src/views/admin/AdminProfile.vue`

**Step 1: 创建 `AdminProfile.vue`**

```vue
<template>
  <div class="admin-profile">
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- Basic Info Tab -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="basicForm" label-width="100px" style="max-width: 480px">
            <el-form-item label="用户名">
              <el-input :value="userInfo.userName" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="basicForm.nickname" placeholder="请输入昵称" maxlength="50" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="basicForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="basicSaving" @click="saveBasic">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Password Tab -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwdForm" label-width="110px" style="max-width: 480px">
            <el-form-item label="旧密码" required>
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位密码" />
            </el-form-item>
            <el-form-item label="确认新密码" required>
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdSaving" @click="savePassword">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Avatar Tab -->
        <el-tab-pane label="头像" name="avatar">
          <div class="avatar-section">
            <el-avatar :size="100" :src="avatarPreview || userInfo.avatar || ''" />
            <div class="avatar-upload">
              <el-upload
                :action="null"
                :http-request="handleAvatarUpload"
                :show-file-list="false"
                accept="image/*"
              >
                <el-button type="primary">选择新头像</el-button>
              </el-upload>
              <p class="upload-tip">支持 JPG / PNG，大小不超过 5MB</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateProfile, updatePassword, updateAvatar } from '../../api/user'
import { uploadFile } from '../../api/file'

const activeTab = ref('basic')
const userInfo = ref({})
const basicSaving = ref(false)
const pwdSaving = ref(false)
const avatarPreview = ref('')

const basicForm = reactive({ nickname: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
    basicForm.nickname = userInfo.value.nickname || ''
    basicForm.email = userInfo.value.email || ''
  } catch {
    // fallback to localStorage
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
      basicForm.nickname = userInfo.value.nickname || ''
      basicForm.email = userInfo.value.email || ''
    }
  }
})

async function saveBasic() {
  basicSaving.value = true
  try {
    await updateProfile({ nickname: basicForm.nickname, email: basicForm.email })
    ElMessage.success('保存成功')
    // Update localStorage
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      const info = JSON.parse(stored)
      info.nickname = basicForm.nickname
      info.email = basicForm.email
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    basicSaving.value = false
  }
}

async function savePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次新密码输入不一致')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  pwdSaving.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch {
    ElMessage.error('密码修改失败，请检查旧密码是否正确')
  } finally {
    pwdSaving.value = false
  }
}

async function handleAvatarUpload({ file }) {
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await uploadFile(fd)
    const url = res.data
    await updateAvatar(url)
    avatarPreview.value = url
    ElMessage.success('头像更新成功')
    // Update localStorage
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      const info = JSON.parse(stored)
      info.avatar = url
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  } catch {
    ElMessage.error('头像上传失败')
  }
}
</script>

<style scoped>
.admin-profile { width: 100%; max-width: 700px; }
.avatar-section {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px 0;
}
.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.upload-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}
</style>
```

**Step 2: 访问 `/admin/profile`，验证三个 Tab 的保存功能**

**Step 3: Commit**

```bash
git add frontend/src/views/admin/AdminProfile.vue
git commit -m "feat: add AdminProfile with basic info, password change, avatar upload tabs"
```

---

## Task 14：最终验证

**Step 1: 确认前端目录结构完整**

运行：`dir frontend\src\views\front` 和 `dir frontend\src\views\admin`

期望输出包含：`HomeView.vue`, `ArticleDetail.vue`, `SearchResult.vue`, `PublishArticle.vue`, `UserProfile.vue` 和 `Dashboard.vue`, `AdminArticle.vue`, `AdminCategory.vue`, `AdminTag.vue`, `AdminUser.vue`, `AdminComment.vue`, `AdminProfile.vue`

**Step 2: 启动前端开发服务器**

```bash
cd frontend && npm run dev
```

期望：无编译错误。

**Step 3: 端到端功能验证清单**

- [ ] 访问 `/` — 文章卡片网格正常展示，分类 Tab 切换，标签过滤，分页
- [ ] 访问 `/article/{id}` — Markdown 渲染，点赞，评论
- [ ] 访问 `/search?keyword=Vue` — 高亮结果展示
- [ ] 登录后访问 `/publish` — 编辑器正常，发布成功跳转
- [ ] 访问 `/user/{id}` — 个人主页展示
- [ ] 以 admin 登录访问 `/admin` — Dashboard 图表正常
- [ ] `/admin/article` — 文章表格，状态切换，删除
- [ ] `/admin/category` — 新增分类，编辑，删除
- [ ] `/admin/tag` — 添加标签，删除
- [ ] `/admin/user` — 用户列表，禁用/启用
- [ ] `/admin/comment` — 评论列表，删除
- [ ] `/admin/profile` — 修改昵称邮箱，修改密码，头像上传

**Step 4: 最终 Commit**

```bash
git add .
git commit -m "feat: complete all 12 frontend views for Tasks 13-17"
```
