import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'article/list',
        name: 'ArticleList',
        component: () => import('@/views/ArticleList.vue'),
        meta: { title: '文章列表' }
      },
      {
        path: 'article/add',
        name: 'ArticleAdd',
        component: () => import('@/views/ArticleEdit.vue'),
        meta: { title: '新建文章' }
      },
      {
        path: 'article/edit/:id',
        name: 'ArticleEdit',
        component: () => import('@/views/ArticleEdit.vue'),
        meta: { title: '编辑文章' }
      },
      {
        path: 'article/audit',
        name: 'ArticleAudit',
        component: () => import('@/views/ArticleAudit.vue'),
        meta: { title: '文章审核' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/Category.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('@/views/OperationLog.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()

  if (to.path === '/login') {
    if (adminStore.isLogin) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!adminStore.isLogin) {
    next('/login')
    return
  }

  next()
})

export default router
