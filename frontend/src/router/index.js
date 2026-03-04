import { createRouter, createWebHistory } from 'vue-router'
import FrontLayout from '../layout/FrontLayout.vue'
import AdminLayout from '../layout/AdminLayout.vue'

const routes = [
  // Front-end pages (FrontLayout)
  {
    path: '/',
    component: FrontLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/front/HomeView.vue')
      },
      {
        path: 'login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { guest: true }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { guest: true }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('../views/ForgotPassword.vue'),
        meta: { guest: true }
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('../views/front/ArticleDetail.vue')
      },
      {
        path: 'publish',
        name: 'PublishArticle',
        component: () => import('../views/front/PublishArticle.vue'),
        meta: { requireAuth: true }
      },
      {
        path: 'edit/:id',
        name: 'EditArticle',
        component: () => import('../views/front/PublishArticle.vue'),
        meta: { requireAuth: true }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('../views/front/UserProfile.vue')
      },
      {
        path: 'search',
        name: 'SearchResult',
        component: () => import('../views/front/SearchResult.vue')
      }
    ]
  },

  // Admin pages (AdminLayout)
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'article',
        name: 'AdminArticle',
        component: () => import('../views/admin/AdminArticle.vue')
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: () => import('../views/admin/AdminCategory.vue')
      },
      {
        path: 'tag',
        name: 'AdminTag',
        component: () => import('../views/admin/AdminTag.vue')
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('../views/admin/AdminUser.vue')
      },
      {
        path: 'comment',
        name: 'AdminComment',
        component: () => import('../views/admin/AdminComment.vue')
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/admin/AdminProfile.vue')
      }
    ]
  },

  // Catch-all redirect
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  const role = localStorage.getItem('userRole')

  // Route requires authentication
  if (to.meta.requireAuth && !token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // Route requires admin role
  if (to.meta.requireAdmin && role !== 'admin') {
    return next('/')
  }

  // Guest-only routes: redirect logged-in users away
  if (to.meta.guest && token) {
    return next(role === 'admin' ? '/admin' : '/')
  }

  next()
})

export default router
