<template>
  <el-container class="admin-layout">
    <!-- Sidebar -->
    <el-aside width="220px" class="admin-sidebar">
      <div class="sidebar-logo">
        <span>后台管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#ccc"
        active-text-color="#fff"
        class="sidebar-menu"
      >
        <el-menu-item index="/admin">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/article">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/category">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tag">
          <el-icon><CollectionTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comment">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-divider />
        <el-menu-item index="/admin/profile">
          <el-icon><Setting /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- Right section -->
    <el-container direction="vertical">
      <!-- Top Header -->
      <el-header class="admin-header" height="56px">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" :src="userInfo?.avatar || ''" />
              <span class="username">{{ userInfo?.nickname || userInfo?.userName || 'Admin' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="front">前台首页</el-dropdown-item>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Odometer, Document, Folder, CollectionTag,
  User, ChatDotRound, Setting, ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userInfo = ref(null)

const activeMenu = computed(() => route.path)

const titleMap = {
  '/admin': '仪表盘',
  '/admin/article': '文章管理',
  '/admin/category': '分类管理',
  '/admin/tag': '标签管理',
  '/admin/user': '用户管理',
  '/admin/comment': '评论管理',
  '/admin/profile': '个人中心'
}
const pageTitle = computed(() => titleMap[route.path] || '后台管理')

onMounted(() => {
  const stored = localStorage.getItem('userInfo')
  if (stored) userInfo.value = JSON.parse(stored)
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    router.push('/login')
  } else if (cmd === 'front') {
    router.push('/')
  } else if (cmd === 'profile') {
    router.push('/admin/profile')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-sidebar {
  background: #001529;
  flex-shrink: 0;
}
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1d2b3a;
}
.sidebar-menu {
  border-right: none;
}
.admin-header {
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.header-left .page-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}
.admin-main {
  background: #f5f7fa;
  min-height: calc(100vh - 56px);
}
</style>
