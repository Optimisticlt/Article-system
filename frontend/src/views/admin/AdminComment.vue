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
