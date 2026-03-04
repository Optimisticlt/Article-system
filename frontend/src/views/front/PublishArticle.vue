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
