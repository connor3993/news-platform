import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: false }
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: () => import('@/views/Category.vue'),
    meta: { title: '分类', requiresAuth: false }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: { title: '搜索', requiresAuth: false }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue'),
    meta: { title: '文章详情', requiresAuth: false }
  },
  {
    path: '/hot',
    name: 'Hot',
    component: () => import('@/views/Hot.vue'),
    meta: { title: '热点', requiresAuth: false }
  },
  {
    path: '/submit',
    name: 'SubmitArticle',
    component: () => import('@/views/SubmitArticle.vue'),
    meta: { title: '发布投稿', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '我的', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

// Router guards
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('user_token')

  // Set page title
  document.title = to.meta.title ? `${to.meta.title} - 新闻资讯平台` : '新闻资讯平台'

  // Redirect to home if already logged in and visiting login
  if (to.name === 'Login' && token) {
    return next('/home')
  }

  // Redirect to login if auth required and not logged in
  if (to.meta.requiresAuth && !token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  next()
})

export default router
