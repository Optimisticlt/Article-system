<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">Create Account</h2>
      <p class="auth-subtitle">Fill in the form to register</p>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister">
        <el-form-item prop="userName"><el-input v-model="form.userName" placeholder="Username (3-20 chars)" :prefix-icon="User" size="large" /></el-form-item>
        <el-form-item prop="email"><el-input v-model="form.email" placeholder="Enter email" :prefix-icon="Message" size="large" /></el-form-item>
        <el-form-item prop="passWord"><el-input v-model="form.passWord" type="password" placeholder="Password (6-30 chars)" :prefix-icon="Lock" size="large" show-password /></el-form-item>
        <el-form-item prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" placeholder="Confirm password" :prefix-icon="Lock" size="large" show-password /></el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="Enter captcha" :prefix-icon="Key" size="large" class="captcha-input" />
            <img :src="captchaImage" class="captcha-img" @click="loadCaptcha" title="Click to refresh" />
          </div>
        </el-form-item>
        <el-form-item><el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">Register</el-button></el-form-item>
        <div class="auth-footer">Already have an account? <router-link to="/login" class="link">Back to login</router-link></div>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Key, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '../api/auth'
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const form = reactive({ userName: '', email: '', passWord: '', confirmPassword: '', captcha: '', captchaKey: '' })
const validateConfirm = (rule, value, callback) => { if (value !== form.passWord) { callback(new Error('Passwords do not match')) } else { callback() } }
const rules = {
  userName: [{ required: true, message: 'Please enter username', trigger: 'blur' }, { min: 3, max: 20, message: '3-20 characters', trigger: 'blur' }],
  email: [{ required: true, message: 'Please enter email', trigger: 'blur' }, { type: 'email', message: 'Invalid email format', trigger: 'blur' }],
  passWord: [{ required: true, message: 'Please enter password', trigger: 'blur' }, { min: 6, max: 30, message: '6-30 characters', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: 'Please confirm password', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
  captcha: [{ required: true, message: 'Please enter captcha', trigger: 'blur' }]
}
async function loadCaptcha() { try { const res = await getCaptcha(); captchaImage.value = res.data.image; form.captchaKey = res.data.key } catch {} }
async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try { await register(form); ElMessage.success('Registration successful'); router.push('/login') } catch { loadCaptcha() } finally { loading.value = false }
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
.link { color: #409eff; text-decoration: none; font-size: 14px; }
.link:hover { color: #337ecc; }
.submit-btn { width: 100%; font-size: 15px; font-weight: 600; letter-spacing: 2px; }
.auth-footer { text-align: center; color: #606266; font-size: 14px; }
</style>
