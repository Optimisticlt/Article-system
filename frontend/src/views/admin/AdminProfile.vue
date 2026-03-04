<template>
  <div class="admin-profile">
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- Basic Info Tab -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="basicForm" label-width="100px" style="max-width: 480px">
            <el-form-item label="用户名">
              <el-input :value="userInfo.userName" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="basicForm.nickname" placeholder="请输入昵称" maxlength="50" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="basicForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="basicSaving" @click="saveBasic">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Password Tab -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwdForm" label-width="110px" style="max-width: 480px">
            <el-form-item label="旧密码" required>
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位密码" />
            </el-form-item>
            <el-form-item label="确认新密码" required>
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdSaving" @click="savePassword">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Avatar Tab -->
        <el-tab-pane label="头像" name="avatar">
          <div class="avatar-section">
            <el-avatar :size="100" :src="avatarPreview || userInfo.avatar || ''" />
            <div class="avatar-upload">
              <el-upload
                :action="null"
                :http-request="handleAvatarUpload"
                :show-file-list="false"
                accept="image/*"
              >
                <el-button type="primary">选择新头像</el-button>
              </el-upload>
              <p class="upload-tip">支持 JPG / PNG，大小不超过 5MB</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateProfile, updatePassword, updateAvatar } from '../../api/user'
import { uploadFile } from '../../api/file'

const activeTab = ref('basic')
const userInfo = ref({})
const basicSaving = ref(false)
const pwdSaving = ref(false)
const avatarPreview = ref('')

const basicForm = reactive({ nickname: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
    basicForm.nickname = userInfo.value.nickname || ''
    basicForm.email = userInfo.value.email || ''
  } catch {
    // fallback to localStorage
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
      basicForm.nickname = userInfo.value.nickname || ''
      basicForm.email = userInfo.value.email || ''
    }
  }
})

async function saveBasic() {
  basicSaving.value = true
  try {
    await updateProfile({ nickname: basicForm.nickname, email: basicForm.email })
    ElMessage.success('保存成功')
    // Update localStorage
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      const info = JSON.parse(stored)
      info.nickname = basicForm.nickname
      info.email = basicForm.email
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    basicSaving.value = false
  }
}

async function savePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次新密码输入不一致')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  pwdSaving.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch {
    ElMessage.error('密码修改失败，请检查旧密码是否正确')
  } finally {
    pwdSaving.value = false
  }
}

async function handleAvatarUpload({ file }) {
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await uploadFile(fd)
    const url = res.data
    await updateAvatar(url)
    avatarPreview.value = url
    ElMessage.success('头像更新成功')
    // Update localStorage
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      const info = JSON.parse(stored)
      info.avatar = url
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  } catch {
    ElMessage.error('头像上传失败')
  }
}
</script>

<style scoped>
.admin-profile { width: 100%; max-width: 700px; }
.avatar-section {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 16px 0;
}
.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.upload-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}
</style>
