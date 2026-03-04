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
