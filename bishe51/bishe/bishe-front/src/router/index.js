import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'frontHome',
        component: () => import('@/views/Home.vue'),
        meta: {
          title: '互动咨询管理',
          subtitle: '便捷、高效的街道就业一站式服务平台'
        }
      },
      {
        path: 'ai-service',
        name: 'frontAiService',
        component: () => import('@/views/AiService.vue'),
        meta: {
          title: 'AI 智能客服',
          subtitle: '结合订单、商品与咨询数据，提供 7 x 24 小时即时答疑'
        }
      },
      {
        path: 'products',
        name: 'frontProducts',
        component: () => import('@/views/Products.vue'),
        meta: {
          title: '农产品浏览',
          subtitle: '按分类筛选、查看详情并快速加入购物车'
        }
      },
      {
        path: 'products/:id',
        name: 'frontProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: {}
      },
      {
        path: 'cart',
        name: 'frontCart',
        component: () => import('@/views/Cart.vue'),
        meta: {
          title: '购物车管理',
          subtitle: '支持数量调整、一键勾选与失效商品清理'
        }
      },
      {
        path: 'orders',
        name: 'frontOrders',
        component: () => import('@/views/Orders.vue'),
        meta: {
          title: '订单跟踪',
          subtitle: '实时查看支付、发货与物流流转状态'
        }
      },
      {
        path: 'reviews',
        name: 'frontReviews',
        component: () => import('@/views/Reviews.vue'),
        meta: {
          title: '评价互动',
          subtitle: '确认收货后可进行星级与文字评价'
        }
      },
      {
        path: 'profile',
        name: 'frontProfile',
        component: () => import('@/views/Profile.vue'),
        meta: {
          title: '个人信息',
          subtitle: '支持头像上传与资料维护'
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const whiteList = ['/login', '/register']
  if (!userStore.token && !whiteList.includes(to.path)) {
    return '/login'
  }
  if (userStore.token && !userStore.userInfo.userId) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      userStore.logout()
      return '/login'
    }
  }
  if (to.path === '/login' && userStore.token) {
    return '/'
  }
  return true
})

export default router
