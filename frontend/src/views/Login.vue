<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">Welcome Back</h2>
      <p class="auth-subtitle">Please enter your credentials</p>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="userName">
          <el-input v-model="form.userName" placeholder="Enter username" :prefix-icon="User" size="large" />
        </el-form-item>

        <el-form-item prop="passWord">
          <el-input v-model="form.passWord" type="password" placeholder="Enter password" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="Enter captcha" :prefix-icon="Key" size="large" class="captcha-input" />
            <img :src="captchaImage" class="captcha-img" @click="loadCaptcha" title="Click to refresh" />
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="form.rememberMe">Remember me</el-checkbox>
            <router-link to="/forgot-password" class="link">Forgot password?</router-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">Login</el-button>
        </el-form-item>

        <div class="auth-footer">
          No account yet? <router-link to="/register" class="link">Register now</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login, getCaptcha } from '../api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const form = reactive({ userName: '', passWord: '', captcha: '', captchaKey: '', rememberMe: false })
const rules = {
  userName: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  passWord: [{ required: true, message: 'Please enter password', trigger: 'blur' }],
  captcha: [{ required: true, message: 'Please enter captcha', trigger: 'blur' }]
}

async function loadCaptcha() {
  try { const res = await getCaptcha(); captchaImage.value = res.data.image; form.captchaKey = res.data.key } catch {}
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login(form)
    localStorage.setItem('accessToken', res.data.accessToken)
    if (res.data.refreshToken) localStorage.setItem('refreshToken', res.data.refreshToken)
    if (res.data.role) localStorage.setItem('userRole', res.data.role)
    ElMessage.success('Login successful')
    router.push(res.data.role === 'admin' ? '/admin' : '/home')
  } catch { loadCaptcha() } finally { loading.value = false }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 40px 16px; }
.auth-card { width: 100%; max-width: 420px; padding: 40px 36px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); border: 1px solid #ebeef5; }
.auth-title { text-align: center; font-size: 24px; font-weight: 700; color: #1a1a1a; margin-bottom: 6px; }
.auth-subtitle { text-align: center; color: #909399; font-size: 13px; margin-bottom: 28px; }
.captcha-row { display: flex; gap: 12px; width: 100%; }
.captcha-input { flex: 1; }
.captcha-img { height: 40px; border-radius: 6px; cursor: pointer; border: 1px solid #dcdfe6; transition: opacity 0.2s; }
.captcha-img:hover { opacity: 0.85; }
.form-options { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.link { color: #409eff; text-decoration: none; font-size: 14px; }
.link:hover { color: #337ecc; }
.submit-btn { width: 100%; font-size: 15px; font-weight: 600; letter-spacing: 2px; }
.auth-footer { text-align: center; color: #606266; font-size: 14px; }
</style>
