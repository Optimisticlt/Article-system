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
