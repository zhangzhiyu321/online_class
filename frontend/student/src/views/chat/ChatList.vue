<template>
  <div class="chat-list">
    <div class="page-header">
      <h1 class="page-title">消息</h1>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索聊天对象..."
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </el-card>

    <div class="chats-container">
      <el-card
        v-for="chat in filteredChatList"
        :key="chat.relationshipId"
        class="chat-item"
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

const router = useRouter()
const route = useRoute()

const chatList = ref([])
const searchKeyword = ref('')
const loading = ref(false)
const totalUnreadCount = ref(0)
let unreadTimer = null
let previousUnreadCount = 0 // 记录上一次的未读数量，用于检测新消息

const loadChatList = async (silent = false) => {
  if (!silent) {
    loading.value = true
  }
  try {
    const data = await getChatList()
    // 确保返回的是数组类型
    if (Array.isArray(data)) {
      chatList.value = data
    } else if (data && Array.isArray(data.list)) {
      chatList.value = data.list
    } else {
      chatList.value = []
    }
    
    // 计算总未读消息数
    const calculatedCount = chatList.value.reduce((sum, chat) => {
      return sum + (chat.unreadCount || 0)
    }, 0)
    
    // 检测是否有新消息（通过比较每个聊天的未读数）
    if (!silent && previousUnreadCount > 0) {
      chatList.value.forEach(chat => {
        if (chat.unreadCount > 0) {
          // 可以在这里添加更详细的通知逻辑
        }
      })
    }
    
    totalUnreadCount.value = calculatedCount
    
    // 如果有 teacherId 参数，查找对应的聊天关系
    const teacherId = route.query.teacherId
    if (teacherId) {
      const chat = chatList.value.find(c => c.otherUserId === parseInt(teacherId))
      if (chat) {
        // 找到聊天关系，跳转到聊天窗口
        goToChat(chat.relationshipId)
      } else {
        // 如果聊天关系不存在，需要创建一个临时聊天关系或直接跳转到聊天窗口
        // 由于后端会在发送第一条消息时自动创建聊天关系，我们可以直接跳转
        // 但需要传递 teacherId 作为接收者ID
        // 这里我们创建一个临时的 relationshipId（使用负数或特殊值），或者直接跳转到聊天窗口
        // 实际上，更好的做法是创建一个接口来初始化聊天关系，但为了简化，我们直接跳转
        // 前端会在 ChatWindow 中处理这种情况
        ElMessage.info('正在打开聊天窗口...')
        // 跳转到一个特殊的聊天窗口，使用 teacherId 作为参数
        router.push(`/chat/new?teacherId=${teacherId}`)
      }
    }
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

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

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
  if (!Array.isArray(chatList.value)) {
    return []
  }
  if (!searchKeyword.value) {
    return chatList.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return chatList.value.filter(chat => {
    return (
      (chat.otherUserName && chat.otherUserName.toLowerCase().includes(keyword)) ||
      (chat.lastMessageContent && chat.lastMessageContent.toLowerCase().includes(keyword))
    )
  })
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
.chat-list {
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 20px;
}

.search-input {
  max-width: 400px;
  width: 100%;
}

.chats-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-item {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.chat-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

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

/* 右下角总未读消息数浮动按钮 */
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

