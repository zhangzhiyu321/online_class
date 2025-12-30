<template>
  <div class="refund-list">
    <div class="page-header">
      <h1 class="page-title">退款记录</h1>
    </div>

    <el-card class="filter-card">
      <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审核" name="1" />
        <el-tab-pane label="已通过" name="2" />
        <el-tab-pane label="已拒绝" name="3" />
        <el-tab-pane label="已完成" name="4" />
      </el-tabs>
    </el-card>

    <div class="refund-grid">
      <el-card
        v-for="refund in refundList"
        :key="refund.id"
        class="refund-card"
        shadow="hover"
        @click="goToDetail(refund.id)"
      >
        <div class="refund-header">
          <div class="refund-info">
            <h3 class="refund-no">退款单号：{{ refund.refundNo }}</h3>
            <el-tag :type="getStatusType(refund.status)" size="small">
              {{ getStatusText(refund.status) }}
            </el-tag>
          </div>
        </div>
        <div class="refund-content">
          <p><strong>退款金额：</strong><span class="amount">¥{{ refund.refundAmount }}</span></p>
          <p><strong>教师：</strong>{{ refund.teacherName }}</p>
          <p><strong>申请时间：</strong>{{ formatDateTime(refund.createdAt) }}</p>
          <p v-if="refund.refundReason"><strong>退款原因：</strong>{{ refund.refundReason }}</p>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && refundList.length === 0" description="暂无退款记录" />

    <div v-if="hasMore" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRefundList } from '@/api/payment'
import { ElMessage } from 'element-plus'

const router = useRouter()

const activeStatus = ref('all')
const refundList = ref([])
const loading = ref(false)
const hasMore = ref(true)

const pagination = ref({
  page: 1,
  pageSize: 10
})

const loadRefundList = async (reset = false) => {
  if (loading.value) return

  if (reset) {
    pagination.value.page = 1
    refundList.value = []
  }

  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }
    if (activeStatus.value !== 'all') {
      params.status = parseInt(activeStatus.value)
    }

    const data = await getRefundList(params)
    const list = data.list || []
    const total = data.total || 0

    if (reset) {
      refundList.value = list
    } else {
      refundList.value.push(...list)
    }

    hasMore.value = refundList.value.length < total
  } catch (error) {
    ElMessage.error('加载退款记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  loadRefundList(true)
}

const loadMore = () => {
  pagination.value.page++
  loadRefundList(false)
}

const goToDetail = (id) => {
  router.push(`/refund/${id}`)
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
  loadRefundList(true)
})
</script>

<style scoped>
.refund-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.refund-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.refund-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.refund-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.refund-header {
  margin-bottom: 16px;
}

.refund-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.refund-no {
  font-size: 16px;
  font-weight: bold;
  margin: 0;
  color: #303133;
}

.refund-content {
  color: #606266;
}

.refund-content p {
  margin: 8px 0;
  font-size: 14px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

@media (max-width: 767px) {
  .refund-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>

