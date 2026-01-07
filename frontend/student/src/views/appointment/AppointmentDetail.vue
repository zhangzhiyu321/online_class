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
          class="status-tag"
        >
          {{ getStatusText(appointmentInfo.status) }}
        </el-tag>
      </div>

      <!-- 桌面端使用 el-descriptions -->
      <el-descriptions :column="2" border class="desktop-descriptions">
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

      <!-- 手机端使用自定义布局 -->
      <div class="mobile-info">
        <div class="info-section">
          <div class="info-group">
            <div class="info-item">
              <span class="info-label">订单号</span>
              <span class="info-value">{{ appointmentInfo.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">预约时间</span>
              <span class="info-value">{{ formatDateTime(appointmentInfo.appointmentDate, appointmentInfo.startTime) }}</span>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h3 class="section-title">课程信息</h3>
          <div class="info-group">
            <div class="info-item">
              <span class="info-label">教师</span>
              <span class="info-value">{{ appointmentInfo.teacherName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">教学阶段</span>
              <span class="info-value">{{ appointmentInfo.stageName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">科目</span>
              <span class="info-value">{{ appointmentInfo.subjectName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">课程时长</span>
              <span class="info-value">{{ appointmentInfo.duration }}分钟</span>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h3 class="section-title">费用信息</h3>
          <div class="info-group">
            <div class="info-item">
              <span class="info-label">课时单价</span>
              <span class="info-value">¥{{ appointmentInfo.pricePerHour }}/小时</span>
            </div>
            <div class="info-item highlight">
              <span class="info-label">总金额</span>
              <span class="info-value amount">¥{{ appointmentInfo.totalAmount }}</span>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h3 class="section-title">学生信息</h3>
          <div class="info-group">
            <div class="info-item">
              <span class="info-label">学生姓名</span>
              <span class="info-value">{{ appointmentInfo.studentName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学生年级</span>
              <span class="info-value">{{ appointmentInfo.studentGrade }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">联系方式</span>
              <span class="info-value">{{ appointmentInfo.studentPhone }}</span>
            </div>
          </div>
        </div>

        <div class="info-section" v-if="appointmentInfo.remark">
          <h3 class="section-title">备注信息</h3>
          <div class="remark-content">{{ appointmentInfo.remark }}</div>
        </div>

        <div class="info-section" v-if="appointmentInfo.dingtalkUrl">
          <h3 class="section-title">会议链接</h3>
          <el-link :href="appointmentInfo.dingtalkUrl" target="_blank" type="primary" class="meeting-link">
            {{ appointmentInfo.dingtalkUrl }}
          </el-link>
        </div>
      </div>

      <div class="appointment-actions">
        <el-button
          v-if="appointmentInfo.status === 1 || appointmentInfo.status === 2"
          size="small"
          class="appointment-btn appointment-btn-cancel"
          @click="handleCancel"
        >
          取消预约
        </el-button>
        <el-button
          v-if="appointmentInfo.status === 2"
          size="small"
          class="appointment-btn appointment-btn-contact"
          @click="goToChat"
        >
          联系教师
        </el-button>
        <el-button
          v-if="appointmentInfo.status === 3 && !appointmentInfo.hasReview"
          size="small"
          class="appointment-btn appointment-btn-payment"
          @click="goToPayment"
        >
          去支付
        </el-button>
        <el-button
          v-if="appointmentInfo.status === 3 && appointmentInfo.hasPayment && !appointmentInfo.hasReview"
          size="small"
          class="appointment-btn appointment-btn-review"
          @click="goToReview"
        >
          去评价
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAppointmentDetail, cancelAppointment } from '@/api/appointment'
import { getChatList } from '@/api/chat'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import '@/styles/appointment-buttons.css'

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
    await cancelAppointment(appointmentId.value, value.trim())
    ElMessage.success('取消成功')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const goToChat = async () => {
  if (!appointmentInfo.value.teacherId) {
    ElMessage.error('教师ID无效')
    return
  }
  
  try {
    // 先检查是否已有聊天关系
    const chatList = await getChatList()
    const chats = Array.isArray(chatList) ? chatList : (chatList?.list || [])
    const existingChat = chats.find(chat => chat.otherUserId === parseInt(appointmentInfo.value.teacherId))
    
    if (existingChat && existingChat.relationshipId) {
      // 如果已有聊天关系，直接跳转到聊天窗口
      router.push(`/chat/${existingChat.relationshipId}`)
    } else {
      // 如果没有聊天关系，跳转到新聊天页面
      router.push(`/chat/new?teacherId=${appointmentInfo.value.teacherId}`)
    }
  } catch (error) {
    console.error('检查聊天关系失败:', error)
    // 如果检查失败，直接跳转到新聊天页面
    router.push(`/chat/new?teacherId=${appointmentInfo.value.teacherId}`)
  }
}

const goToPayment = () => {
  router.push(`/payment/create?appointmentId=${appointmentId.value}`)
}

const goToReview = () => {
  router.push(`/review/create?appointmentId=${appointmentId.value}`)
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
  padding: 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f3f4f6;
  flex-wrap: wrap;
  gap: 16px;
}

.detail-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.status-tag {
  border-radius: 12px;
  padding: 6px 16px;
  font-weight: 500;
}

.amount {
  color: #ef4444;
  font-weight: 700;
  font-size: 22px;
}

/* 按钮样式已移至公共文件 @/styles/appointment-buttons.css */
.appointment-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #f3f4f6;
}

/* 桌面端描述列表样式 */
.desktop-descriptions {
  display: block;
}

:deep(.desktop-descriptions .el-descriptions) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.desktop-descriptions .el-descriptions__label) {
  font-weight: 600;
  color: #374151;
  background: #f9fafb;
  font-size: 14px;
  padding: 16px 20px;
}

:deep(.desktop-descriptions .el-descriptions__content) {
  color: #4b5563;
  font-size: 14px;
  padding: 16px 20px;
}

:deep(.desktop-descriptions .el-descriptions__table) {
  border-radius: 16px;
}

/* 手机端自定义布局 */
.mobile-info {
  display: none;
}

/* 信息区块 */
.info-section {
  margin-bottom: 24px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e5e7eb;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item.highlight {
  background: #ffffff;
  padding: 16px;
  border-radius: 8px;
  border: 2px solid #fef0f0;
  margin-top: 8px;
}

.info-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  flex-shrink: 0;
  min-width: 80px;
}

.info-value {
  font-size: 15px;
  color: #1a1a1a;
  font-weight: 500;
  text-align: right;
  flex: 1;
}

.info-value.amount {
  color: #ef4444;
  font-weight: 700;
  font-size: 20px;
}

.remark-content {
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.meeting-link {
  display: block;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  font-size: 14px;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .appointment-detail {
    padding: 0 12px;
  }

  .detail-card {
    padding: 20px;
  }
}

@media (max-width: 767px) {
  .appointment-detail {
    padding: 0;
  }

  .back-button {
    margin: 12px 16px;
  }

  .detail-card {
    margin: 0;
    border-radius: 0;
    box-shadow: none;
    padding: 20px 16px;
  }

  .detail-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .detail-header h2 {
    font-size: 22px;
  }

  .status-tag {
    font-size: 13px;
    padding: 4px 12px;
  }

  /* 隐藏桌面端描述列表 */
  .desktop-descriptions {
    display: none;
  }

  /* 显示手机端布局 */
  .mobile-info {
    display: block;
  }

  .info-section {
    margin-bottom: 16px;
    padding: 16px;
  }

  .section-title {
    font-size: 15px;
    margin-bottom: 12px;
    padding-bottom: 8px;
  }

  .info-item {
    padding: 10px 0;
  }

  .info-label {
    font-size: 13px;
    min-width: 70px;
  }

  .info-value {
    font-size: 14px;
  }

  .info-value.amount {
    font-size: 18px;
  }

  .appointment-actions {
    margin-top: 24px;
    padding-top: 20px;
  }
}

@media (max-width: 480px) {
  .detail-header h2 {
    font-size: 20px;
  }

  .info-section {
    padding: 12px;
  }

  .section-title {
    font-size: 14px;
  }

  .info-label {
    font-size: 12px;
    min-width: 60px;
  }

  .info-value {
    font-size: 13px;
  }

  .info-value.amount {
    font-size: 16px;
  }
}
</style>

