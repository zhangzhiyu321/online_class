<!--
  主布局组件 (MainLayout)
  功能：
  1. 顶部导航栏（首页按钮、Logo、菜单项、用户下拉菜单）
  2. 主内容区域（路由视图）
  3. 首页侧边栏（抽屉式）
  4. 移动端底部导航栏
  5. 未读消息和通知数量显示
  6. 用户下拉菜单（个人中心、退出登录）
-->
<template>
  <div class="main-layout">
    <!-- 顶部导航栏（固定定位，始终显示在页面顶部） -->
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <div class="home-button" @click="handleHomeClick">
            <el-icon :size="24"><HomeFilled /></el-icon>
            <span class="home-text">首页</span>
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
            <el-menu-item index="notifications">
              <el-icon><Bell /></el-icon>
              <span>通知</span>
              <el-badge
                v-if="notificationUnreadCount > 0"
                :value="notificationUnreadCount"
                class="badge"
              />
            </el-menu-item>
          </el-menu>
          <el-dropdown 
            @command="handleCommand"
            @visible-change="handleDropdownVisibleChange"
            trigger="click"
            :placement="'bottom-end'"
            :popper-options="{ strategy: 'fixed' }"
            popper-class="user-dropdown-popper"
          >
            <div class="user-info" :class="{ active: isUserDropdownOpen }">
              <el-icon :size="24" class="user-icon"><UserFilled /></el-icon>
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
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
    <el-main class="main-content" :class="{ 'drawer-open': showHomeDrawer }">
      <router-view />
    </el-main>

    <!-- 首页侧边栏 -->
    <transition name="drawer-fade">
      <div v-if="showHomeDrawer" class="drawer-backdrop" @click="closeHomeDrawer"></div>
    </transition>
    <transition name="drawer-slide">
      <div v-if="showHomeDrawer" class="home-drawer">
        <div class="drawer-header">
          <h2 class="drawer-title">首页</h2>
          <el-icon class="drawer-close" @click="closeHomeDrawer"><Close /></el-icon>
        </div>
        <div class="drawer-content">
          <HomeView />
        </div>
      </div>
    </transition>

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
        <el-icon :size="24" class="nav-icon">
          <component :is="activeMenu === item.index ? item.icon : item.iconOutline" />
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
/**
 * 主布局组件 - Script Setup
 * 使用 Composition API 实现组件逻辑
 */

