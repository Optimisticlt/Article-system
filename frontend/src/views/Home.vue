<template>
  <div class="home-page">
    <div class="home-card">
      <div class="avatar"><el-icon :size="48"><UserFilled /></el-icon></div>
      <h2 class="welcome">Welcome Back</h2>
      <div v-if="userInfo" class="user-info">
        <div class="info-item"><span class="label">Username</span><span class="value">{{ userInfo.userName }}</span></div>
        <div class="info-item"><span class="label">Email</span><span class="value">{{ userInfo.email }}</span></div>
        <div class="info-item"><span class="label">Status</span><el-tag :type="userInfo.status === 1 ? 'success' : 'danger'" size="small">{{ userInfo.status === 1 ? 'Active' : 'Disabled' }}</el-tag></div>
        <div class="info-item"><span class="label">Registered</span><span class="value">{{ userInfo.createTime }}</span></div>
      </div>
      <el-button type="danger" size="large" class="logout-btn" @click="handleLogout">Logout</el-button>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled } from '@element-plus/icons-vue'
import { getUserInfo } from '../api/auth'
const router = useRouter()
const userInfo = ref(null)
onMounted(async () => { try { const res = await getUserInfo(); userInfo.value = res.data } catch { router.push('/login') } })
function handleLogout() { localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); router.push('/login') }
</script>
<style scoped>
.home-page { width: 100%; height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.home-card { width: 440px; padding: 48px 40px; background: rgba(255,255,255,0.15); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border-radius: 20px; border: 1px solid rgba(255,255,255,0.25); box-shadow: 0 25px 50px rgba(0,0,0,0.15); text-align: center; }
.avatar { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, #a855f7, #7c3aed); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; color: #fff; }
.welcome { font-size: 24px; color: #fff; font-weight: 700; margin-bottom: 32px; }
.user-info { text-align: left; margin-bottom: 32px; }
.info-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid rgba(255,255,255,0.15); }
.info-item:last-child { border-bottom: none; }
.label { color: rgba(255,255,255,0.7); font-size: 14px; }
.value { color: #fff; font-size: 14px; font-weight: 500; }
.logout-btn { width: 100%; border-radius: 10px; font-size: 16px; font-weight: 600; letter-spacing: 2px; }
</style>
