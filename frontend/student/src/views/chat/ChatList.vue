<template>
  <div class="chat-list list-page-container">
    <!-- 搜索栏 -->
    <el-card class="list-page-filter-card">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索聊天对象..."
        clearable
        class="list-page-search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </el-card>

    <div class="chats-container list-page-list-container">
      <el-card
        v-for="chat in filteredChatList"
        :key="chat.relationshipId"
        class="chat-item list-page-item-card"
        shadow="hover"
        @click="handleChatClick(chat)"
      >
        <div class="chat-content">
          <el-avatar :src="chat.otherUserAvatar" :size="50">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="chat-info">
            <div class="chat-header">
              <h3 class="chat-name">{{ chat.otherUserName }}</h3>
              <div class="chat-header-right" @click.stop>
                <span class="chat-time">{{ formatTime(chat.lastMessageTime) }}</span>
                <el-badge
                  v-if="chat.unreadCount > 0"
                  :value="chat.unreadCount"
                  :max="99"
                  class="chat-unread-badge"
                />
              </div>
            </div>
            <div class="chat-preview">
              <p class="last-message">{{ getLastMessagePreview(chat) }}</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && (!filteredChatList || filteredChatList.length === 0)" :description="searchKeyword ? '未找到匹配的聊天记录' : '暂无聊天记录'" />
    
    <!-- 右下角总未读消息数浮动按钮 -->
    <div v-if="totalUnreadCount > 0" class="total-unread-badge">
      <el-badge :value="totalUnreadCount" :max="99" class="unread-badge-wrapper">
        <el-icon :size="24" class="message-icon"><ChatDotRound /></el-icon>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getChatList, getUnreadCount } from '@/api/chat'
