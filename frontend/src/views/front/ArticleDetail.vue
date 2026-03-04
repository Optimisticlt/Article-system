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
