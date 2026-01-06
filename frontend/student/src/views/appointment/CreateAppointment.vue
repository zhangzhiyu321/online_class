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
      <!-- 教师信息和步骤指示器合并 -->
      <transition name="fade-up" appear>
        <el-card class="header-card" shadow="never">
          <div class="header-content">
            <!-- 教师信息 -->
            <div v-if="teacherInfo" class="teacher-info-compact">
              <el-avatar :src="teacherInfo.avatar" :size="48" class="teacher-avatar" />
              <div class="teacher-details-compact">
                <h3 class="teacher-name-compact">{{ teacherInfo.nickname || teacherInfo.realName }}</h3>
                <div class="teacher-rating-compact">
                  <el-rate
                    v-model="teacherInfo.rating"
                    disabled
                    size="small"
                    text-color="#ff9900"
                  />
                  <span class="rating-text-compact">{{ teacherInfo.rating || 0 }}.0</span>
                </div>
              </div>
            </div>
            
            <!-- 步骤指示器 -->
            <div class="steps-indicator-compact">
              <div
                v-for="(step, index) in steps"
                :key="index"
                class="step-item-compact"
                :class="{ active: currentStep === index, completed: currentStep > index }"
              >
                <div class="step-number-compact">
                  <el-icon v-if="currentStep > index"><Check /></el-icon>
                  <span v-else>{{ index + 1 }}</span>
                </div>
                <span class="step-label-compact">{{ step.label }}</span>
                <div class="step-line-compact" v-if="index < steps.length - 1"></div>
              </div>
            </div>
          </div>
        </el-card>
      </transition>

      <!-- 表单卡片 -->
      <transition name="slide" mode="out-in">
        <el-card :key="currentStep" class="form-card" shadow="never">
          <!-- 步骤 1: 选择课程信息 -->
          <div v-if="currentStep === 0" class="step-content">
            <h2 class="step-title">选择课程信息</h2>

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
                  :placeholder="!form.stageId ? '请先选择教学阶段' : availableTeachings.length === 0 ? '该阶段暂无可选科目' : '请选择科目'"
                  size="large"
                  class="full-width-select"
                  :disabled="!form.stageId || availableTeachings.length === 0"
                  @change="handleSubjectChange"
                >
                  <el-option
                    v-for="teaching in availableTeachings"
                    :key="teaching.id"
                    :label="`${teaching.stageName}${teaching.subjectName} - ¥${teaching.pricePerHour}/小时`"
                    :value="teaching.subjectId"
                  />
                </el-select>
              </el-form-item>

              <div class="step-actions">
                <el-button size="large" @click="$router.back()">取消</el-button>
                <el-button
                  type="primary"
                  size="large"
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
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
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
    
    // 从教师详情中获取可用的教学阶段（去重）
    if (data.stages && data.stages.length > 0) {
      teachingStages.value = data.stages
    } else {
      teachingStages.value = []
    }
    
    // 初始化时不设置 availableTeachings，等用户选择阶段后再筛选
    availableTeachings.value = []
  } catch (error) {
    ElMessage.error('加载教师信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleStageChange = () => {
  // 清空已选择的科目
  form.value.subjectId = null
  
  // 根据选中的阶段筛选可用的教学信息
  if (teacherInfo.value?.teachings && form.value.stageId) {
    availableTeachings.value = teacherInfo.value.teachings.filter(
      t => t.stageId === form.value.stageId
    )
    
    // 如果没有可用的科目，提示用户
    if (availableTeachings.value.length === 0) {
      ElMessage.warning('该教学阶段暂无可选科目')
    }
  } else {
    availableTeachings.value = []
  }
  
  // 清除科目字段的验证状态
  if (step1FormRef.value) {
    step1FormRef.value.clearValidate('subjectId')
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

  if (currentStep.value === 0) {
    formRef = step1FormRef.value
  } else if (currentStep.value === 1) {
    formRef = step2FormRef.value
  }

  if (!formRef) {
    ElMessage.warning('表单未初始化，请刷新页面重试')
    return
  }

  try {
    // 使用 validate 方法进行表单验证，会自动显示字段错误提示
    const valid = await formRef.validate()
    if (valid) {
      // 验证成功，跳转到下一步
      currentStep.value++
    }
    // 如果验证失败，validate 会抛出错误，被 catch 捕获
  } catch (error) {
    // 验证失败时，Element Plus 会自动在对应字段下方显示错误提示
    // 这里可以添加额外的提示信息
    if (currentStep.value === 0) {
      if (!form.value.stageId) {
        ElMessage.warning('请选择教学阶段')
      } else if (!form.value.subjectId) {
        ElMessage.warning('请选择科目')
      }
    } else if (currentStep.value === 1) {
      if (!form.value.appointmentDate) {
        ElMessage.warning('请选择预约日期')
      } else if (!form.value.timeSlot) {
        ElMessage.warning('请选择时间段')
      }
    }
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleSubmit = async () => {
  if (!step3FormRef.value) {
    ElMessage.warning('表单未初始化，请刷新页面重试')
    return
  }

  try {
    await step3FormRef.value.validate((valid) => {
      if (!valid) {
        ElMessage.warning('请完善必填信息后再提交')
        return
      }
    })
  } catch (error) {
    console.error('表单验证错误:', error)
    ElMessage.error('表单验证失败，请检查输入信息')
    return
  }

  if (!form.value.timeSlot || form.value.timeSlot.length !== 2) {
    ElMessage.error('请选择完整的时间段')
    return
  }

  submitting.value = true
  try {
    const [startTime, endTime] = form.value.timeSlot
    
    // 确保日期格式正确
    let appointmentDate = form.value.appointmentDate
    if (appointmentDate instanceof Date) {
      // 如果是Date对象，转换为YYYY-MM-DD格式
      const year = appointmentDate.getFullYear()
      const month = String(appointmentDate.getMonth() + 1).padStart(2, '0')
      const day = String(appointmentDate.getDate()).padStart(2, '0')
      appointmentDate = `${year}-${month}-${day}`
    }
    
    // 构建提交数据
    const data = {
      teacherId: form.value.teacherId,
      stageId: form.value.stageId,
      subjectId: form.value.subjectId,
      appointmentDate: appointmentDate,
      startTime: startTime,
      endTime: endTime,
      duration: duration.value,
      pricePerHour: pricePerHour.value,
      totalAmount: parseFloat(calculatedAmount.value),
      studentName: form.value.studentName,
      studentGrade: form.value.studentGrade,
      studentPhone: form.value.studentPhone,
      remark: form.value.remark || ''
    }
    
    console.log('提交预约数据:', data)
    
    // 提交预约
    const response = await createAppointment(data)
    console.log('预约提交成功:', response)
    
    // 显示成功提示
    ElMessage.success('预约成功，等待教师确认')
    
    // 延迟一下再跳转，确保用户看到成功提示
    setTimeout(() => {
      // 尝试跳转到预约列表页面
      router.push('/appointments').catch(routerError => {
        console.error('路由跳转失败:', routerError)
        // 如果跳转失败，不显示警告，因为预约已经成功了
      })
    }, 500)
  } catch (error) {
    console.error('提交预约失败:', error)
    console.error('错误详情:', {
      status: error?.response?.status,
      data: error?.response?.data,
      message: error?.message
    })
    
    // 获取错误信息
    let errorMessage = '预约失败，请稍后重试'
    
    if (error?.response?.data) {
      // 后端返回的错误信息
      if (error.response.data.message) {
        errorMessage = error.response.data.message
      } else if (error.response.data.error) {
        errorMessage = error.response.data.error
      }
    } else if (error?.message) {
      errorMessage = error.message
    }
    
    // 只有真正的错误才显示错误提示（排除404，因为可能是未实现的接口）
    if (error?.response?.status !== 404) {
      ElMessage.error(errorMessage)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTeacherDetail()
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
  margin-bottom: 20px;
  padding: 8px 16px;
  font-size: 14px;
  color: #6b7280;
  border-radius: 8px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover {
  color: #3b82f6;
  background: #f3f4f6;
  transform: translateX(-4px);
}

.appointment-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 合并的头部卡片 */
.header-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 紧凑的教师信息 */
.teacher-info-compact {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.teacher-avatar {
  flex-shrink: 0;
}

.teacher-details-compact {
  flex: 1;
}

.teacher-name-compact {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.teacher-rating-compact {
  display: flex;
  align-items: center;
  gap: 6px;
}

.rating-text-compact {
  font-size: 13px;
  color: #6b7280;
}

/* 紧凑的步骤指示器 */
.steps-indicator-compact {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
}

.step-item-compact {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  flex: 0 0 auto;
}

.step-number-compact {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f3f4f6;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.step-item-compact.active .step-number-compact {
  background: #3b82f6;
  color: #ffffff;
  transform: scale(1.05);
}

.step-item-compact.completed .step-number-compact {
  background: #10b981;
  color: #ffffff;
}

.step-label-compact {
  font-size: 13px;
  color: #6b7280;
  transition: color 0.15s;
  white-space: nowrap;
}

.step-item-compact.active .step-label-compact {
  color: #3b82f6;
  font-weight: 600;
}

.step-item-compact.completed .step-label-compact {
  color: #10b981;
}

.step-line-compact {
  flex: 1;
  height: 2px;
  background: #e5e7eb;
  margin: 0 6px;
  transition: background 0.15s;
}

.step-item-compact.completed .step-line-compact {
  background: #10b981;
}

/* 表单卡片 - 更圆润 */
.form-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.step-content {
  padding: 24px;
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  letter-spacing: -0.3px;
}

.step-form {
  margin-top: 0;
}

.step-form :deep(.el-form-item__label) {
  font-weight: 500;
  font-size: 14px;
  color: #374151;
  margin-bottom: 8px;
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
  color: #3b82f6;
}

/* 步骤操作按钮 */
.step-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.step-actions .el-button {
  flex: 0 0 auto;
  min-width: 160px;
  width: 160px;
  height: 44px;
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

/* ========== 响应式设计 ========== */

/* 手机端样式（屏幕宽度 <= 767px） */
@media (max-width: 767px) {
  .create-appointment {
    padding: 12px;
    min-height: calc(100vh - 56px);
  }

  .appointment-container {
    gap: 12px;
  }

  .header-card {
    border-radius: 12px;
  }

  .header-content {
    gap: 12px;
  }

  .teacher-info-compact {
    padding-bottom: 12px;
  }

  .teacher-name-compact {
    font-size: var(--font-size-h6, 16px);
  }

  .step-content {
    padding: 20px 16px;
  }

  .step-title {
    font-size: var(--font-size-h5, 18px);
    margin-bottom: var(--spacing-md, 16px);
  }

  .step-label-compact {
    display: none;
  }

  .step-number-compact {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }

  .step-actions {
    flex-direction: row;
    justify-content: center;
    gap: 12px;
    margin-top: 20px;
    padding-top: 16px;
  }

  .step-actions .el-button {
    flex: 0 0 auto;
    min-width: 120px;
    width: 120px;
    height: var(--button-height, 44px);
    font-size: var(--font-size-button, 14px);
  }

  .price-preview {
    padding: 16px;
    margin: 16px 0;
    border-radius: 12px;
  }

  .price-item {
    font-size: 13px;
  }

  .price-item.total {
    font-size: 15px;
  }

  .price-total {
    font-size: 18px;
  }

  .back-button {
    padding: 6px 12px;
    font-size: 13px;
  }
}

/* 平板端样式（屏幕宽度 768px ~ 1024px） */
@media (min-width: 768px) and (max-width: 1024px) {
  .create-appointment {
    padding: 20px;
    max-width: 700px;
  }

  .appointment-container {
    gap: 16px;
  }

  .step-content {
    padding: 24px 20px;
  }

  .step-title {
    font-size: var(--font-size-h4, 20px);
  }

  .step-label-compact {
    font-size: 12px;
  }

  .step-number-compact {
    width: 26px;
    height: 26px;
    font-size: 12px;
  }

  .step-actions .el-button {
    min-width: 150px;
    width: 150px;
    height: 42px;
  }

  .teacher-name-compact {
    font-size: 17px;
  }
}

/* 桌面端样式（屏幕宽度 > 1024px） */
@media (min-width: 1025px) {
  .create-appointment {
    padding: 24px;
    max-width: 800px;
  }

  .appointment-container {
    gap: 20px;
  }

  .step-content {
    padding: 32px;
  }

  .step-title {
    font-size: var(--font-size-h3, 22px);
  }

  .step-actions .el-button {
    min-width: 160px;
    width: 160px;
    height: 44px;
  }

  .header-content {
    gap: 20px;
  }

  .teacher-info-compact {
    padding-bottom: 20px;
  }
}
</style>
