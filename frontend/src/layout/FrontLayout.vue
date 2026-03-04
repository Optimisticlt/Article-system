<template>
  <el-container class="front-layout" direction="vertical">
    <!-- Top Navigation -->
    <el-header class="front-header" height="60px">
      <div class="header-inner">
        <div class="header-left">
          <router-link to="/" class="logo">TechBlog</router-link>
          <router-link to="/" class="nav-link">首页</router-link>
        </div>
        <div class="header-center">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文章..."
            :prefix-icon="Search"
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          />
        </div>
        <div class="header-right">
          <template v-if="userInfo">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userInfo.avatar || ''" />
                <span class="username">{{ userInfo.nickname || userInfo.userName }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                  <el-dropdown-item command="publish">发布文章</el-dropdown-item>
                  <el-dropdown-item v-if="userInfo.role === 'admin'" command="admin">后台管理</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- Main Content -->
    <el-main class="front-main">
      <router-view />
    </el-main>

    <!-- Footer -->
    <el-footer class="front-footer" height="60px">
      <span>© 2026 TechBlog. All rights reserved.</span>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const searchKeyword = ref('')
const userInfo = ref(null)

onMounted(() => {
  const stored = localStorage.getItem('userInfo')
  if (stored) userInfo.value = JSON.parse(stored)
})

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value.trim() } })
  }
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push(`/user/${userInfo.value.id}`)
  } else if (cmd === 'publish') {
    router.push('/publish')
  } else if (cmd === 'admin') {
    router.push('/admin')
  }
}
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  background: #f5f7fa;
}
.front-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 24px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
}
.nav-link {
  color: #333;
  text-decoration: none;
  font-size: 14px;
}
.nav-link:hover {
  color: #409eff;
}
.header-center {
  flex: 1;
  max-width: 400px;
}
.search-input {
  width: 100%;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}
.username {
  font-size: 14px;
}
.front-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
  width: 100%;
}
.front-footer {
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 13px;
}
</style>
