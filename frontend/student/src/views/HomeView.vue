<template>
  <div class="home-view">
    <!-- 欢迎区域 - Calendly 风格 -->
    <transition name="fade-up" appear>
      <div class="welcome-section">
        <h1 class="welcome-title">欢迎使用线上家教系统</h1>
        <p class="welcome-subtitle">选择您需要的功能，开始您的学习之旅</p>
      </div>
    </transition>

    <!-- 功能卡片网格 - 带延迟动画 -->
    <div class="page-links">
      <transition-group name="card" appear>
        <div
          v-for="(page, index) in pages"
          :key="page.path"
          class="page-card"
          :style="{ '--delay': index * 0.05 + 's' }"
          @click="goToPage(page.path)"
        >
          <div class="card-content">
            <div class="page-icon-wrapper">
              <el-icon :size="28" class="page-icon">
                <component :is="page.icon" />
              </el-icon>
            </div>
            <div class="page-info">
              <h3 class="page-title">{{ page.title }}</h3>
              <p class="page-description">{{ page.description }}</p>
            </div>
            <div class="card-arrow">
              <el-icon :size="20"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  User,
  Calendar,
  Money,
  ChatDotRound,
  Document,
  Setting,
  ArrowRight,
  Bell
} from '@element-plus/icons-vue'

const router = useRouter()

const pages = [
  {
    title: '教师列表',
    description: '浏览和搜索教师',
    path: '/teachers',
    icon: 'User'
  },
  {
    title: '教师详情',
    description: '查看教师详细信息（需要传入教师ID）',
    path: '/teacher/1',
    icon: 'Document'
  },
  {
    title: '创建预约',
    description: '创建新的预约（需要传入教师ID）',
    path: '/appointment/create/1',
    icon: 'Calendar'
  },
  {
    title: '预约列表',
    description: '查看我的所有预约',
    path: '/appointments',
    icon: 'Calendar'
  },
  {
    title: '预约详情',
    description: '查看预约详情（需要传入预约ID）',
    path: '/appointment/1',
    icon: 'Document'
  },
  {
    title: '支付列表',
    description: '查看支付记录',
    path: '/payments',
    icon: 'Money'
  },
  {
    title: '支付详情',
    description: '查看支付详情（需要传入支付ID）',
    path: '/payment/1',
    icon: 'Money'
  },
  {
    title: '聊天列表',
    description: '查看所有聊天',
    path: '/chats',
    icon: 'ChatDotRound'
  },
  {
    title: '聊天窗口',
    description: '聊天界面（需要传入关系ID）',
    path: '/chat/1',
    icon: 'ChatDotRound'
  },
  {
    title: '个人中心',
    description: '个人信息管理',
    path: '/profile',
    icon: 'Setting'
  },
  {
    title: '系统公告',
    description: '查看系统公告和通知',
    path: '/announcements',
    icon: 'Bell'
  }
]

const goToPage = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  min-height: calc(100vh - 60px);
  will-change: transform;
  transform: translateZ(0);
}

/* 欢迎区域 - Calendly 风格 */
.welcome-section {
  text-align: center;
  margin-bottom: 48px;
  padding: 0 16px;
}

.welcome-title {
  font-size: var(--font-size-h1, 42px);
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: var(--spacing-md, 12px);
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.welcome-subtitle {
  font-size: var(--font-size-h5, 18px);
  color: #6b7280;
  font-weight: 400;
  margin: 0;
}

/* 卡片网格布局 */
.page-links {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 0 16px;
}

/* 卡片样式 - 简约灰白风格 */
.page-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              border-color 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  will-change: transform;
  transform: translateZ(0);
}

.page-card:hover {
  transform: translateY(-2px) translateZ(0);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border-color: #d1d5db;
  background: #ffffff;
}

.card-content {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 20px;
}

.page-icon-wrapper {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  transform: translateZ(0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-card:hover .page-icon-wrapper {
  transform: scale(1.05) rotate(5deg) translateZ(0);
}

.page-icon {
  color: #6b7280;
}

.page-info {
  flex: 1;
  text-align: left;
  min-width: 0;
}

.page-title {
  font-size: var(--font-size-h5, 18px);
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px 0;
  line-height: 1.4;
}

.page-description {
  font-size: var(--font-size-body-small, 14px);
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-arrow {
  flex-shrink: 0;
  color: #9ca3af;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              color 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: translateX(-8px) translateZ(0);
  will-change: transform, opacity;
}

.page-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(0) translateZ(0);
  color: #667eea;
}

/* 淡入向上动画 - 缩短到0.2s，性能优化 */
.fade-up-enter-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: opacity, transform;
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(20px) translateZ(0);
}

/* 卡片进入动画 - 缩短到0.2s，性能优化 */
.card-enter-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: var(--delay);
  will-change: opacity, transform;
}

.card-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.95) translateZ(0);
}

.card-move {
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .page-links {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 767px) {
  .home-view {
    padding: 24px 16px;
  }

  .welcome-section {
    margin-bottom: 32px;
  }

  .welcome-title {
    font-size: var(--font-size-h2, 28px);
  }

  .welcome-subtitle {
    font-size: var(--font-size-h6, 16px);
  }

  .page-links {
    grid-template-columns: 1fr;
    padding: 0;
    gap: 16px;
  }

  .card-content {
    padding: 20px;
    gap: 16px;
  }

  .page-icon-wrapper {
    width: 48px;
    height: 48px;
  }

  .page-title {
    font-size: var(--font-size-h6, 16px);
  }

  .page-description {
    font-size: var(--font-size-body-small, 13px);
  }
}

/* 深色模式支持（可选） */
@media (prefers-color-scheme: dark) {
  .page-card {
    background: #1f2937;
    border-color: #374151;
  }

  .page-title {
    color: #f9fafb;
  }

  .page-description {
    color: #9ca3af;
  }

  .welcome-title {
    color: #f9fafb;
  }

  .welcome-subtitle {
    color: #9ca3af;
  }
}
</style>
