<template>
  <div class="create-refund" v-loading="loading">
    <el-button
      type="text"
      :icon="ArrowLeft"
      @click="$router.back()"
      class="back-button"
    >
      返回
    </el-button>

    <el-card class="form-card">
      <template #header>
        <span>申请退款</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="支付单号">
          <el-input v-model="paymentInfo.paymentNo" disabled />
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input v-model="paymentInfo.amount" disabled>
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="退款原因" prop="refundReason">
          <el-input
            v-model="form.refundReason"
            type="textarea"
            :rows="4"
            placeholder="请输入退款原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交申请
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPaymentDetail } from '@/api/payment'
import { createRefund } from '@/api/payment'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const paymentId = ref(route.query.paymentId)
const paymentInfo = ref({})
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  paymentId: null,
  refundReason: ''
})

const formRef = ref()

const rules = {
  refundReason: [
    { required: true, message: '请输入退款原因', trigger: 'blur' },
    { min: 10, message: '退款原因至少10个字符', trigger: 'blur' }
  ]
}

const loadPaymentInfo = async () => {
  if (!paymentId.value) {
    ElMessage.error('缺少支付单ID')
    router.back()
    return
  }

  loading.value = true
  try {
    const data = await getPaymentDetail(paymentId.value)
    paymentInfo.value = data
    form.value.paymentId = data.id
  } catch (error) {
    ElMessage.error('加载支付信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
  })

  await ElMessageBox.confirm('确认提交退款申请？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })

  submitting.value = true
  try {
    await createRefund(form.value)
    ElMessage.success('退款申请已提交，等待审核')
    router.push('/refunds')
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadPaymentInfo()
})
</script>

<style scoped>
.create-refund {
  max-width: 800px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}
</style>