import { User, Search, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { normalizeApiData, createSearchFilter } from '@/utils/dataHelper'
import { useTimeFormatter } from '@/composables/useTimeFormatter'
const router = useRouter()
const route = useRoute()
const { formatRelativeTime } = useTimeFormatter()

const loading = ref(false)

const chatList = ref([])
const searchKeyword = ref('')
const totalUnreadCount = ref(0)
let unreadTimer = null
let previousUnreadCount = 0 // 用于检测新消息通知

const loadChatList = async (silent = false) => {
  if (!silent) {
    loading.value = true
  }
  try {
    const data = await getChatList()
    chatList.value = normalizeApiData(data)
  } catch (error) {
    if (!silent) {
      ElMessage.error('加载聊天列表失败')
    }
    console.error(error)
    chatList.value = []
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
  
  // 计算总未读消息数
  totalUnreadCount.value = chatList.value.reduce((sum, chat) => {
    return sum + (chat.unreadCount || 0)
  }, 0)
    
  // 如果有 teacherId 参数，查找对应的聊天关系
  const teacherId = route.query.teacherId
  if (teacherId) {
    const chat = chatList.value.find(c => c.otherUserId === parseInt(teacherId))
    if (chat) {
      goToChat(chat.relationshipId)
    } else {
      ElMessage.info('正在打开聊天窗口...')
      router.push(`/chat/new?teacherId=${teacherId}`)
    }
  }
}

// 请求浏览器通知权限
const requestNotificationPermission = async () => {
  if ('Notification' in window && Notification.permission === 'default') {
    await Notification.requestPermission()
  }
}

// 显示浏览器通知
const showNotification = (title, body, icon) => {
  if ('Notification' in window && Notification.permission === 'granted') {
    // 检查页面是否可见，如果可见则不显示通知（避免重复）
    if (document.hidden) {
      new Notification(title, {
        body: body,
        icon: icon || '/favicon.ico',
        badge: '/favicon.ico',
        tag: 'chat-message', // 使用 tag 避免重复通知
        requireInteraction: false
      })
    }
  }
}

// 加载总未读消息数
const loadTotalUnreadCount = async () => {
  try {
    const data = await getUnreadCount()
    const newCount = data.count || data.total || 0
    
    // 检测是否有新消息（未读数增加）
    if (newCount > previousUnreadCount && previousUnreadCount > 0) {
      const newMessagesCount = newCount - previousUnreadCount
      // 显示浏览器通知
      showNotification(
        '新消息',
        `您有 ${newMessagesCount} 条新消息`,
        '/favicon.ico'
      )
    }
    
    previousUnreadCount = newCount
    totalUnreadCount.value = newCount
  } catch (error) {
    console.error('加载总未读消息数失败:', error)
    totalUnreadCount.value = 0
  }
}

const getLastMessagePreview = (chat) => {
  if (!chat.lastMessageContent && !chat.lastMessageType) {
    return '暂无消息'
  }
  if (chat.lastMessageType === 1) {
    return chat.lastMessageContent || '文本消息'
  } else if (chat.lastMessageType === 2) {
    return '[文件]'
  } else if (chat.lastMessageType === 3) {
    return '[语音]'
  } else if (chat.lastMessageType === 4) {
    return '[图片]'
  }
  return '消息'
}

// 使用统一的时间格式化函数
const formatTime = formatRelativeTime

const handleChatClick = (chat) => {
  if (chat && chat.relationshipId) {
    goToChat(chat.relationshipId)
  } else {
    console.error('聊天项数据无效:', chat)
    ElMessage.error('无法打开聊天窗口')
  }
}

const goToChat = (relationshipId) => {
  if (!relationshipId) {
    console.error('relationshipId 为空')
    ElMessage.error('无法打开聊天窗口')
    return
  }
  router.push(`/chat/${relationshipId}`)
}

const filteredChatList = computed(() => {
  return createSearchFilter(
    chatList.value,
    searchKeyword.value,
    ['otherUserName', 'lastMessageContent']
  )
})

// 处理聊天已读事件
const handleChatRead = (event) => {
  // 当有消息被标记为已读时，立即刷新未读数量
  loadTotalUnreadCount()
  loadChatList(true)
}

// 页面可见性变化处理
const handleVisibilityChange = () => {
  if (document.hidden) {
    // 页面不可见时，降低轮询频率（每10秒）
    if (unreadTimer) {
      clearInterval(unreadTimer)
    }
    unreadTimer = setInterval(() => {
      loadTotalUnreadCount()
      loadChatList(true) // 静默刷新
    }, 10000)
  } else {
    // 页面可见时，提高轮询频率（每3秒）
    if (unreadTimer) {
      clearInterval(unreadTimer)
    }
    // 立即刷新一次
    loadTotalUnreadCount()
    loadChatList(true)
    // 然后每3秒刷新
    unreadTimer = setInterval(() => {
      loadTotalUnreadCount()
      loadChatList(true) // 静默刷新
    }, 3000)
  }
}

onMounted(async () => {
  // 请求通知权限
  await requestNotificationPermission()
  
  // 初始加载
  await loadChatList()
  await loadTotalUnreadCount()
  
  // 设置定时器，每3秒刷新一次未读消息数（页面可见时）
  unreadTimer = setInterval(() => {
    loadTotalUnreadCount()
    loadChatList(true) // 静默刷新，不显示 loading
  }, 3000)
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 监听聊天已读事件
  window.addEventListener('chat-read', handleChatRead)
  
  // 初始化页面可见性状态
  handleVisibilityChange()
})

onUnmounted(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
  // 移除页面可见性监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  // 移除聊天已读事件监听
  window.removeEventListener('chat-read', handleChatRead)
})
</script>

<style scoped>
.chat-content {
  display: flex;
  gap: 16px;
  align-items: center;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chat-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.chat-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  margin-left: 12px;
}

.chat-preview {
  display: flex;
  align-items: center;
}

.last-message {
  flex: 1;
  font-size: 14px;
  color: #606266;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-unread-badge {
  flex-shrink: 0;
}

.chat-unread-badge :deep(.el-badge__content) {
  border: 2px solid #fff;
  font-weight: 600;
  font-size: 11px;
}

.total-unread-badge {
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1000;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.total-unread-badge:hover {
  transform: scale(1.1);
}

.unread-badge-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-icon {
  color: #409eff;
  background: #fff;
  border-radius: 50%;
  padding: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.total-unread-badge :deep(.el-badge__content) {
  border: 2px solid #fff;
  font-weight: 600;
}

@media (max-width: 767px) {
  .total-unread-badge {
    bottom: 100px;
    right: 16px;
  }
  
  .message-icon {
    padding: 6px;
  }

  .chat-content {
    gap: 12px;
  }

  .chat-name {
    font-size: 14px;
  }

  .last-message {
    font-size: 12px;
  }
}
</style>

