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
