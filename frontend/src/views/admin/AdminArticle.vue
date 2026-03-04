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
