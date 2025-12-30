<template>
  <div class="create-review" v-loading="loading">
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
        <span>评价教师</span>
      </template>

      <div v-if="appointmentInfo" class="appointment-info">
        <h3>课程信息</h3>
        <p><strong>教师：</strong>{{ appointmentInfo.teacherName }}</p>
        <p><strong>科目：</strong>{{ appointmentInfo.subjectName }}</p>
        <p><strong>课程时间：</strong>{{ formatDateTime(appointmentInfo.appointmentDate, appointmentInfo.startTime) }}</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="评分" prop="rating">
          <el-rate
            v-model="form.rating"
            :max="5"
            show-score
            text-color="#ff9900"
            score-template="{value}分"
          />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入您的评价（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="评价图片">
          <el-upload
            v-model:file-list="fileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleRemove"
            list-type="picture-card"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交评价
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAppointmentDetail } from '@/api/appointment'
import { createReview } from '@/api/review'
import { uploadFile } from '@/api/common'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const appointmentId = ref(route.query.appointmentId)
const appointmentInfo = ref(null)
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  appointmentId: null,
  rating: 5,
  content: '',
  images: []
})

const fileList = ref([])
const formRef = ref()

const rules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }]
}

const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/common/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

const loadAppointmentInfo = async () => {
  if (!appointmentId.value) {
    ElMessage.error('缺少预约ID')
    router.back()
    return
  }

  loading.value = true
  try {
    const data = await getAppointmentDetail(appointmentId.value)
    appointmentInfo.value = data
    form.value.appointmentId = data.id
  } catch (error) {
    ElMessage.error('加载预约信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleUploadSuccess = (response) => {
  const url = response.data || response.url
  form.value.images.push(url)
  ElMessage.success('上传成功')
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleRemove = (file) => {
  const url = file.url || file.response?.data || file.response?.url
  form.value.images = form.value.images.filter(img => img !== url)
}

const formatDateTime = (date, time) => {
  if (!date) return ''
  const dateStr = date.split('T')[0]
  return time ? `${dateStr} ${time}` : dateStr
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
  })

  submitting.value = true
  try {
    await createReview(form.value)
    ElMessage.success('评价提交成功')
    router.push(`/appointment/${appointmentId.value}`)
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadAppointmentInfo()
})
</script>

<style scoped>
.create-review {
  max-width: 800px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.appointment-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.appointment-info h3 {
  margin: 0 0 12px 0;
  color: #303133;
}

.appointment-info p {
  margin: 8px 0;
  color: #606266;
}
</style>

