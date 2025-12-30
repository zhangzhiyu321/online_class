<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <div class="logo">
            <el-icon><School /></el-icon>
            <span class="logo-text">线上家教</span>
          </div>
        </div>
        <div class="header-right">
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            class="header-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="teachers">
              <el-icon><User /></el-icon>
              <span>找教师</span>
            </el-menu-item>
            <el-menu-item index="appointments">
              <el-icon><Calendar /></el-icon>
              <span>我的预约</span>
            </el-menu-item>
            <el-menu-item index="payments">
              <el-icon><Money /></el-icon>
              <span>支付记录</span>
            </el-menu-item>
            <el-menu-item index="chats">
              <el-icon><ChatDotRound /></el-icon>
              <span>消息</span>
              <el-badge
                v-if="unreadCount > 0"
                :value="unreadCount"
                class="badge"
              />
            </el-menu-item>
          </el-menu>
          <el-dropdown 
            @command="handleCommand"
            :placement="'bottom-end'"
            :popper-options="{ strategy: 'fixed' }"
          >
            <div class="user-info">
              <el-avatar :src="userStore.userInfo?.avatar" :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 移动端底部导航 -->
    <div 
      class="mobile-bottom-nav"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
    >
      <div
        v-for="item in mobileNavItems"
        :key="item.index"
        class="nav-item"
        :class="{ active: activeMenu === item.index }"
        :data-index="item.index"
        @click="handleMobileNav(item.index)"
      >
        <el-icon :size="24">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.label }}</span>
        <el-badge
          v-if="item.index === 'chats' && unreadCount > 0"
          :value="unreadCount"
          class="mobile-badge"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  School,
  User,
  Calendar,
  Money,
  ChatDotRound,
  ArrowDown,
  SwitchButton,
  HomeFilled
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const unreadCount = ref(0)

// 加载未读消息数
const loadUnreadCount = async () => {
  try {
    const { getUnreadCount } = await import('@/api/chat')
    const data = await getUnreadCount()
    unreadCount.value = data.count || data.total || 0
  } catch (error) {
    console.error('加载未读消息数失败:', error)
    unreadCount.value = 0
  }
}

// 路由映射表
const routeMap = {
  '/home': 'home',
  '/teachers': 'teachers',
  '/appointment': 'appointments',
  '/payment': 'payments',
  '/chat': 'chats'
}

const activeMenu = computed(() => {
  const path = route.path
  for (const [routePath, menuIndex] of Object.entries(routeMap)) {
    if (path === routePath || path.startsWith(routePath)) {
      return menuIndex
    }
  }
  return 'teachers'
})

const mobileNavItems = [
  { index: 'home', label: '首页', icon: HomeFilled },
  { index: 'teachers', label: '找教师', icon: User },
  { index: 'appointments', label: '预约', icon: Calendar },
  { index: 'payments', label: '支付', icon: Money },
  { index: 'chats', label: '消息', icon: ChatDotRound }
]

// 菜单索引到路由的映射
const menuRouteMap = {
  home: '/home',
  teachers: '/teachers',
  appointments: '/appointments',
  payments: '/payments',
  chats: '/chats'
}

const handleMenuSelect = (index) => {
  const routePath = menuRouteMap[index]
  if (routePath) {
    router.push(routePath)
  }
}

const handleMobileNav = (index) => {
  handleMenuSelect(index)
}

// 触摸滑动交互
const touchState = reactive({
  startX: 0,
  startY: 0,
  isDragging: false,
  targetIndex: null
})

const getNavItemFromPoint = (x, y) => {
  const target = document.elementFromPoint(x, y)
  return target?.closest('.nav-item')
}

const handleTouchStart = (e) => {
  const touch = e.touches[0]
  touchState.startX = touch.clientX
  touchState.startY = touch.clientY
  touchState.isDragging = false
  
  const navItem = getNavItemFromPoint(touch.clientX, touch.clientY)
  if (navItem) {
    touchState.targetIndex = navItem.dataset.index
  }
}

