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
        :key="chat.id"
        class="chat-item"
        shadow="hover"
        @click="goToChat(chat.id)"
      >
        <div class="chat-content">
          <el-avatar :src="chat.avatar" :size="50">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="chat-info">
            <div class="chat-header">
              <h3 class="chat-name">{{ chat.name }}</h3>
              <span class="chat-time">{{ formatTime(chat.lastMessageTime) }}</span>
            </div>
            <div class="chat-preview">
              <p class="last-message">{{ getLastMessagePreview(chat.lastMessage) }}</p>
              <el-badge
                v-if="chat.unreadCount > 0"
                :value="chat.unreadCount"
                class="unread-badge"
              />
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && (!filteredChatList || filteredChatList.length === 0)" :description="searchKeyword ? '未找到匹配的聊天记录' : '暂无聊天记录'" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getChatList } from '@/api/chat'
import { User, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const chatList = ref([])
const searchKeyword = ref('')
const loading = ref(false)

const loadChatList = async () => {
  loading.value = true
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
    
    // 如果有 teacherId 参数，直接跳转到对应聊天
    const teacherId = route.query.teacherId
    if (teacherId && chatList.value.length > 0) {
      const chat = chatList.value.find(c => c.teacherId === parseInt(teacherId))
      if (chat) {
        goToChat(chat.id)
      }
    }
  } catch (error) {
    ElMessage.error('加载聊天列表失败')
    console.error(error)
    chatList.value = []
  } finally {
    loading.value = false
  }
}

const getLastMessagePreview = (message) => {
  if (!message) return '暂无消息'
  if (message.type === 1) {
    return message.content || '文本消息'
  } else if (message.type === 2) {
    return '[文件]'
  } else if (message.type === 3) {
    return '[语音]'
  } else if (message.type === 4) {
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

const goToChat = (relationshipId) => {
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
      (chat.name && chat.name.toLowerCase().includes(keyword)) ||
      (chat.lastMessage && chat.lastMessage.content && chat.lastMessage.content.toLowerCase().includes(keyword))
    )
  })
})

onMounted(() => {
  loadChatList()
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
  justify-content: space-between;
  align-items: center;
  gap: 12px;
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

.unread-badge {
  flex-shrink: 0;
}

@media (max-width: 767px) {
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

