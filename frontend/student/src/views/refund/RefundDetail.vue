<template>
  <div class="refund-detail" v-loading="loading">
    <el-button
      type="text"
      :icon="ArrowLeft"
      @click="$router.back()"
      class="back-button"
    >
      返回
    </el-button>

    <el-card v-if="refundInfo" class="detail-card">
      <div class="detail-header">
        <h2>退款详情</h2>
        <el-tag
          :type="getStatusType(refundInfo.status)"
          size="large"
        >
          {{ getStatusText(refundInfo.status) }}
        </el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="退款单号">{{ refundInfo.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <el-tag :type="getStatusType(refundInfo.status)">
            {{ getStatusText(refundInfo.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <span class="amount">¥{{ refundInfo.refundAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="教师姓名">{{ refundInfo.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDateTime(refundInfo.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">
          {{ refundInfo.refundReason || '无' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="refundInfo.auditReason" label="审核意见" :span="2">
          {{ refundInfo.auditReason }}
        </el-descriptions-item>
        <el-descriptions-item v-if="refundInfo.auditAt" label="审核时间">
          {{ formatDateTime(refundInfo.auditAt) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="refundInfo.refundTime" label="退款时间">
          {{ formatDateTime(refundInfo.refundTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="refundInfo.refundAccount" label="退款账号">
          {{ refundInfo.refundAccount }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getRefundDetail } from '@/api/payment'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()

const refundId = ref(route.params.id)
const refundInfo = ref(null)
const loading = ref(false)

const loadDetail = async () => {
  loading.value = true
  try {
    const data = await getRefundDetail(refundId.value)
    refundInfo.value = data
  } catch (error) {
    ElMessage.error('加载退款详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    1: '待审核',
    2: '已通过',
    3: '已拒绝',
    4: '已完成'
  }
  return texts[status] || '未知'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.refund-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h2 {
  margin: 0;
  color: #303133;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}
</style>

