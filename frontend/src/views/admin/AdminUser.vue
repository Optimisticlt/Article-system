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
