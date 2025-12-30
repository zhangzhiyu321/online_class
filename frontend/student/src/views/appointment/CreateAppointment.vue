<template>
  <div class="create-appointment" v-loading="loading">
    <!-- 返回按钮 -->
    <transition name="fade">
      <el-button
        type="text"
        :icon="ArrowLeft"
        @click="$router.back()"
        class="back-button"
      >
        返回
      </el-button>
    </transition>

    <!-- 主容器 -->
    <div class="appointment-container">
      <!-- 教师信息卡片 -->
      <transition name="fade-up" appear>
        <div v-if="teacherInfo" class="teacher-card">
          <el-avatar :src="teacherInfo.avatar" :size="64" class="teacher-avatar" />
          <div class="teacher-details">
            <h3 class="teacher-name">{{ teacherInfo.nickname || teacherInfo.realName }}</h3>
            <div class="teacher-rating">
              <el-rate
                v-model="teacherInfo.rating"
                disabled
                size="small"
                text-color="#ff9900"
              />
              <span class="rating-text">{{ teacherInfo.rating || 0 }}.0</span>
            </div>
          </div>
        </div>
      </transition>

      <!-- 步骤指示器 -->
      <transition name="fade-up" appear>
        <div class="steps-indicator">
          <div
            v-for="(step, index) in steps"
            :key="index"
            class="step-item"
            :class="{ active: currentStep === index, completed: currentStep > index }"
          >
            <div class="step-number">
              <el-icon v-if="currentStep > index"><Check /></el-icon>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <span class="step-label">{{ step.label }}</span>
            <div class="step-line" v-if="index < steps.length - 1"></div>
          </div>
        </div>
      </transition>

      <!-- 表单卡片 -->
      <transition name="slide" mode="out-in">
        <el-card :key="currentStep" class="form-card" shadow="never">
          <!-- 步骤 1: 选择课程信息 -->
          <div v-if="currentStep === 0" class="step-content">
            <h2 class="step-title">选择课程信息</h2>
            <p class="step-description">请选择您要预约的教学阶段和科目</p>

            <el-form
              ref="step1FormRef"
              :model="form"
              :rules="step1Rules"
              label-position="top"
              class="step-form"
            >
              <el-form-item label="教学阶段" prop="stageId">
                <el-select
                  v-model="form.stageId"
                  placeholder="请选择教学阶段"
                  size="large"
                  class="full-width-select"
                  @change="handleStageChange"
                >
                  <el-option
                    v-for="stage in teachingStages"
                    :key="stage.id"
                    :label="stage.name"
                    :value="stage.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="科目" prop="subjectId">
                <el-select
                  v-model="form.subjectId"
                  placeholder="请选择科目"
                  size="large"
                  class="full-width-select"
                  :disabled="!form.stageId"
                  @change="handleSubjectChange"
                >
                  <el-option
                    v-for="teaching in availableTeachings"
                    :key="teaching.id"
                    :label="`${teaching.subjectName} - ¥${teaching.pricePerHour}/小时`"
                    :value="teaching.subjectId"
                  />
                </el-select>
              </el-form-item>

              <div class="step-actions">
                <el-button size="large" @click="$router.back()">取消</el-button>
                <el-button
                  type="primary"
                  size="large"
                  :disabled="!form.stageId || !form.subjectId"
                  @click="nextStep"
                >
                  下一步
                  <el-icon class="ml-2"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-form>
          </div>

          <!-- 步骤 2: 选择时间 -->
          <div v-if="currentStep === 1" class="step-content">
            <h2 class="step-title">选择预约时间</h2>
            <p class="step-description">请选择您希望上课的日期和时间段</p>

            <el-form
              ref="step2FormRef"
              :model="form"
              :rules="step2Rules"
              label-position="top"
              class="step-form"
            >
              <el-form-item label="预约日期" prop="appointmentDate">
                <el-date-picker
                  v-model="form.appointmentDate"
                  type="date"
                  placeholder="选择日期"
                  size="large"
                  class="full-width-select"
                  :disabled-date="disabledDate"
                  @change="handleDateChange"
                />
              </el-form-item>

              <el-form-item label="时间段" prop="timeSlot">
                <el-time-picker
                  v-model="form.timeSlot"
                  is-range
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="HH:mm"
                  value-format="HH:mm"
                  size="large"
                  class="full-width-select"
                />
              </el-form-item>

              <div v-if="duration > 0 && pricePerHour > 0" class="price-preview">
                <div class="price-item">
                  <span>时长：</span>
                  <span class="price-value">{{ duration }} 分钟</span>
                </div>
                <div class="price-item">
                  <span>单价：</span>
                  <span class="price-value">¥{{ pricePerHour }}/小时</span>
                </div>
                <div class="price-item total">
                  <span>预计费用：</span>
                  <span class="price-total">¥{{ calculatedAmount }}</span>
                </div>
              </div>

              <div class="step-actions">
                <el-button size="large" @click="prevStep">
                  <el-icon class="mr-2"><ArrowLeft /></el-icon>
                  上一步
                </el-button>
                <el-button
                  type="primary"
                  size="large"
                  :disabled="!form.appointmentDate || !form.timeSlot"
                  @click="nextStep"
                >
                  下一步
                  <el-icon class="ml-2"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-form>
          </div>

          <!-- 步骤 3: 填写学生信息 -->
          <div v-if="currentStep === 2" class="step-content">
            <h2 class="step-title">填写学生信息</h2>
            <p class="step-description">请填写学生的基本信息和联系方式</p>

            <el-form
              ref="step3FormRef"
              :model="form"
              :rules="step3Rules"
              label-position="top"
              class="step-form"
            >
              <el-form-item label="学生姓名" prop="studentName">
                <el-input
                  v-model="form.studentName"
                  placeholder="请输入学生姓名"
                  size="large"
                />
              </el-form-item>

              <el-form-item label="学生年级" prop="studentGrade">
                <el-input
                  v-model="form.studentGrade"
                  placeholder="请输入学生年级"
                  size="large"
                />
              </el-form-item>

              <el-form-item label="联系方式" prop="studentPhone">
                <el-input
                  v-model="form.studentPhone"
                  placeholder="请输入手机号码"
                  size="large"
                />
              </el-form-item>

              <el-form-item label="备注信息（选填）">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入学习需求、特殊要求等"
                />
              </el-form-item>

              <div class="step-actions">
                <el-button size="large" @click="prevStep">
                  <el-icon class="mr-2"><ArrowLeft /></el-icon>
                  上一步
                </el-button>
                <el-button
                  type="primary"
                  size="large"
                  :loading="submitting"
                  @click="handleSubmit"
                >
                  提交预约
                </el-button>
              </div>
            </el-form>
          </div>
        </el-card>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeacherDetail } from '@/api/teacher'
