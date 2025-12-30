<template>
  <div class="announcement-list" v-loading="loading">
    <h1 class="page-title">系统公告</h1>
    
    <el-card 
      v-for="announcement in announcements" 
      :key="announcement.id"
      class="announcement-card"
    >
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-tag :type="getPriorityType(announcement.priority)" size="small">
              {{ getPriorityText(announcement.priority) }}
            </el-tag>
            <el-tag :type="getTypeType(announcement.type)" size="small" style="margin-left: 8px">
              {{ getTypeText(announcement.type) }}
            </el-tag>
            <span class="announcement-title">{{ announcement.title }}</span>
          </div>
          <div class="header-right">
            <span class="publish-time">{{ formatTime(announcement.publishTime) }}</span>
          </div>
        </div>
      </template>
      
      <div class="announcement-content">
        <p class="content-preview">{{ getContentPreview(announcement.content) }}</p>
      </div>
      
      <div class="card-footer">
        <el-button type="primary" text @click="viewDetail(announcement.id)">
          查看详情
        </el-button>
        <span class="view-count">浏览 {{ announcement.viewCount || 0 }} 次</span>
      </div>
    </el-card>
    
    <el-empty v-if="!loading && (!announcements || announcements.length === 0)" description="暂无公告" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublishedAnnouncements } from '@/api/announcement'
import { ElMessage } from 'element-plus'

const router = useRouter()
const announcements = ref([])
const loading = ref(false)

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const data = await getPublishedAnnouncements()
    // 确保返回的是数组类型
    if (Array.isArray(data)) {
      announcements.value = data
    } else if (data && Array.isArray(data.list)) {
      announcements.value = data.list
    } else {
      announcements.value = []
    }
  } catch (error) {
    ElMessage.error('加载公告列表失败')
    console.error(error)
    announcements.value = []
  } finally {
    loading.value = false
  }
}

const getPriorityType = (priority) => {
  const types = {
    1: '',
    2: 'warning',
    3: 'danger'
  }
  return types[priority] || ''
}

const getPriorityText = (priority) => {
  const texts = {
    1: '普通',
    2: '重要',
    3: '紧急'
  }
  return texts[priority] || '普通'
}

const getTypeType = (type) => {
  const types = {
    1: 'info',
    2: 'success',
    3: 'warning'
  }
  return types[type] || 'info'
}

const getTypeText = (type) => {
  const texts = {
    1: '系统公告',
    2: '活动公告',
    3: '维护公告'
  }
  return texts[type] || '系统公告'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getContentPreview = (content) => {
  if (!content) return ''
  // 移除HTML标签（如果有）
  const text = content.replace(/<[^>]*>/g, '')
  // 限制长度
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

const viewDetail = (id) => {
  router.push(`/announcement/${id}`)
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 24px;
}

.announcement-card {
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.announcement-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.announcement-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-left: 8px;
}

.header-right {
  display: flex;
  align-items: center;
}

.publish-time {
  font-size: 14px;
  color: #6b7280;
}

.announcement-content {
  margin: 16px 0;
}

.content-preview {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
  margin: 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.view-count {
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 767px) {
  .announcement-list {
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-left {
    flex-wrap: wrap;
  }
  
  .announcement-title {
    width: 100%;
    margin-left: 0;
    margin-top: 8px;
  }
}
</style>

