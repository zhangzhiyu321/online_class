<template>
  <div class="payment-detail" v-loading="loading">
    <el-button
      type="text"
      :icon="ArrowLeft"
      @click="$router.back()"
      class="back-button"
    >
      返回
    </el-button>

    <el-card v-if="paymentInfo" class="detail-card">
      <div class="detail-header">
        <h2>支付详情</h2>
        <el-tag
          :type="getStatusType(paymentInfo.status)"
          size="large"
        >
          {{ getStatusText(paymentInfo.status) }}
        </el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="支付单号">{{ paymentInfo.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getStatusType(paymentInfo.status)">
            {{ getStatusText(paymentInfo.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="教师姓名">{{ paymentInfo.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">
          <span class="amount">¥{{ paymentInfo.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="课程时间">{{ formatDate(paymentInfo.appointmentDate) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(paymentInfo.createdAt) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 待支付状态 -->
      <div v-if="paymentInfo.status === 1" class="payment-section">
        <h3>支付信息</h3>
        <el-alert
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            <div class="payment-info">
              <p><strong>收款人：</strong>{{ paymentInfo.accountHolder }}</p>
              <p><strong>开户银行：</strong>{{ paymentInfo.bankName }}</p>
              <p><strong>银行账号：</strong>{{ paymentInfo.bankAccount }}</p>
              <p class="tip">请转账后上传转账凭证</p>
            </div>
          </template>
        </el-alert>

        <el-form
          ref="proofFormRef"
          :model="proofForm"
          :rules="proofRules"
          label-width="120px"
          class="proof-form"
        >
          <el-form-item label="转账截图" prop="transferImage">
            <el-upload
              v-model:file-list="fileList"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              list-type="picture"
              :limit="1"
            >
              <el-button type="primary">上传图片</el-button>
              <template #tip>
                <div class="el-upload__tip">支持 jpg/png 格式，大小不超过 5MB</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="转账金额" prop="transferAmount">
            <el-input-number
              v-model="proofForm.transferAmount"
              :precision="2"
              :min="0"
              :max="paymentInfo.amount * 2"
              placeholder="请输入转账金额"
            />
          </el-form-item>

          <el-form-item label="转账时间" prop="transferTime">
            <el-date-picker
              v-model="proofForm.transferTime"
              type="datetime"
              placeholder="选择转账时间"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmitProof" :loading="submitting">
              提交凭证
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 已上传凭证 -->
      <div v-if="paymentInfo.status === 2 && paymentInfo.transferImage" class="proof-section">
        <h3>转账凭证</h3>
        <el-image
          :src="paymentInfo.transferImage"
          :preview-src-list="[paymentInfo.transferImage]"
          fit="cover"
          class="proof-image"
        />
        <div class="proof-info">
          <p><strong>转账金额：</strong>¥{{ paymentInfo.transferAmount }}</p>
          <p><strong>转账时间：</strong>{{ formatDateTime(paymentInfo.transferTime) }}</p>
          <p class="tip">等待教师确认收款</p>
        </div>
      </div>

      <!-- 已拒绝 -->
      <div v-if="paymentInfo.status === 4" class="reject-section">
        <el-alert
          type="error"
          :closable="false"
          show-icon
        >
          <template #title>
            <div>
              <p><strong>拒绝原因：</strong>{{ paymentInfo.rejectReason }}</p>
              <p class="tip">请重新上传正确的转账凭证</p>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 已完成状态 - 可以申请退款 -->
      <div v-if="paymentInfo.status === 3" class="actions-section">
        <el-button
          type="warning"
          @click="goToRefund"
        >
          申请退款
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPaymentDetail, uploadPaymentProof } from '@/api/payment'
import { uploadFile } from '@/api/common'
import { useUserStore } from '@/stores/user'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const paymentId = ref(route.params.id)
const paymentInfo = ref(null)
const loading = ref(false)
const submitting = ref(false)

const fileList = ref([])
const proofForm = ref({
  transferImage: '',
  transferAmount: null,
  transferTime: null
})

const proofFormRef = ref()

const proofRules = {
  transferImage: [{ required: true, message: '请上传转账截图', trigger: 'change' }],
  transferAmount: [
    { required: true, message: '请输入转账金额', trigger: 'blur' }
  ],
  transferTime: [{ required: true, message: '请选择转账时间', trigger: 'change' }]
}

const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/common/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

const loadDetail = async () => {
  loading.value = true
  try {
    const data = await getPaymentDetail(paymentId.value)
    paymentInfo.value = data
    if (data.transferAmount) {
      proofForm.value.transferAmount = data.transferAmount
    }
    if (data.transferTime) {
      proofForm.value.transferTime = new Date(data.transferTime)
    }
    if (data.transferImage) {
      fileList.value = [{ url: data.transferImage }]
      proofForm.value.transferImage = data.transferImage
    }
  } catch (error) {
    ElMessage.error('加载支付详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    1: 'warning',
    2: 'info',
    3: 'success',
    4: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    1: '待支付',
    2: '待确认',
    3: '已完成',
    4: '已拒绝'
  }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  return date.split('T')[0]
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

const handleUploadSuccess = (response) => {
  proofForm.value.transferImage = response.data || response.url
  ElMessage.success('上传成功')
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleSubmitProof = async () => {
  if (!proofFormRef.value) return
  await proofFormRef.value.validate((valid) => {
    if (!valid) return
  })

  submitting.value = true
  try {
    await uploadPaymentProof(paymentId.value, {
      ...proofForm.value,
      transferTime: proofForm.value.transferTime?.toISOString()
    })
    ElMessage.success('提交成功，等待教师确认')
    loadDetail()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const goToRefund = () => {
  router.push(`/refund/create?paymentId=${paymentId.value}`)
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.payment-detail {
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

.payment-section,
.proof-section,
.reject-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.payment-section h3,
.proof-section h3 {
  margin-bottom: 16px;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.payment-info {
  line-height: 1.8;
}

.payment-info p {
  margin: 8px 0;
  color: #4b5563;
  font-size: 15px;
}

.payment-info strong {
  color: #1a1a1a;
  font-weight: 600;
}

.tip {
  color: #6b7280;
  font-size: 13px;
  margin-top: 8px;
  font-style: italic;
}

.proof-form {
  margin-top: 20px;
}

.proof-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

.proof-form :deep(.el-input__wrapper),
.proof-form :deep(.el-textarea__inner) {
  border-radius: 12px;
}

.proof-image {
  width: 100%;
  max-width: 500px;
  border-radius: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.proof-image:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.proof-info {
  line-height: 1.8;
}

.proof-info p {
  margin: 8px 0;
  color: #4b5563;
  font-size: 15px;
}

.proof-info strong {
  color: #1a1a1a;
  font-weight: 600;
}

:deep(.el-alert) {
  border-radius: 16px;
  border: none;
}

:deep(.el-alert__title) {
  font-weight: 500;
}

:deep(.el-upload) {
  border-radius: 12px;
}

:deep(.el-upload-list) {
  margin-top: 12px;
}

:deep(.el-upload-list__item) {
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .payment-detail {
    padding: 0 12px;
  }
}

@media (max-width: 767px) {
  .payment-detail {
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

  .payment-section h3,
  .proof-section h3 {
    font-size: 18px;
  }

  .proof-image {
    max-width: 100%;
  }

  .proof-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left;
  }
}

@media (max-width: 480px) {
  .detail-header h2 {
    font-size: 20px;
  }

  .amount {
    font-size: 18px;
  }

  .payment-section,
  .proof-section,
  .reject-section {
    margin-top: 20px;
    padding-top: 20px;
  }

  .payment-info p,
  .proof-info p {
    font-size: 14px;
  }
}
</style>

