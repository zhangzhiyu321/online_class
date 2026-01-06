<template>
  <div class="notification-list">
    <div class="page-header">
      <div class="header-left">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge">
          <span></span>
        </el-badge>
      </div>
      <el-button
        v-if="unreadCount > 0"
        type="text"
        @click="markAllRead"
        :loading="markingAll"
      >
        全部标记为已读
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-tabs v-model="activeType" @tab-change="handleTypeChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="系统通知" name="1" />
        <el-tab-pane label="预约通知" name="2" />
        <el-tab-pane label="支付通知" name="3" />
        <el-tab-pane label="评价通知" name="4" />
        <el-tab-pane label="聊天通知" name="5" />
      </el-tabs>
    </el-card>

    <div class="notification-list-content">
      <div
        v-for="notification in notificationList"
        :key="notification.id"
        class="notification-item"
        :class="{ unread: !notification.isRead }"
        @click="handleClick(notification)"
      >
        <div class="notification-icon">
          <el-icon :size="24" :color="getTypeColor(notification.type)">
            <component :is="getTypeIcon(notification.type)" />
          </el-icon>
        </div>
        <div class="notification-content">
          <div class="notification-header">
            <h3 class="notification-title">{{ notification.title }}</h3>
            <el-tag :type="getTypeTag(notification.type)" size="small">
              {{ getTypeText(notification.type) }}
            </el-tag>
          </div>
          <p class="notification-text">{{ notification.content }}</p>
          <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
        </div>
        <div v-if="!notification.isRead" class="unread-dot"></div>
      </div>
    </div>

    <el-empty v-if="!loading && notificationList.length === 0" description="暂无通知" />

    <div v-if="hasMore" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotificationList, markAllNotificationsRead, getUnreadCount } from '@/api/notification'
import { Bell, Calendar, Money, Star, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const activeType = ref('all')
const notificationList = ref([])
const unreadCount = ref(0)
const loading = ref(false)
const markingAll = ref(false)
const hasMore = ref(true)

const pagination = ref({
  page: 1,
  pageSize: 20
})

let refreshTimer = null

const loadNotificationList = async (reset = false) => {
  if (loading.value) return

  if (reset) {
    pagination.value.page = 1
    notificationList.value = []
  }

  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }
    if (activeType.value !== 'all') {
      params.type = parseInt(activeType.value)
    }

    const data = await getNotificationList(params)
    const list = data.list || []
    const total = data.total || 0

    if (reset) {
      notificationList.value = list
    } else {
      notificationList.value.push(...list)
    }

    hasMore.value = notificationList.value.length < total
  } catch (error) {
    ElMessage.error('加载通知失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const data = await getUnreadCount()
    unreadCount.value = data.count || 0
  } catch (error) {
    console.error(error)
  }
}

const handleTypeChange = () => {
  loadNotificationList(true)
}

const loadMore = () => {
  pagination.value.page++
  loadNotificationList(false)
}

const markAllRead = async () => {
  markingAll.value = true
  try {
    await markAllNotificationsRead()
    ElMessage.success('已全部标记为已读')
    loadNotificationList(true)
    loadUnreadCount()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    markingAll.value = false
  }
}

const handleClick = (notification) => {
  // 根据通知类型跳转到相应页面
  if (notification.relatedType === 'appointment' && notification.relatedId) {
    router.push(`/appointment/${notification.relatedId}`)
  } else if (notification.relatedType === 'payment' && notification.relatedId) {
    router.push(`/payment/${notification.relatedId}`)
  } else if (notification.relatedType === 'refund' && notification.relatedId) {
    router.push(`/refund/${notification.relatedId}`)
  }
}

const getTypeIcon = (type) => {
  const icons = {
    1: Bell,
    2: Calendar,
    3: Money,
    4: Star,
    5: ChatDotRound
  }
  return icons[type] || Bell
}

const getTypeColor = (type) => {
  const colors = {
    1: '#409eff',
    2: '#67c23a',
    3: '#e6a23c',
    4: '#f56c6c',
    5: '#909399'
  }
  return colors[type] || '#409eff'
}

const getTypeTag = (type) => {
  const tags = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: ''
  }
  return tags[type] || 'info'
}

const getTypeText = (type) => {
  const texts = {
    1: '系统通知',
    2: '预约通知',
    3: '支付通知',
    4: '评价通知',
    5: '聊天通知'
  }
  return texts[type] || '通知'
}

const formatTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
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

const refreshData = () => {
  loadNotificationList(true)
  loadUnreadCount()
}

onMounted(() => {
  refreshData()
  // 每30秒刷新一次未读数
  refreshTimer = setInterval(() => {
    loadUnreadCount()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.notification-list {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
}

.notification-list-content {
  margin-bottom: 20px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  border: 1px solid #ebeef5;
}

.notification-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.notification-item.unread {
  background: #f0f9ff;
  border-color: #409eff;
}

.notification-icon {
  margin-right: 16px;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.notification-text {
  color: #606266;
  font-size: 14px;
  margin: 8px 0;
  line-height: 1.5;
}

.notification-time {
  color: #909399;
  font-size: 12px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  position: absolute;
  top: 16px;
  right: 16px;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

@media (max-width: 767px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>

