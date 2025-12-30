import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'teachers',
          name: 'Teachers',
          component: () => import('@/views/teacher/TeacherList.vue')
        },
        {
          path: 'teacher/:id',
          name: 'TeacherDetail',
          component: () => import('@/views/teacher/TeacherDetail.vue')
        },
        {
          path: 'appointments',
          name: 'Appointments',
          component: () => import('@/views/appointment/AppointmentList.vue')
        },
        {
          path: 'appointment/:id',
          name: 'AppointmentDetail',
          component: () => import('@/views/appointment/AppointmentDetail.vue')
        },
        {
          path: 'appointment/create/:teacherId',
          name: 'CreateAppointment',
          component: () => import('@/views/appointment/CreateAppointment.vue')
        },
        {
          path: 'payments',
          name: 'Payments',
          component: () => import('@/views/payment/PaymentList.vue')
        },
        {
          path: 'payment/:id',
          name: 'PaymentDetail',
          component: () => import('@/views/payment/PaymentDetail.vue')
        },
        {
          path: 'chats',
          name: 'Chats',
          component: () => import('@/views/chat/ChatList.vue')
        },
        {
          path: 'chat/:relationshipId',
          name: 'ChatWindow',
          component: () => import('@/views/chat/ChatWindow.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue')
        },
        {
          path: 'announcements',
          name: 'Announcements',
          component: () => import('@/views/AnnouncementList.vue')
        },
        {
          path: 'announcement/:id',
          name: 'AnnouncementDetail',
          component: () => import('@/views/AnnouncementDetail.vue')
        }
      ]
    }
  ]
})

// 开发模式：设置为 true 可以跳过登录验证，方便查看页面效果
const DEV_MODE = true // 开发时设为 true，生产环境请设为 false

// 路由守卫
router.beforeEach((to, from, next) => {
  // 开发模式下跳过路由守卫
  if (DEV_MODE) {
    next()
    return
  }

  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated()

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    next('/teachers')
  } else {
    next()
  }
})

export default router
