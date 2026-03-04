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