const handleTouchMove = (e) => {
  if (!touchState.targetIndex) return
  
  const touch = e.touches[0]
  const deltaX = Math.abs(touch.clientX - touchState.startX)
  const deltaY = Math.abs(touch.clientY - touchState.startY)
  
  // 如果移动超过10px，认为是滑动
  if (deltaX > 10 || deltaY > 10) {
    touchState.isDragging = true
    const navItem = getNavItemFromPoint(touch.clientX, touch.clientY)
    if (navItem?.dataset.index) {
      touchState.targetIndex = navItem.dataset.index
    }
  }
}

const handleTouchEnd = () => {
  if (touchState.targetIndex && touchState.isDragging) {
    handleMenuSelect(touchState.targetIndex)
  }
  // 重置状态
  Object.assign(touchState, {
    startX: 0,
    startY: 0,
    isDragging: false,
    targetIndex: null
  })
}

const handleCommand = (command) => {
  const commandMap = {
    profile: () => router.push('/profile'),
    logout: () => {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        router.push('/login')
      }).catch(() => {
        // 用户取消，不做任何操作
      })
    }
  }
  
  const handler = commandMap[command]
  if (handler) {
    handler()
  } else {
    console.warn(`未知的命令: ${command}`)
  }
}

onMounted(() => {
  if (!userStore.userInfo) {
    userStore.loadUserInfo()
  }
  // 加载未读消息数
  loadUnreadCount()
  // 定期刷新未读消息数（每30秒）
  const unreadTimer = setInterval(() => {
    if (userStore.isAuthenticated()) {
      loadUnreadCount()
    }
  }, 30000)
  
  // 清理定时器
  return () => {
    clearInterval(unreadTimer)
  }
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid #e5e7eb;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              background 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  transform: translateZ(0);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  padding: 8px 12px;
  border-radius: 12px;
  background: #f9fafb;
}

.logo .el-icon {
  color: #6b7280;
}

.logo-text {
  display: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-menu {
  border-bottom: none;
  background: transparent;
}

.header-menu .el-menu-item {
  border-radius: 12px;
  margin: 0 4px;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              background 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              color 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f9fafb;
  will-change: transform;
  transform: translateZ(0);
}

.header-menu .el-menu-item:hover {
  background: #f3f4f6;
  color: #1a1a1a;
  transform: translateY(-1px) translateZ(0);
}

.header-menu .el-menu-item.is-active {
  background: #f3f4f6;
  color: #1a1a1a;
  border-bottom: none;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 16px;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              background 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              border-color 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  will-change: transform;
  transform: translateZ(0);
}

.user-info:hover {
  background-color: #f3f4f6;
  border-color: #d1d5db;
  transform: translateY(-1px) translateZ(0);
}

.username {
  display: none;
}

.badge {
  margin-left: 4px;
}

.main-content {
  flex: 1;
  padding: 32px 24px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  background: transparent;
  min-height: calc(100vh - 64px);
  will-change: transform;
  transform: translateZ(0);
}

/* 移动端底部导航 */
.mobile-bottom-nav {
  display: flex;
  justify-content: space-around;
  align-items: center;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.1);
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom));
  z-index: 1000;
  border-top: 1px solid #e5e7eb;
  will-change: transform;
  transform: translateZ(0);
  touch-action: pan-y;
  user-select: none;
  -webkit-user-select: none;
  -webkit-touch-callout: none;
  -webkit-tap-highlight-color: transparent;
  -webkit-overflow-scrolling: touch;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  color: #6b7280;
  font-size: 12px;
  position: relative;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              background 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              color 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  margin: 0 4px;
  background: #f9fafb;
  will-change: transform;
  transform: translateZ(0);
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  contain: layout style paint;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #1a1a1a;
  transform: translateY(-2px) translateZ(0);
}

.nav-item.active {
  color: #1a1a1a;
  background: #f3f4f6;
  font-weight: 600;
}

.nav-item:active {
  transform: scale(0.95) translateZ(0);
}

.nav-item span {
  font-size: 12px;
}

.mobile-badge {
  position: absolute;
  top: 0;
  right: 8px;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .logo-text {
    display: inline;
  }

  .username {
    display: inline;
  }

  .mobile-bottom-nav {
    display: none;
  }

  .main-content {
    padding-bottom: 20px;
  }
}

@media (max-width: 767px) {
  .header-menu {
    display: none;
  }

  .main-content {
    padding: 16px;
    padding-bottom: 70px; /* 为底部导航留出空间 */
  }
}
</style>