import { createAppointment } from '@/api/appointment'
import { getTeachingStages } from '@/api/common'
import { ArrowLeft, ArrowRight, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const teacherId = ref(route.params.teacherId)
const teacherInfo = ref(null)
const teachingStages = ref([])
const availableTeachings = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentStep = ref(0)

const steps = [
  { label: '课程信息' },
  { label: '选择时间' },
  { label: '学生信息' }
]

const form = ref({
  teacherId: teacherId.value,
  stageId: null,
  subjectId: null,
  appointmentDate: null,
  timeSlot: null,
  studentName: '',
  studentGrade: '',
  studentPhone: '',
  remark: ''
})

const step1FormRef = ref()
const step2FormRef = ref()
const step3FormRef = ref()

const step1Rules = {
  stageId: [{ required: true, message: '请选择教学阶段', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }]
}

const step2Rules = {
  appointmentDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }]
}

const step3Rules = {
  studentName: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  studentGrade: [{ required: true, message: '请输入学生年级', trigger: 'blur' }],
  studentPhone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const pricePerHour = computed(() => {
  const teaching = availableTeachings.value.find(t => t.subjectId === form.value.subjectId)
  return teaching?.pricePerHour || 0
})

const duration = computed(() => {
  if (!form.value.timeSlot || !Array.isArray(form.value.timeSlot)) return 0
  const [start, end] = form.value.timeSlot
  const startTime = new Date(`2000-01-01 ${start}`)
  const endTime = new Date(`2000-01-01 ${end}`)
  return Math.round((endTime - startTime) / (1000 * 60))
})

const calculatedAmount = computed(() => {
  if (!pricePerHour.value || !duration.value) return '0.00'
  const amount = (pricePerHour.value * duration.value) / 60
  return amount.toFixed(2)
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const loadTeacherDetail = async () => {
  loading.value = true
  try {
    const data = await getTeacherDetail(teacherId.value)
    teacherInfo.value = data
    if (data.teachings) {
      availableTeachings.value = data.teachings
    }
  } catch (error) {
    ElMessage.error('加载教师信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadTeachingStages = async () => {
  try {
    const data = await getTeachingStages()
    teachingStages.value = data || []
  } catch (error) {
    console.error('加载教学阶段失败:', error)
  }
}

const handleStageChange = () => {
  form.value.subjectId = null
  if (teacherInfo.value?.teachings) {
    availableTeachings.value = teacherInfo.value.teachings.filter(
      t => t.stageId === form.value.stageId
    )
  }
}

const handleSubjectChange = () => {
  // 可以在这里添加逻辑
}

const handleDateChange = () => {
  // 可以在这里加载该日期的可用时间段
}

const nextStep = async () => {
  let formRef = null
  let rules = {}

  if (currentStep.value === 0) {
    formRef = step1FormRef.value
    rules = step1Rules
  } else if (currentStep.value === 1) {
    formRef = step2FormRef.value
    rules = step2Rules
  }

  if (!formRef) return

  await formRef.validate((valid) => {
    if (valid) {
      currentStep.value++
    }
  })
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleSubmit = async () => {
  if (!step3FormRef.value) return
  await step3FormRef.value.validate((valid) => {
    if (!valid) return
  })

  if (!form.value.timeSlot || form.value.timeSlot.length !== 2) {
    ElMessage.error('请选择完整的时间段')
    return
  }

  submitting.value = true
  try {
    const [startTime, endTime] = form.value.timeSlot
    const data = {
      ...form.value,
      startTime,
      endTime,
      duration: duration.value,
      pricePerHour: pricePerHour.value,
      totalAmount: parseFloat(calculatedAmount.value)
    }
    await createAppointment(data)
    ElMessage.success('预约成功，等待教师确认')
    router.push('/appointments')
  } catch (error) {
    ElMessage.error(error.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTeacherDetail()
  loadTeachingStages()
})
</script>

<style scoped>
.create-appointment {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.back-button {
  margin-bottom: 24px;
  color: #6b7280;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover {
  color: #667eea;
  transform: translateX(-4px);
}

.appointment-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 教师信息卡片 - 更圆润 */
.teacher-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.teacher-avatar {
  flex-shrink: 0;
}

.teacher-details {
  flex: 1;
}

.teacher-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.teacher-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-text {
  font-size: 14px;
  color: #6b7280;
}

/* 步骤指示器 - 更圆润 */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  flex: 1;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f3f4f6;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.step-item.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  transform: scale(1.1);
}

.step-item.completed .step-number {
  background: #10b981;
  color: #ffffff;
}

.step-label {
  font-size: 14px;
  color: #6b7280;
  transition: color 0.15s;
  white-space: nowrap;
}

.step-item.active .step-label {
  color: #667eea;
  font-weight: 600;
}

.step-item.completed .step-label {
  color: #10b981;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #e5e7eb;
  margin: 0 8px;
  transition: background 0.15s;
}

.step-item.completed .step-line {
  background: #10b981;
}

/* 表单卡片 - 更圆润 */
.form-card {
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.step-content {
  padding: 32px;
}

.step-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.step-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 32px 0;
}

.step-form {
  margin-top: 24px;
}

.full-width-select {
  width: 100%;
}

/* 价格预览 - 更圆润 */
.price-preview {
  margin: 24px 0;
  padding: 20px;
  background: #f9fafb;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #6b7280;
}

.price-item.total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.price-value {
  font-weight: 600;
  color: #1a1a1a;
}

.price-total {
  font-size: 20px;
  color: #667eea;
}

/* 步骤操作按钮 */
.step-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.step-actions .el-button {
  flex: 1;
  max-width: 200px;
}

.ml-2 {
  margin-left: 8px;
}

.mr-2 {
  margin-right: 8px;
}

/* 动画效果 - 缩短时间 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-up-enter-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 响应式设计 */
@media (max-width: 767px) {
  .create-appointment {
    padding: 16px;
  }

  .step-content {
    padding: 24px 20px;
  }

  .step-title {
    font-size: 24px;
  }

  .step-description {
    font-size: 14px;
  }

  .steps-indicator {
    padding: 16px;
    overflow-x: auto;
  }

  .step-label {
    display: none;
  }

  .step-actions {
    flex-direction: column;
  }

  .step-actions .el-button {
    max-width: 100%;
  }

  .teacher-card {
    padding: 20px;
  }
}
</style>
