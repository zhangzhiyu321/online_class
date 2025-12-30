<template>
  <div class="announcement-detail">
    <el-button type="text" :icon="ArrowLeft" @click="$router.back()" class="back-button">
      返回
    </el-button>
    
    <el-card v-loading="loading" v-if="announcement">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-tag :type="getPriorityType(announcement.priority)" size="small">
              {{ getPriorityText(announcement.priority) }}
            </el-tag>
            <el-tag :type="getTypeType(announcement.type)" size="small" style="margin-left: 8px">
              {{ getTypeText(announcement.type) }}
            </el-tag>
          </div>
          <div class="header-right">
            <span class="publish-time">{{ formatTime(announcement.publishTime) }}</span>
          </div>
        </div>
        <h1 class="announcement-title">{{ announcement.title }}</h1>
      </template>
      
      <div class="announcement-content" v-html="formatContent(announcement.content)"></div>
      
      <div class="card-footer">
        <span class="view-count">浏览 {{ announcement.viewCount || 0 }} 次</span>
      </div>
    </el-card>
    
    <el-empty v-if="!loading && !announcement" description="公告不存在或已删除" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAnnouncement } from '@/api/announcement'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const announcement = ref(null)
const loading = ref(false)

const loadAnnouncement = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const data = await getAnnouncement(id)
    announcement.value = data
  } catch (error) {
    ElMessage.error('加载公告详情失败')
    console.error(error)
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

const formatContent = (content) => {
  if (!content) return ''
  // 简单的格式化，保留换行
  return content.replace(/\n/g, '<br>')
}

onMounted(() => {
  loadAnnouncement()
})
</script>

<style scoped>
.announcement-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.back-button {
  margin-bottom: 16px;
}

.announcement-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 16px 0 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
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
  font-size: 15px;
  color: #1a1a1a;
  line-height: 1.8;
  padding: 24px 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
}

.view-count {
  font-size: 14px;
  color: #9ca3af;
}

@media (max-width: 767px) {
  .announcement-detail {
    padding: 16px;
  }
  
  .announcement-title {
    font-size: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .announcement-content {
    font-size: 14px;
    padding: 16px 0;
  }
}
</style>

