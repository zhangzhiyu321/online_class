<template>
  <div class="payment-list">
    <div class="page-header">
      <h1 class="page-title">支付记录</h1>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <div class="filter-content">
        <el-radio-group v-model="statusFilter" @change="loadPayments">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button :label="1">待支付</el-radio-button>
          <el-radio-button :label="2">待确认</el-radio-button>
          <el-radio-button :label="3">已完成</el-radio-button>
          <el-radio-button :label="4">已拒绝</el-radio-button>
        </el-radio-group>
      </div>
      <div class="search-content">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索教师姓名、订单号..."
          clearable
          @input="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </el-card>

    <!-- 支付列表 -->
    <div class="payments">
      <el-card
        v-for="payment in filteredPaymentList"
        :key="payment.id"
        class="payment-card"
        shadow="hover"
        @click="goToDetail(payment.id)"
      >
        <div class="payment-header">
          <div class="payment-info">
            <h3 class="teacher-name">{{ payment.teacherName }}</h3>
            <el-tag
              :type="getStatusType(payment.status)"
              size="small"
            >
              {{ getStatusText(payment.status) }}
            </el-tag>
          </div>
          <div class="payment-amount">
            <span class="amount-label">支付金额</span>
            <span class="amount-value">¥{{ payment.amount }}</span>
          </div>
        </div>
        <div class="payment-details">
          <div class="detail-item">
            <span class="label">订单号：</span>
            <span>{{ payment.paymentNo }}</span>
          </div>
          <div class="detail-item">
            <span class="label">课程时间：</span>
            <span>{{ formatDate(payment.appointmentDate) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">创建时间：</span>
            <span>{{ formatDateTime(payment.createdAt) }}</span>
          </div>
        </div>
        <div class="payment-actions">
          <el-button
            v-if="payment.status === 1"
            type="primary"
            size="small"
            @click.stop="goToDetail(payment.id)"
          >
            去支付
          </el-button>
          <el-button
            type="primary"
            text
            size="small"
            @click.stop="goToDetail(payment.id)"
          >
            查看详情
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && (!filteredPaymentList || filteredPaymentList.length === 0)" :description="searchKeyword ? '未找到匹配的支付记录' : '暂无支付记录'" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getPaymentList } from '@/api/payment'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const paymentList = ref([])
const statusFilter = ref('')
const searchKeyword = ref('')
const loading = ref(false)

const loadPayments = async () => {
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const data = await getPaymentList(params)
    // 确保返回的是数组类型
    if (Array.isArray(data)) {
      paymentList.value = data
    } else if (data && Array.isArray(data.list)) {
      paymentList.value = data.list
    } else {
      paymentList.value = []
    }
  } catch (error) {
    ElMessage.error('加载支付记录失败')
    console.error(error)
    paymentList.value = []
  } finally {
    loading.value = false
  }
}

const filteredPaymentList = computed(() => {
  if (!Array.isArray(paymentList.value)) {
    return []
  }
  if (!searchKeyword.value) {
    return paymentList.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return paymentList.value.filter(payment => {
    return (
      (payment.teacherName && payment.teacherName.toLowerCase().includes(keyword)) ||
      (payment.paymentNo && payment.paymentNo.toLowerCase().includes(keyword))
    )
  })
})

const handleSearch = () => {
  // 如果后端支持搜索，可以调用 loadPayments
  // 否则使用前端过滤（已通过 computed 实现）
}

const getStatusType = (status) => {
  const types = {
    1: 'warning', // 待支付
    2: 'info', // 待确认
    3: 'success', // 已完成
    4: 'danger' // 已拒绝
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

const goToDetail = (id) => {
  router.push(`/payment/${id}`)
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
.payment-list {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.search-content {
  display: flex;
  justify-content: center;
}

.search-input {
  max-width: 400px;
  width: 100%;
}

.payments {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.payment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.payment-info {
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

.payment-amount {
  text-align: right;
}

.amount-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.amount-value {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.payment-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.label {
  color: #909399;
}

.payment-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

@media (max-width: 767px) {
  .payment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .payment-amount {
    text-align: left;
    width: 100%;
  }

  .payment-actions {
    flex-direction: column;
  }

  .payment-actions .el-button {
    width: 100%;
  }
}
</style>

