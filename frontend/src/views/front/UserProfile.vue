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
