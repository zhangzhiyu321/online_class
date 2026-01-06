<template>
  <div class="appointment-list list-page-container">
    <!-- 筛选栏 -->
    <el-card class="list-page-filter-card">
      <div class="list-page-filter-content">
        <el-radio-group v-model="statusFilter" @change="loadAppointments">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button :label="1">待确认</el-radio-button>
          <el-radio-button :label="2">已确认</el-radio-button>
          <el-radio-button :label="3">已完成</el-radio-button>
          <el-radio-button :label="4">已取消</el-radio-button>
        </el-radio-group>
      </div>
      <div class="list-page-search-content">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索教师姓名、科目名称..."
          clearable
          @input="handleSearch"
          class="list-page-search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </el-card>

    <!-- 预约列表 -->
    <div class="appointments list-page-list-container">
      <el-card
        v-for="appointment in filteredAppointmentList"
        :key="appointment.id"
        class="appointment-card list-page-item-card"
        shadow="hover"
        @click="goToDetail(appointment.id)"
      >
        <div class="appointment-header">
          <div class="appointment-info">
            <h3 class="teacher-name">{{ appointment.teacherName }}</h3>
            <el-tag
              :type="getAppointmentStatusType(appointment.status)"
              size="small"
            >
              {{ getAppointmentStatusText(appointment.status) }}
            </el-tag>
          </div>
          <div class="appointment-time">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDateTime(appointment.appointmentDate, appointment.startTime) }}</span>
          </div>
        </div>
        <div class="appointment-details">
          <div class="detail-item">
            <span class="label">科目：</span>
            <span>{{ appointment.subjectName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">时长：</span>
            <span>{{ appointment.duration }}分钟</span>
          </div>
          <div class="detail-item">
            <span class="label">金额：</span>
            <span class="amount">¥{{ appointment.totalAmount }}</span>
          </div>
        </div>
        <div class="appointment-actions">
          <el-button
            v-if="appointment.status === 1 || appointment.status === 2"
            size="small"
            class="appointment-btn appointment-btn-cancel"
            @click.stop="handleCancel(appointment)"
          >
            取消预约
          </el-button>
          <el-button
            v-if="appointment.status === 2"
            size="small"
            class="appointment-btn appointment-btn-contact"
            @click.stop="goToChat(appointment.teacherId)"
          >
            联系教师
          </el-button>
          <el-button
            size="small"
            class="appointment-btn appointment-btn-detail"
            @click.stop="goToDetail(appointment.id)"
          >
            查看详情
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && (!filteredAppointmentList || filteredAppointmentList.length === 0)" :description="searchKeyword ? '未找到匹配的预约记录' : '暂无预约记录'" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getAppointmentList, cancelAppointment } from '@/api/appointment'
import { Calendar, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { normalizeApiData, createSearchFilter } from '@/utils/dataHelper'
import { getStatusText, getStatusType } from '@/utils/statusHelper'
import { useTimeFormatter } from '@/composables/useTimeFormatter'
import '@/styles/appointment-buttons.css'

const router = useRouter()
const { formatDateTime } = useTimeFormatter()

const appointmentList = ref([])
const statusFilter = ref('')
const searchKeyword = ref('')
const loading = ref(false)

const loadAppointments = async () => {
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const data = await getAppointmentList(params)
    appointmentList.value = normalizeApiData(data)
  } catch (error) {
    ElMessage.error('加载预约列表失败')
    console.error(error)
    appointmentList.value = []
  } finally {
    loading.value = false
  }
}

// 使用统一的状态处理函数
const getAppointmentStatusType = (status) => getStatusType('appointment', status)
const getAppointmentStatusText = (status) => getStatusText('appointment', status)

const filteredAppointmentList = computed(() => {
  return createSearchFilter(
    appointmentList.value,
    searchKeyword.value,
    ['teacherName', 'subjectName', 'remark']
  )
})

const handleSearch = () => {
  // 前端过滤已通过 computed 实现
}

const goToDetail = (id) => {
  router.push(`/appointment/${id}`)
}

const goToChat = (teacherId) => {
  router.push(`/chats?teacherId=${teacherId}`)
}

const handleCancel = async (appointment) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消预约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入取消原因',
      inputValidator: (value) => {
        if (!value || value.trim() === '') {
          return '取消原因不能为空'
        }
        return true
      }
    })
    await cancelAppointment(appointment.id, value.trim())
    ElMessage.success('取消成功')
    loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

onMounted(() => {
  loadAppointments()
})
</script>

<style scoped>

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.appointment-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.teacher-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.appointment-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.appointment-details {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: #909399;
  font-size: 14px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

/* 按钮样式已移至公共文件 @/styles/appointment-buttons.css */

@media (max-width: 767px) {
  .appointment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .appointment-details {
    flex-direction: column;
    gap: 12px;
  }
}
</style>