import { ref, computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  User,
  Calendar,
  Money,
  ChatDotRound,
  ArrowDown,
  SwitchButton,
  HomeFilled,
  Bell,
  UserFilled,
  Close
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import HomeView from '@/views/HomeView.vue'

// ========== 路由和状态管理 ==========
const router = useRouter() // Vue Router 实例，用于页面跳转
const route = useRoute() // 当前路由对象，用于获取当前路径
const userStore = useUserStore() // 用户状态管理 Store

// ========== 响应式数据定义 ==========

// 未读消息数量
const unreadCount = ref(0)

// 未读通知数量
const notificationUnreadCount = ref(0)

// 首页侧边栏显示状态（true: 显示, false: 隐藏）
const showHomeDrawer = ref(false)

// 用户下拉菜单打开状态（用于控制激活样式）
const isUserDropdownOpen = ref(false)

// ========== 数据加载方法 ==========

/**
 * 加载未读消息数量
 * 从API获取当前用户的未读消息数，用于在导航栏显示徽章
 */
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

/**
 * 加载未读通知数量
 * 从API获取当前用户的未读通知数，用于在导航栏显示徽章
 */
const loadNotificationUnreadCount = async () => {
  try {
    const { getUnreadCount } = await import('@/api/notification')
    const data = await getUnreadCount()
    notificationUnreadCount.value = data.count || 0
  } catch (error) {
    console.error('加载未读通知数失败:', error)
    notificationUnreadCount.value = 0
  }
}

// ========== 路由和菜单配置 ==========

/**
 * 路由路径到菜单索引的映射表
 * 用于根据当前路由路径确定哪个菜单项应该高亮显示
 */
const routeMap = {
  '/home': 'home',
  '/teachers': 'teachers',
  '/appointment': 'appointments',
  '/payment': 'payments',
  '/refund': 'refunds',
  '/notification': 'notifications',
  '/chat': 'chats'
}

/**
 * 当前激活的菜单项（计算属性）
 * 根据当前路由路径自动计算应该高亮的菜单项
 * 如果路径匹配不到任何映射，默认返回 'teachers'
 */
const activeMenu = computed(() => {
  const path = route.path
  for (const [routePath, menuIndex] of Object.entries(routeMap)) {
    if (path === routePath || path.startsWith(routePath)) {
      return menuIndex
    }
  }
  return 'teachers'
})

/**
 * 移动端底部导航栏配置
 * 包含导航项的信息：索引、标签文本、图标组件
 * icon: 选中状态时使用的图标（填充版本或CSS填充）
 * iconOutline: 未选中状态时使用的图标（线条版本）
 */
const mobileNavItems = [
  { 
    index: 'teachers', 
    label: '找教师', 
    icon: UserFilled,  // 填充版本（选中时）
    iconOutline: User  // 线条版本（未选中时）
  },
  { 
    index: 'appointments', 
    label: '预约', 
    icon: Calendar,  // 选中时通过CSS填充
    iconOutline: Calendar  // 线条版本（未选中时）
  },
  { 
    index: 'payments', 
    label: '支付', 
    icon: Money,  // 选中时通过CSS填充
    iconOutline: Money  // 线条版本（未选中时）
  },
  { 
    index: 'chats', 
    label: '消息', 
    icon: ChatDotRound,  // 选中时通过CSS填充
    iconOutline: ChatDotRound  // 线条版本（未选中时）
  }
]

/**
 * 菜单索引到路由路径的映射表
 * 用于根据菜单项索引跳转到对应的路由路径
 */
const menuRouteMap = {
  'notifications': '/notifications',
  'refunds': '/refunds',
  home: '/home',
  teachers: '/teachers',
  appointments: '/appointments',
  payments: '/payments',
  chats: '/chats'
}

// ========== 事件处理方法 ==========

/**
 * 处理首页按钮点击事件
 * 打开首页侧边栏（抽屉式）
 */
const handleHomeClick = () => {
  showHomeDrawer.value = true
}

/**
 * 关闭首页侧边栏
 */
const closeHomeDrawer = () => {
  showHomeDrawer.value = false
}

/**
 * 处理菜单项选择事件
 * @param {string} index - 菜单项索引（如 'home', 'teachers' 等）
 * 如果点击的是首页，打开侧边栏；否则跳转到对应路由
 */
const handleMenuSelect = (index) => {
  // 如果点击的是首页，打开侧边栏而不是跳转路由
  if (index === 'home') {
    handleHomeClick()
    return
  }
  const routePath = menuRouteMap[index]
  if (routePath) {
    router.push(routePath)
  }
}

/**
 * 处理移动端导航点击事件
 * @param {string} index - 导航项索引
 * 移动端底部导航的点击事件处理，复用桌面端菜单选择逻辑
 */
const handleMobileNav = (index) => {
  handleMenuSelect(index)
}

// ========== 移动端触摸交互处理 ==========

/**
 * 触摸状态对象（响应式）
 * 用于跟踪移动端底部导航的触摸滑动交互状态
 */
const touchState = reactive({
  startX: 0,        // 触摸开始时的X坐标
  startY: 0,        // 触摸开始时的Y坐标
  isDragging: false, // 是否正在拖拽
  targetIndex: null  // 当前目标导航项的索引
})

/**
 * 根据坐标点获取对应的导航项元素
 * @param {number} x - X坐标
 * @param {number} y - Y坐标
 * @returns {HTMLElement|null} 导航项元素或null
 */
const getNavItemFromPoint = (x, y) => {
  const target = document.elementFromPoint(x, y)
  return target?.closest('.nav-item')
}

/**
 * 处理触摸开始事件
 * 记录触摸起始位置和目标导航项
 * @param {TouchEvent} e - 触摸事件对象
 */
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

/**
 * 处理触摸移动事件
 * 判断是否正在拖拽，并更新目标导航项
 * @param {TouchEvent} e - 触摸事件对象
 */
const handleTouchMove = (e) => {
  if (!touchState.targetIndex) return
  
  const touch = e.touches[0]
  const deltaX = Math.abs(touch.clientX - touchState.startX)
  const deltaY = Math.abs(touch.clientY - touchState.startY)
  
  // 如果移动超过10px，认为是滑动（拖拽）
  if (deltaX > 10 || deltaY > 10) {
    touchState.isDragging = true
    const navItem = getNavItemFromPoint(touch.clientX, touch.clientY)
    if (navItem?.dataset.index) {
      touchState.targetIndex = navItem.dataset.index
    }
  }
}

/**
 * 处理触摸结束事件
 * 如果是在拖拽状态下结束，则触发对应导航项的点击事件
 */
const handleTouchEnd = () => {
  if (touchState.targetIndex && touchState.isDragging) {
    handleMenuSelect(touchState.targetIndex)
  }
  // 重置触摸状态
  Object.assign(touchState, {
    startX: 0,
    startY: 0,
    isDragging: false,
    targetIndex: null
  })
}

/**
 * 处理用户下拉菜单显示/隐藏状态变化
 * @param {boolean} visible - 下拉菜单是否可见
 */
const handleDropdownVisibleChange = (visible) => {
  isUserDropdownOpen.value = visible
}

/**
 * 处理用户下拉菜单命令选择
 * @param {string} command - 命令标识（'profile' 或 'logout'）
 */
const handleCommand = (command) => {
  isUserDropdownOpen.value = false
  const commandMap = {
    // 跳转到个人中心页面
    profile: () => router.push('/profile'),
    // 退出登录（需要确认）
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

// ========== 监听器和生命周期 ==========

/**
 * 监听首页侧边栏状态变化，控制body滚动
 * 当侧边栏打开时，禁止body滚动（防止背景页面滚动）
 * 当侧边栏关闭时，恢复body滚动
 */
watch(showHomeDrawer, (isOpen) => {
  if (isOpen) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

/**
 * 组件挂载时的初始化操作
 * 1. 加载用户信息（如果未加载）
 * 2. 加载未读消息数和通知数
 * 3. 设置定时器，每30秒刷新一次未读数量
 */
onMounted(() => {
  // 如果用户信息未加载，则加载用户信息
  if (!userStore.userInfo) {
    userStore.loadUserInfo()
  }
  // 立即加载未读消息数和通知数
  loadUnreadCount()
  loadNotificationUnreadCount()
  // 设置定时器，每30秒刷新一次未读消息数和通知数（仅在用户已登录时）
  const unreadTimer = setInterval(() => {
    if (userStore.isAuthenticated()) {
      loadUnreadCount()
      loadNotificationUnreadCount()
    }
  }, 30000)
  
  // 返回清理函数（在组件卸载时调用，清理定时器）
  return () => {
    clearInterval(unreadTimer)
  }
})

/**
 * 组件卸载时的清理操作
 * 确保组件卸载时恢复body滚动（防止侧边栏关闭后body仍无法滚动）
 */
onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style scoped>
/* ========== 主布局容器样式 ========== */

/* 主布局容器（垂直布局，占满视口高度） */
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ========== 顶部导航栏样式 ========== */

/* 顶部导航栏头部（固定定位，始终显示在页面顶部） */
.header {
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 64px;
  position: sticky; /* 粘性定位，滚动时保持在顶部 */
  top: 0;
  z-index: 1000; /* 确保在其他内容之上 */
  border-bottom: 1px solid #e5e7eb;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              background 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform; /* 优化性能 */
  transform: translateZ(0); /* GPU加速 */
}

.header-content {
  max-width: var(--container-max-width-desktop, 1200px);
  margin: 0 auto;
  padding: 0 var(--spacing-lg, 20px);
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

/* home-button 特定样式（覆盖通用样式） */
.home-button {
  font-size: var(--font-size-body, 16px);
  font-weight: 500;
  border-radius: 16px;
  height: var(--button-height, 44px);
  padding: 6px 12px;
}

.home-button:hover {
  color: #409eff;
}

.home-text {
  display: none;
  font-size: var(--font-size-body-small, 14px);
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
  font-size: var(--font-size-body-small, 14px);
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

/* user-info 特定样式（覆盖通用样式） */
.user-info {
  gap: 10px;
  border-radius: 16px;
  height: 44px;
  padding: 6px 12px;
}

.username {
  display: none;
}

.badge {
  margin-left: 4px;
}

.main-content {
  flex: 1;
  padding: var(--spacing-2xl, 32px) var(--spacing-lg, 24px);
  max-width: var(--container-max-width-desktop, 1200px);
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  background: transparent;
  min-height: calc(100vh - 64px);
  will-change: transform;
  transform: translateZ(0);
}

/* ========== 移动端底部导航样式 ========== */

/* 移动端底部导航栏（固定定位在页面底部） */
.mobile-bottom-nav {
  display: flex;
  justify-content: space-around; /* 导航项均匀分布 */
  align-items: center;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom)); /* 适配iPhone等设备的底部安全区域 */
  z-index: 1000;
  will-change: transform;
  transform: translateZ(0); /* GPU加速 */
  touch-action: pan-y; /* 允许垂直滚动，优化触摸交互 */
  user-select: none; /* 禁止文本选择 */
  -webkit-user-select: none;
  -webkit-touch-callout: none; /* 禁止长按弹出菜单（iOS） */
  -webkit-tap-highlight-color: transparent; /* 移除点击高亮 */
  -webkit-overflow-scrolling: touch; /* iOS平滑滚动 */
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  cursor: pointer;
  color: #000000;
  font-size: 12px;
  position: relative;
  transition: color 0.2s ease;
  will-change: transform;
  transform: translateZ(0);
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  contain: layout style paint;
  flex: 1;
}

.nav-item .nav-icon {
  transition: color 0.2s ease, transform 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55), opacity 0.2s ease;
  color: #000000;
  opacity: 0.5;
}

/* 未选中状态：图标较淡，看起来像线条 */
.nav-item .nav-icon :deep(svg) {
  transition: fill 0.2s ease, stroke 0.2s ease, stroke-width 0.2s ease, opacity 0.2s ease;
}

/* 选中状态：蓝色填充，图标跳动 */
.nav-item.active .nav-icon {
  color: #409eff;
  opacity: 1;
  animation: iconBounce 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 选中状态：强制所有SVG元素使用蓝色填充 */
.nav-item.active .nav-icon :deep(svg) {
  color: #409eff !important;
  fill: #409eff !important;
}

/* 选中状态：处理所有SVG子元素，确保蓝色填充 */
/* 首先设置所有元素的基础样式 */
.nav-item.active .nav-icon :deep(svg *) {
  fill: #409eff !important;
  stroke: #409eff !important;
}

/* 对于所有路径和形状元素，优先使用 fill 填充 */
.nav-item.active .nav-icon :deep(svg circle),
.nav-item.active .nav-icon :deep(svg rect),
.nav-item.active .nav-icon :deep(svg polygon),
.nav-item.active .nav-icon :deep(svg ellipse) {
  fill: #409eff !important;
  stroke: #409eff !important;
  stroke-width: 0 !important;
}

/* 对于线条元素（line, polyline），使用粗线条实现填充效果 */
.nav-item.active .nav-icon :deep(svg line),
.nav-item.active .nav-icon :deep(svg polyline) {
  fill: none !important;
  stroke: #409eff !important;
  stroke-width: 3 !important;
  stroke-linecap: round !important;
  stroke-linejoin: round !important;
}

/* 对于路径元素，默认尝试使用 fill 填充，但如果原本只有 stroke（线条图标），则加粗 stroke */
.nav-item.active .nav-icon :deep(svg path) {
  fill: #409eff !important;
  stroke: #409eff !important;
  stroke-width: 2.5 !important;
  stroke-linecap: round !important;
  stroke-linejoin: round !important;
}

.nav-item.active {
  color: #409eff;
}

.nav-item span {
  font-size: var(--font-size-small, 12px);
  transition: color 0.2s ease;
  font-weight: 500;
}


.mobile-badge {
  position: absolute;
  top: 0;
  right: 8px;
}

/* 响应式设计 - 平板端 */
@media (min-width: 768px) and (max-width: 1024px) {
  .header-content {
    padding: 0 var(--spacing-md, 16px);
    max-width: var(--container-max-width-tablet, 700px);
  }

  .home-text {
    display: inline;
    font-size: var(--font-size-body-small, 14px);
  }

  .username {
    display: inline;
    font-size: var(--font-size-body-small, 14px);
  }

  .mobile-bottom-nav {
    display: none;
  }

  .main-content {
    padding: var(--spacing-xl, 24px) var(--spacing-md, 16px);
    padding-bottom: var(--spacing-lg, 20px);
    max-width: var(--container-max-width-tablet, 700px);
  }

  .header {
    height: 60px;
  }

  .home-button {
    height: var(--button-height, 40px);
    font-size: var(--font-size-body-small, 14px);
  }

  .user-info {
    height: var(--button-height, 40px);
    font-size: var(--font-size-body-small, 14px);
  }
}

/* 响应式设计 - 电脑端 */
@media (min-width: 1025px) {
  .home-text {
    display: inline;
    font-size: var(--font-size-body, 16px);
  }

  .username {
    display: inline;
    font-size: var(--font-size-body, 16px);
  }

  .mobile-bottom-nav {
    display: none;
  }

  .main-content {
    padding-bottom: var(--spacing-lg, 20px);
  }
}

/* 响应式设计 - 手机端 */
@media (max-width: 767px) {
  .header {
    height: 56px;
  }

  .header-content {
    padding: 0 var(--spacing-md, 12px);
  }

  .header-menu {
    display: none;
  }

  .home-button {
    height: var(--button-height, 44px);
    padding: 6px 10px;
    font-size: var(--font-size-body-small, 14px);
  }

  .user-info {
    height: var(--button-height, 44px);
    padding: 6px 10px;
    font-size: var(--font-size-body-small, 14px);
  }

  .main-content {
    padding: var(--spacing-md, 16px);
    padding-bottom: 70px; /* 为底部导航留出空间 */
  }

  .mobile-bottom-nav {
    padding: var(--spacing-sm, 8px) 0 calc(var(--spacing-sm, 8px) + env(safe-area-inset-bottom));
  }

  .nav-item {
    padding: var(--spacing-sm, 8px) var(--spacing-md, 12px);
    gap: var(--spacing-xs, 4px);
  }

  .nav-item span {
    font-size: var(--font-size-xs, 11px);
  }

  .nav-item .nav-icon {
    font-size: 22px !important;
  }
}

/* 侧边栏相关样式 */
.drawer-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  backdrop-filter: blur(2px);
}

.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.3s ease;
}

.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}

.home-drawer {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 420px;
  max-width: 90vw;
  background: #ffffff;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  z-index: 2001;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.drawer-slide-enter-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.drawer-slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.drawer-slide-enter-from {
  transform: translateX(-100%);
}

.drawer-slide-leave-to {
  transform: translateX(-100%);
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg, 20px) var(--spacing-lg, 24px);
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
  flex-shrink: 0;
}

.drawer-title {
  font-size: var(--font-size-h4, 20px);
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.drawer-close {
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 4px;
  border-radius: 4px;
}

.drawer-close:hover {
  color: #1a1a1a;
  background: #f3f4f6;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: #ffffff;
}

.drawer-content :deep(.home-view) {
  max-width: 100%;
  padding: 24px;
  min-height: auto;
}

.main-content.drawer-open {
  pointer-events: none;
}

/* 响应式调整 - 手机端 */
@media (max-width: 767px) {
  .home-drawer {
    width: 85vw;
    max-width: 360px;
  }

  .drawer-header {
    padding: var(--spacing-md, 16px) var(--spacing-md, 16px);
  }

  .drawer-title {
    font-size: var(--font-size-h5, 18px);
  }

  .drawer-close {
    font-size: 20px;
  }

  .drawer-content :deep(.home-view) {
    padding: var(--spacing-md, 16px);
  }
}

/* 响应式调整 - 平板端 */
@media (min-width: 768px) and (max-width: 1024px) {
  .home-drawer {
    width: 400px;
    max-width: 80vw;
  }

  .drawer-header {
    padding: var(--spacing-lg, 20px) var(--spacing-lg, 20px);
  }

  .drawer-content :deep(.home-view) {
    padding: var(--spacing-lg, 20px);
  }
}

/* 响应式调整 - 电脑端 */
@media (min-width: 1025px) {
  .home-drawer {
    width: 420px;
  }
}

</style>

