<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">Reset Password</h2>
      <p class="auth-subtitle">Enter your email to receive a reset link</p>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleSubmit">
        <el-form-item prop="email"><el-input v-model="form.email" placeholder="Enter your email" :prefix-icon="Message" size="large" /></el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" :disabled="countdown > 0" @click="handleSubmit">
            {{ countdown > 0 ? countdown + 's before resend' : 'Send Reset Email' }}
          </el-button>
        </el-form-item>
        <div class="auth-footer"><router-link to="/login" class="link">Back to login</router-link></div>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { forgotPassword } from '../api/auth'
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
let timer = null
const form = reactive({ email: '' })
const rules = { email: [{ required: true, message: 'Please enter email', trigger: 'blur' }, { type: 'email', message: 'Invalid email format', trigger: 'blur' }] }
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try { await forgotPassword(form.email); ElMessage.success('Reset email sent'); startCountdown() } catch {} finally { loading.value = false }
}
function startCountdown() { countdown.value = 60; timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) }
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>
<style scoped>
.auth-page { display: flex; justify-content: center; padding: 40px 16px; }
.auth-card { width: 100%; max-width: 420px; padding: 40px 36px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); border: 1px solid #ebeef5; }
.auth-title { text-align: center; font-size: 24px; font-weight: 700; color: #1a1a1a; margin-bottom: 6px; }
.auth-subtitle { text-align: center; color: #909399; font-size: 13px; margin-bottom: 28px; }
.link { color: #409eff; text-decoration: none; font-size: 14px; }
.link:hover { color: #337ecc; }
.submit-btn { width: 100%; font-size: 15px; font-weight: 600; }
.auth-footer { text-align: center; color: #606266; font-size: 14px; }
</style>
