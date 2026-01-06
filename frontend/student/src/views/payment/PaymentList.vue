<template>
  <div class="payment-list list-page-container">
    <!-- 筛选栏 -->
    <el-card class="list-page-filter-card">
      <div class="list-page-filter-content">
        <el-radio-group v-model="statusFilter" @change="loadPayments">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button :label="1">待支付</el-radio-button>
          <el-radio-button :label="2">待确认</el-radio-button>
          <el-radio-button :label="3">已完成</el-radio-button>
          <el-radio-button :label="4">已拒绝</el-radio-button>
        </el-radio-group>
      </div>
      <div class="list-page-search-content">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索教师姓名、订单号..."
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

    <!-- 支付列表 -->
    <div class="payments list-page-list-container">
      <el-card
        v-for="payment in filteredPaymentList"
        :key="payment.id"
        class="payment-card list-page-item-card"
        shadow="hover"
        @click="goToDetail(payment.id)"
      >
        <div class="payment-header">
          <div class="payment-info">
            <h3 class="teacher-name">{{ payment.teacherName }}</h3>
            <el-tag
              :type="getPaymentStatusType(payment.status)"
              size="small"
            >
              {{ getPaymentStatusText(payment.status) }}
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
import { normalizeApiData, createSearchFilter } from '@/utils/dataHelper'
import { getStatusText, getStatusType } from '@/utils/statusHelper'
import { useTimeFormatter } from '@/composables/useTimeFormatter'

const router = useRouter()
const { formatDate, formatDateTime } = useTimeFormatter()

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
    paymentList.value = normalizeApiData(data)
  } catch (error) {
    ElMessage.error('加载支付记录失败')
    console.error(error)
    paymentList.value = []
  } finally {
    loading.value = false
  }
}

// 使用统一的状态处理函数
const getPaymentStatusType = (status) => getStatusType('payment', status)
const getPaymentStatusText = (status) => getStatusText('payment', status)

const filteredPaymentList = computed(() => {
  return createSearchFilter(
    paymentList.value,
    searchKeyword.value,
    ['teacherName', 'paymentNo']
  )
})

const handleSearch = () => {
  // 前端过滤已通过 computed 实现
}

const goToDetail = (id) => {
  router.push(`/payment/${id}`)
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
.filter-content {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.search-content {
  display: flex;
  justify-content: center;
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

