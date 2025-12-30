<template>
  <div class="appointment-detail" v-loading="loading">
    <el-button
      type="text"
      :icon="ArrowLeft"
      @click="$router.back()"
      class="back-button"
    >
      返回
    </el-button>

    <el-card v-if="appointmentInfo" class="detail-card">
      <div class="detail-header">
        <h2>预约详情</h2>
        <el-tag
          :type="getStatusType(appointmentInfo.status)"
          size="large"
        >
          {{ getStatusText(appointmentInfo.status) }}
        </el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ appointmentInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ formatDateTime(appointmentInfo.appointmentDate, appointmentInfo.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="教师姓名">{{ appointmentInfo.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="教学阶段">{{ appointmentInfo.stageName }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ appointmentInfo.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="课程时长">{{ appointmentInfo.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="课时单价">¥{{ appointmentInfo.pricePerHour }}/小时</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="amount">¥{{ appointmentInfo.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ appointmentInfo.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学生年级">{{ appointmentInfo.studentGrade }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ appointmentInfo.studentPhone }}</el-descriptions-item>
        <el-descriptions-item label="备注信息" :span="2">
          {{ appointmentInfo.remark || '无' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="appointmentInfo.dingtalkUrl" label="钉钉会议链接" :span="2">
          <el-link :href="appointmentInfo.dingtalkUrl" target="_blank" type="primary">
            {{ appointmentInfo.dingtalkUrl }}
          </el-link>
        </el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button
          v-if="appointmentInfo.status === 1 || appointmentInfo.status === 2"
          type="danger"
          @click="handleCancel"
        >
          取消预约
        </el-button>
        <el-button
          v-if="appointmentInfo.status === 2"
          type="primary"
          @click="goToChat"
        >
          联系教师
        </el-button>
        <el-button
          v-if="appointmentInfo.status === 3"
          type="primary"
          @click="goToPayment"
        >
          去支付
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAppointmentDetail, cancelAppointment } from '@/api/appointment'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const appointmentId = ref(route.params.id)
const appointmentInfo = ref(null)
const loading = ref(false)

const loadDetail = async () => {
  loading.value = true
  try {
    const data = await getAppointmentDetail(appointmentId.value)
    appointmentInfo.value = data
  } catch (error) {
    ElMessage.error('加载预约详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    1: 'warning',
    2: 'success',
    3: 'info',
    4: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    1: '待确认',
    2: '已确认',
    3: '已完成',
    4: '已取消'
  }
  return texts[status] || '未知'
}

const formatDateTime = (date, time) => {
  if (!date) return ''
  const dateStr = date.split('T')[0]
  return `${dateStr} ${time || ''}`
}

const handleCancel = async () => {
  try {
    await ElMessageBox.prompt('请输入取消原因', '取消预约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    const reason = '用户取消'
    await cancelAppointment(appointmentId.value, reason)
    ElMessage.success('取消成功')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const goToChat = () => {
  router.push(`/chats?teacherId=${appointmentInfo.value.teacherId}`)
}

const goToPayment = () => {
  router.push(`/payments?appointmentId=${appointmentId.value}`)
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.appointment-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 16px;
}

.back-button {
  margin-bottom: 20px;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover {
  background: #f3f4f6;
}

.detail-card {
  margin-bottom: 20px;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.amount {
  color: #ef4444;
  font-weight: 600;
  font-size: 20px;
}

.actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.actions .el-button {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 优化描述列表样式 */
:deep(.el-descriptions) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #374151;
  background: #f9fafb;
}

:deep(.el-descriptions__content) {
  color: #4b5563;
}

:deep(.el-descriptions__table) {
  border-radius: 16px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .appointment-detail {
    padding: 0 12px;
  }
}

@media (max-width: 767px) {
  .appointment-detail {
    padding: 0 12px;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .detail-header h2 {
    font-size: 24px;
  }

  .actions {
    flex-direction: column;
    width: 100%;
  }

  .actions .el-button {
    width: 100%;
  }

  :deep(.el-descriptions) {
    font-size: 14px;
  }

  :deep(.el-descriptions__label) {
    width: 100px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .detail-header h2 {
    font-size: 20px;
  }

  .amount {
    font-size: 18px;
  }

  :deep(.el-descriptions__table) {
    display: block;
  }

  :deep(.el-descriptions__label),
  :deep(.el-descriptions__content) {
    display: block;
    width: 100%;
    text-align: left;
  }
}
</style>

