<template>
  <div class="create-payment" v-loading="loading">
    <el-button
      type="text"
      :icon="ArrowLeft"
      @click="$router.back()"
      class="back-button"
    >
      返回
    </el-button>

    <el-card v-if="paymentInfo" class="payment-card">
      <div class="payment-header">
        <h2>支付订单</h2>
        <el-tag :type="getStatusType(paymentInfo.status)" size="large">
          {{ getStatusText(paymentInfo.status) }}
        </el-tag>
      </div>

      <!-- 订单信息 -->
      <el-descriptions :column="2" border class="order-info">
        <el-descriptions-item label="支付单号">{{ paymentInfo.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="教师姓名">{{ paymentInfo.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="课程时间">{{ formatDate(paymentInfo.appointmentDate) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(paymentInfo.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="支付金额" :span="2">
          <span class="amount">¥{{ paymentInfo.amount }}</span>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 移动端紧凑显示 -->
      <div class="mobile-order-info">
        <div class="info-row">
          <span class="info-label">支付单号</span>
          <span class="info-value">{{ paymentInfo.paymentNo }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">教师姓名</span>
          <span class="info-value">{{ paymentInfo.teacherName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">课程时间</span>
          <span class="info-value">{{ formatDate(paymentInfo.appointmentDate) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">创建时间</span>
          <span class="info-value">{{ formatDateTime(paymentInfo.createdAt) }}</span>
        </div>
        <div class="info-row highlight">
          <span class="info-label">支付金额</span>
          <span class="info-value amount">¥{{ paymentInfo.amount }}</span>
        </div>
      </div>

      <!-- 选择支付方式 -->
      <div v-if="paymentInfo.status === 1" class="payment-method-section">
        <h3>选择支付方式</h3>
        <el-radio-group v-model="selectedPaymentMethod" @change="handlePaymentMethodChange" class="payment-methods">
          <el-radio :label="1" border class="payment-method-item">
            <div class="method-content">
              <el-icon class="method-icon"><CreditCard /></el-icon>
              <span class="method-name">银行转账</span>
            </div>
          </el-radio>
          <el-radio :label="2" border class="payment-method-item">
            <div class="method-content">
              <el-icon class="method-icon"><Money /></el-icon>
              <span class="method-name">支付宝</span>
            </div>
          </el-radio>
          <el-radio :label="3" border class="payment-method-item">
            <div class="method-content">
              <el-icon class="method-icon"><ChatDotRound /></el-icon>
              <span class="method-name">微信支付</span>
            </div>
          </el-radio>
        </el-radio-group>

        <!-- 银行转账信息 -->
        <div v-if="selectedPaymentMethod === 1" class="payment-content bank-transfer">
          <el-alert type="info" :closable="false" show-icon>
            <template #title>
              <div class="bank-info">
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
                :http-request="handleCustomUpload"
                :on-remove="handleRemoveFile"
                list-type="picture"
                :limit="1"
                :before-upload="beforeUpload"
              >
                <el-button type="primary">上传图片</el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 jpg/png/gif/webp 格式，大小不超过 5MB</div>
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
                style="width: 100%"
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
              <el-button type="primary" @click="handleSubmitProof" :loading="submitting" size="large">
                提交凭证
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 支付宝二维码 -->
        <div v-if="selectedPaymentMethod === 2" class="payment-content qrcode-payment">
          <el-alert type="success" :closable="false" show-icon>
            <template #title>
              <p>请使用支付宝扫描下方二维码完成支付</p>
            </template>
          </el-alert>
          <div class="qrcode-container">
            <el-image
              :src="paymentInfo.alipayQrCode || 'https://via.placeholder.com/300x300?text=支付宝二维码'"
              fit="contain"
              class="qrcode-image"
              :preview-src-list="[paymentInfo.alipayQrCode || '']"
            />
            <p class="qrcode-tip">支付金额：<span class="amount">¥{{ paymentInfo.amount }}</span></p>
            <p class="qrcode-tip">支付完成后，系统将自动确认</p>
          </div>
        </div>

        <!-- 微信支付二维码 -->
        <div v-if="selectedPaymentMethod === 3" class="payment-content qrcode-payment">
          <el-alert type="success" :closable="false" show-icon>
            <template #title>
              <p>请使用微信扫描下方二维码完成支付</p>
            </template>
          </el-alert>
          <div class="qrcode-container">
            <el-image
              :src="paymentInfo.wechatQrCode || 'https://via.placeholder.com/300x300?text=微信二维码'"
              fit="contain"
              class="qrcode-image"
              :preview-src-list="[paymentInfo.wechatQrCode || '']"
            />
            <p class="qrcode-tip">支付金额：<span class="amount">¥{{ paymentInfo.amount }}</span></p>
            <p class="qrcode-tip">支付完成后，系统将自动确认</p>
          </div>
        </div>
      </div>
      <!-- 已提交凭证状态 -->
      <div v-if="paymentInfo.status === 2" class="payment-content submitted">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            <p>支付凭证已提交，等待教师确认收款</p>
          </template>
        </el-alert>
        <!-- 显示已上传的凭证 -->
        <div v-if="paymentInfo.transferImage" class="proof-section">
          <div class="proof-image-wrapper">
            <el-image
              :src="paymentInfo.transferImage"
              :preview-src-list="[paymentInfo.transferImage]"
              fit="cover"
              class="proof-image"
              :preview-teleported="true"
            />
            <div class="image-hint">点击查看大图</div>
          </div>
          <div class="proof-info">
            <p v-if="paymentInfo.transferAmount"><strong>转账金额：</strong>¥{{ paymentInfo.transferAmount }}</p>
            <p v-if="paymentInfo.transferTime"><strong>转账时间：</strong>{{ formatDateTime(paymentInfo.transferTime) }}</p>
          </div>
        </div>
      </div>

      <!-- 已拒绝状态 -->
      <div v-if="paymentInfo.status === 4" class="payment-content rejected">
        <el-alert type="error" :closable="false" show-icon>
          <template #title>
            <div>
              <p v-if="paymentInfo.rejectReason"><strong>拒绝原因：</strong>{{ paymentInfo.rejectReason }}</p>
              <p class="tip">请重新上传正确的转账凭证</p>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 已完成状态 -->
      <div v-if="paymentInfo.status === 3" class="payment-content completed">
        <el-alert type="success" :closable="false" show-icon>
          <template #title>
            <p>支付已完成</p>
          </template>
        </el-alert>
        <div class="actions-section">
          <el-button type="warning" @click="goToRefund">申请退款</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createPayment, getPaymentDetail, uploadPaymentProof, updatePaymentMethod } from '@/api/payment'
import { uploadFile } from '@/api/common'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, CreditCard, Money, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 支持两种路由方式：/payment/create?paymentId=xxx&appointmentId=xxx 或 /payment/:id
const paymentIdFromRoute = ref(route.params.id || route.query.paymentId)
const appointmentId = ref(route.query.appointmentId)
const paymentInfo = ref(null)
const loading = ref(false)
const submitting = ref(false)
const selectedPaymentMethod = ref(1)

const fileList = ref([])
const proofForm = ref({
  transferImage: '',
  transferAmount: null,
  transferTime: null
})

const proofFormRef = ref()

const proofRules = {
  transferImage: [{ 
    required: true, 
    message: '请上传转账截图', 
    trigger: 'change',
    validator: (rule, value, callback) => {
      if (!proofForm.value.transferImage) {
        callback(new Error('请上传转账截图'))
      } else {
        callback()
      }
    }
  }],
  transferAmount: [
    { required: true, message: '请输入转账金额', trigger: 'blur' }
  ],
  transferTime: [{ required: true, message: '请选择转账时间', trigger: 'change' }]
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

// 自定义上传
const handleCustomUpload = async (options) => {
  const { file } = options
  try {
    const result = await uploadFile(file, 'image')
    // 处理不同的返回格式
    const imageUrl = result?.url || result?.data?.url || (typeof result === 'string' ? result : '')
    if (!imageUrl) {
      throw new Error('上传失败：未获取到图片URL')
    }
    proofForm.value.transferImage = imageUrl
    fileList.value = [{
      name: file.name,
      url: imageUrl,
      uid: file.uid
    }]
    // 触发表单验证
    if (proofFormRef.value) {
      proofFormRef.value.validateField('transferImage')
    }
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error(error.message || '上传失败')
    // 移除失败的文件
    fileList.value = fileList.value.filter(item => item.uid !== file.uid)
  }
}

// 移除文件
const handleRemoveFile = () => {
  proofForm.value.transferImage = ''
  fileList.value = []
}

const loadPayment = async () => {
  loading.value = true
  try {
    // 如果已有支付ID（从路由参数或query参数），直接查询
    if (paymentIdFromRoute.value) {
      const data = await getPaymentDetail(paymentIdFromRoute.value)
      paymentInfo.value = data
      selectedPaymentMethod.value = data.paymentMethod || 1
      // 如果数据中有已上传的凭证信息，填充表单
      if (data.transferAmount) {
        proofForm.value.transferAmount = data.transferAmount
      }
      if (data.transferTime) {
        proofForm.value.transferTime = new Date(data.transferTime)
      }
      if (data.transferImage) {
        fileList.value = [{ 
          name: '转账截图',
          url: data.transferImage,
          uid: Date.now()
        }]
        proofForm.value.transferImage = data.transferImage
      }
      return
    }

    // 否则根据预约ID创建支付记录
    if (appointmentId.value) {
      const response = await createPayment({
        appointmentId: appointmentId.value,
        paymentMethod: selectedPaymentMethod.value
      })
      paymentInfo.value = response
      selectedPaymentMethod.value = response.paymentMethod || 1
    } else {
      ElMessage.error('缺少预约ID或支付ID')
      router.back()
    }
  } catch (error) {
    ElMessage.error(error.message || '加载支付信息失败')
    console.error(error)
    router.back()
  } finally {
    loading.value = false
  }
}

const handlePaymentMethodChange = async (method) => {
  if (!paymentInfo.value || paymentInfo.value.status !== 1) return
  
  // 更新支付方式
  try {
    await updatePaymentMethod(paymentInfo.value.id, method)
    // 重新加载支付信息
    const data = await getPaymentDetail(paymentInfo.value.id)
    paymentInfo.value = data
    ElMessage.success('支付方式已更新')
  } catch (error) {
    ElMessage.error(error.message || '更新支付方式失败')
    console.error(error)
    // 恢复原值
    selectedPaymentMethod.value = paymentInfo.value.paymentMethod || 1
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


const handleSubmitProof = async () => {
  if (!proofFormRef.value) return
  await proofFormRef.value.validate((valid) => {
    if (!valid) return
  })

  submitting.value = true
  try {
    const paymentId = paymentIdFromRoute.value || paymentInfo.value.id
    await uploadPaymentProof(paymentId, {
      ...proofForm.value,
      transferTime: proofForm.value.transferTime?.toISOString()
    })
    ElMessage.success('提交成功，等待教师确认')
    loadPayment()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const goToRefund = () => {
  const paymentId = paymentIdFromRoute.value || paymentInfo.value.id
  router.push(`/refund/create?paymentId=${paymentId}`)
}

onMounted(() => {
  loadPayment()
})
</script>

<style scoped>
.create-payment {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 10px;
}

.back-button {
  margin-bottom: 12px;
  padding: 6px 12px;
  border-radius: 10px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover {
  background: #f3f4f6;
}

.payment-card {
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  padding: 12px 14px 12px;
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f5f9;
  gap: 10px;
}

.payment-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.order-info {
  margin-bottom: 10px;
}

/* 移动端隐藏表格，显示紧凑列表 */
.mobile-order-info {
  display: none;
}

.mobile-order-info .info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
}

.mobile-order-info .info-row:last-child {
  border-bottom: none;
}

.mobile-order-info .info-row.highlight {
  background: #fef0f0;
  padding: 8px 10px;
  border-radius: 6px;
  margin: 6px 0;
  border: none;
}

.mobile-order-info .info-label {
  color: #64748b;
  font-weight: 500;
  flex-shrink: 0;
  min-width: 70px;
  font-size: 13px;
}

.mobile-order-info .info-value {
  color: #0f172a;
  font-weight: 500;
  text-align: right;
  flex: 1;
  font-size: 13px;
}

.mobile-order-info .info-value.amount {
  color: #ef4444;
  font-weight: 700;
  font-size: 16px;
}

.amount {
  color: #ef4444;
  font-weight: 700;
  font-size: 18px;
}

.payment-method-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.payment-method-section h3 {
  margin: 0 0 10px 0;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.payment-methods {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.payment-method-item {
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  transition: all 0.2s;
}

.payment-method-item:hover {
  background: #f8fafc;
}

.method-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.method-icon {
  font-size: 20px;
  color: #409eff;
}

.method-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.payment-content {
  margin-top: 10px;
}

.bank-info {
  line-height: 1.6;
}

.bank-info p {
  margin: 6px 0;
  color: #475569;
  font-size: 14px;
}

.bank-info strong {
  color: #0f172a;
  font-weight: 600;
}

.tip {
  color: #94a3b8;
  font-size: 12px;
  margin-top: 4px;
  font-style: italic;
}

.proof-form {
  margin-top: 8px;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18px 14px;
  background: #f8fafc;
  border-radius: 12px;
  margin-top: 8px;
  border: 1px dashed #e2e8f0;
}

.qrcode-image {
  width: 200px;
  height: 200px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 10px;
}

.qrcode-tip {
  margin: 2px 0;
  color: #475569;
  font-size: 13px;
  text-align: center;
}

.qrcode-tip .amount {
  color: #ef4444;
  font-weight: 700;
  font-size: 16px;
}

.proof-section {
  margin-top: 10px;
}

.proof-image-wrapper {
  display: inline-block;
  position: relative;
  margin-bottom: 10px;
}

.proof-image {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  object-fit: cover;
}

.proof-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.image-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 11px;
  text-align: center;
  padding: 4px 0;
  border-radius: 0 0 8px 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.proof-image-wrapper:hover .image-hint {
  opacity: 1;
}

.proof-info {
  line-height: 1.6;
}

.proof-info p {
  margin: 6px 0;
  color: #475569;
  font-size: 14px;
}

.proof-info strong {
  color: #0f172a;
  font-weight: 600;
}

.actions-section {
  margin-top: 10px;
  text-align: center;
}

:deep(.el-alert) {
  border-radius: 12px;
  border: none;
}

:deep(.el-upload) {
  border-radius: 10px;
}

@media (max-width: 767px) {
  .create-payment {
    padding: 0 10px;
  }

  .payment-card {
    padding: 10px 12px 8px;
  }

  .payment-header {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 8px;
    padding-bottom: 8px;
  }

  .payment-header h2 {
    font-size: 16px;
  }

  /* 移动端隐藏表格，显示紧凑列表 */
  .order-info {
    display: none;
  }

  .mobile-order-info {
    display: block;
    margin-bottom: 10px;
    padding: 6px 0;
  }

  .payment-method-section {
    margin-top: 8px;
    padding-top: 8px;
  }

  .payment-method-section h3 {
    font-size: 14px;
    margin-bottom: 8px;
  }

  .payment-methods {
    grid-template-columns: 1fr;
    gap: 6px;
    margin-bottom: 8px;
  }

  .payment-method-item {
    padding: 8px;
  }

  .method-icon {
    font-size: 16px;
  }

  .method-name {
    font-size: 12px;
  }

  .payment-content {
    margin-top: 8px;
  }

  .bank-info p {
    font-size: 12px;
    margin: 3px 0;
  }

  .proof-form {
    margin-top: 6px;
  }

  .proof-form :deep(.el-form-item) {
    margin-bottom: 10px;
  }

  .proof-form :deep(.el-form-item__label) {
    font-size: 12px;
    padding-bottom: 3px;
    width: 90px !important;
  }

  .qrcode-container {
    padding: 14px 10px;
    margin-top: 6px;
  }

  .qrcode-image {
    width: 160px;
    height: 160px;
    margin-bottom: 8px;
  }

  .qrcode-tip {
    font-size: 12px;
    margin: 2px 0;
  }

  .qrcode-tip .amount {
    font-size: 14px;
  }

  .proof-section {
    margin-top: 8px;
  }

  .proof-image {
    width: 120px;
    height: 120px;
  }

  .image-hint {
    font-size: 10px;
    padding: 3px 0;
  }

  .proof-info p {
    font-size: 12px;
    margin: 3px 0;
  }

  .actions-section {
    margin-top: 8px;
  }

  .actions-section .el-button {
    width: 100%;
  }
}
</style>

