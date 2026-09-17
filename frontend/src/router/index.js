import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Dashboard from '../components/Dashboard.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import Product from '../views/admin/Product.vue'
import LoanApplication from '../views/admin/loan/LoanApplication.vue'
import LoanDetail from '../views/admin/loan/LoanDetail.vue'
import { checkAuth } from '../api/admin'
import { clearAuth, getToken } from '../api/client'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'admin',
          component: Dashboard
        },
        {
          path: 'products',
          name: 'admin-products',
          component: Product
        },
        {
          path: 'loan-applications',
          name: 'admin-loan-applications',
          component: LoanApplication
        },
        {
          path: 'loan-applications/:id',
          name: 'admin-loan-detail',
          component: LoanDetail
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: { name: 'admin' }
    }
  ]
})

router.beforeEach(async (to) => {
  if (!to.matched.some(record => record.meta.requiresAuth)) {
    return true
  }

  if (!getToken()) {
    return { name: 'login' }
  }

  try {
    const response = await checkAuth()
    if (response.success) {
      return true
    }
  } catch (_) {
    // 统一按无效登录状态处理
  }

  clearAuth()
  return { name: 'login' }
})

export default router
