<template>
  <div class="payment-list list-page-container" v-loading="loading">
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
    </el-card>

    <!-- 支付列表 -->
    <div class="payment-cards list-page-list-container">
      <el-card
        v-for="payment in paymentList"
        :key="payment.id"
        class="payment-card list-page-item-card"
        shadow="hover"
        @click="goPay(payment)"
      >
        <div class="card-header">
          <div class="left">
            <h3 class="teacher">{{ payment.teacherName }}</h3>
            <el-tag :type="getPaymentStatusType(payment.status)" size="small">
              {{ getPaymentStatusText(payment.status) }}
            </el-tag>
          </div>
        </div>

        <div class="card-body">
          <div class="row">
            <span class="label">支付单号</span>
            <span class="value">{{ payment.paymentNo }}</span>
          </div>
          <div class="row">
            <span class="label">课程时间</span>
            <span class="value">{{ formatDate(payment.appointmentDate) }}</span>
          </div>
          <div class="row">
            <span class="label">创建时间</span>
            <span class="value">{{ formatDateTime(payment.createdAt) }}</span>
          </div>
          <div class="right">
            <span class="label">支付金额</span>
            <span class="amount">¥{{ payment.amount }}</span>
          </div>
        </div>

        <div class="card-actions">
          <el-button
            v-if="payment.status === 1"
            type="primary"
            size="small"
            @click.stop="goPay(payment)"
          >
            继续支付
          </el-button>
          <el-button
            v-else
            type="default"
            size="small"
            @click.stop="goDetail(payment.id)"
          >
            查看详情
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty
      v-if="!loading && (!paymentList || paymentList.length === 0)"
      description="暂无支付订单"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPaymentList } from '@/api/payment'
import { ElMessage } from 'element-plus'
import { getStatusText, getStatusType } from '@/utils/statusHelper'
import { useTimeFormatter } from '@/composables/useTimeFormatter'
import '@/styles/list-pages.css'

const router = useRouter()
const { formatDate, formatDateTime } = useTimeFormatter()

const paymentList = ref([])
const statusFilter = ref('')
const loading = ref(false)

const loadPayments = async () => {
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value) params.status = statusFilter.value
    const data = await getPaymentList(params)
    paymentList.value = data?.list || data || []
  } catch (error) {
    ElMessage.error('加载支付记录失败')
    console.error(error)
    paymentList.value = []
  } finally {
    loading.value = false
  }
}

const getPaymentStatusType = (status) => getStatusType('payment', status)
const getPaymentStatusText = (status) => getStatusText('payment', status)

const goPay = (payment) => {
  router.push(`/payment/create?paymentId=${payment.id}&appointmentId=${payment.appointmentId}`)
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
/* 统一样式已通过 @/styles/list-pages.css 引入 */

.payment-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.payment-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-header .left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.teacher {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.card-header .right {
  text-align: right;
}

.card-header .label {
  display: block;
  font-size: 12px;
  color: #9ca3af;
}

.amount {
  font-size: 22px;
  font-weight: 700;
  color: #ef4444;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 12px 0;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #4b5563;
  font-size: 14px;
}

.row .label {
  color: #9ca3af;
  margin-right: 8px;
}

.row .value {
  color: #374151;
  font-weight: 500;
  text-align: right;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 767px) {
  .payment-list {
    padding: 0 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .card-header .right {
    width: 100%;
    text-align: left;
  }
}
</style>

