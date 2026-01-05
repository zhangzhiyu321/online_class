<template>
  <div class="teacher-detail" v-loading="loading">
    <el-button
      type="text"
      :icon="ArrowLeft"
      @click="$router.back()"
      class="back-button"
    >
      返回
    </el-button>

    <div v-if="teacherInfo" class="detail-content">
      <!-- 基本信息卡片 -->
      <el-card class="info-card">
        <div class="teacher-header">
          <div class="avatar-section">
            <el-avatar :src="teacherInfo.avatar" :size="100">
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-tag
              v-if="teacherInfo.onlineStatus === 1"
              type="success"
              size="small"
              class="online-tag"
            >
              在线
            </el-tag>
          </div>
          <div class="info-section">
            <h2 class="teacher-name">{{ teacherInfo.nickname || teacherInfo.realName }}</h2>
            <div class="rating-section">
              <el-rate
                v-model="teacherInfo.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
              <span class="rating-text">({{ teacherInfo.ratingCount }}条评价)</span>
            </div>
            <div class="meta-info">
              <el-tag v-if="teacherInfo.certified" type="success" size="small">已认证</el-tag>
              <span class="teaching-years">教学{{ teacherInfo.teachingYears }}年</span>
            </div>
            <el-button
              type="primary"
              size="large"
              @click="goToCreateAppointment"
              class="appointment-button"
            >
              立即预约
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 详细信息 - 使用标签页分组 -->
      <el-card class="tabs-card">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- 简介与教学 -->
          <el-tab-pane label="简介与教学" name="info">
            <div class="tab-content">
              <!-- 个人简介 -->
              <div class="info-section-item">
                <h3 class="section-title-small">个人简介</h3>
                <p class="introduction">{{ teacherInfo.introduction || '暂无简介' }}</p>
              </div>

              <!-- 教学信息 -->
              <div class="info-section-item">
                <h3 class="section-title-small">教学信息</h3>
                <div class="teaching-info">
                  <div class="info-item">
                    <span class="label">教学阶段：</span>
                    <div class="tags-wrapper">
                      <el-tag
                        v-for="stage in teacherInfo.stages"
                        :key="stage.id"
                        class="info-tag"
                      >
                        {{ stage.name }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="info-item">
                    <span class="label">可教科目：</span>
                    <div class="subjects-list">
                      <div
                        v-for="teaching in teacherInfo.teachings"
                        :key="teaching.id"
                        class="subject-item"
                      >
                        <el-tag type="primary" class="subject-tag">
                          {{ teaching.stageName }}{{ teaching.subjectName }}
                        </el-tag>
                        <span class="price">¥{{ teaching.pricePerHour }}/小时</span>
                      </div>
                    </div>
                  </div>
                  <div v-if="teacherInfo.teachingStyle" class="info-item">
                    <span class="label">教学风格：</span>
                    <p class="teaching-style">{{ teacherInfo.teachingStyle }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 可授课时间 -->
          <el-tab-pane label="可授课时间" name="schedule">
            <div class="tab-content">
              <div class="schedule-section">
                <div v-if="teacherInfo.schedules && teacherInfo.schedules.length > 0" class="schedule-container">
                  <!-- 日历视图 -->
                  <div class="calendar-wrapper">
                    <el-calendar v-model="selectedDate">
                      <template #date-cell="{ data }">
                        <div 
                          class="calendar-cell"
                          :class="{
                            'has-schedule': isAvailableDate(data.day),
                            'selected': isSelectedDate(data.day)
                          }"
                          @click="selectDate(data.day)"
                        >
                          <div class="date-number">{{ data.day.split('-').slice(2).join('-') }}</div>
                          <div v-if="isAvailableDate(data.day)" class="available-dot"></div>
                        </div>
                      </template>
                    </el-calendar>
                  </div>
                  
                  <!-- 选中日期的时间段详情 -->
                  <div v-if="selectedDateStr" class="time-detail">
                    <div class="detail-header">
                      <h3 class="detail-title">{{ formatSelectedDate(selectedDateStr) }}</h3>
                      <span class="detail-subtitle">{{ getWeekdayName(selectedDateStr) }}</span>
                    </div>
                    <div class="time-slots-list">
                      <div
                        v-for="schedule in getSchedulesForDate(selectedDateStr)"
                        :key="schedule.id"
                        class="time-slot-item"
                      >
                        <el-icon class="time-icon"><Clock /></el-icon>
                        <span class="time-range-text">
                          {{ formatTimeRange(schedule.startTime, schedule.endTime) }}
                        </span>
                      </div>
                      <div v-if="getSchedulesForDate(selectedDateStr).length === 0" class="no-time-slots">
                        <el-empty description="该日期暂无可预约时间段" :image-size="80" />
                      </div>
                    </div>
                  </div>
                  <div v-else class="time-detail-placeholder">
                    <el-empty description="请点击日历上的日期查看具体时间段" :image-size="100" />
                  </div>
                </div>
                <el-empty v-else description="暂无可授课时间" />
              </div>
            </div>
          </el-tab-pane>

          <!-- 学生评价 -->
          <el-tab-pane :label="`学生评价 (${reviews.length})`" name="reviews">
            <div class="tab-content">
              <div v-if="reviews.length > 0" class="reviews-list">
                <div
                  v-for="review in reviews"
                  :key="review.id"
                  class="review-item"
                >
                  <div class="review-header">
                    <el-avatar :size="40">{{ review.studentName?.[0] || '学' }}</el-avatar>
                    <div class="review-meta">
                      <span class="review-name">{{ review.studentName || '匿名' }}</span>
                      <el-rate
                        v-model="review.rating"
                        disabled
                        size="small"
                        text-color="#ff9900"
                      />
                      <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                    </div>
                  </div>
                  <p v-if="review.content" class="review-content">{{ review.content }}</p>
                </div>
              </div>
              <el-empty v-else description="暂无评价" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeacherDetail, getTeacherReviews } from '@/api/teacher'
import { ArrowLeft, User, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const teacherId = ref(route.params.id)
const teacherInfo = ref(null)
const reviews = ref([])
const loading = ref(false)
const selectedDate = ref(new Date())
const selectedDateStr = ref('')
const activeTab = ref('info')

const loadTeacherDetail = async () => {
  loading.value = true
  try {
    const data = await getTeacherDetail(teacherId.value)
    teacherInfo.value = data
  } catch (error) {
    ElMessage.error('加载教师信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadReviews = async () => {
  try {
    const data = await getTeacherReviews(teacherId.value, { page: 1, pageSize: 10 })
    reviews.value = data.list || []
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

// 检查日期是否可预约
const isAvailableDate = (date) => {
  if (!teacherInfo.value || !teacherInfo.value.schedules) {
    return false
  }
  
  const dateObj = new Date(date)
  const weekday = dateObj.getDay() === 0 ? 7 : dateObj.getDay() // 转换为1-7（周一到周日）
  const dateStr = dateObj.toISOString().split('T')[0] // yyyy-MM-dd 格式
  
  // 检查是否有该日期的时间段
  return teacherInfo.value.schedules.some(schedule => {
    // 固定周期时间段（每周重复，scheduleType === 1）
    if (schedule.scheduleType === 1 && schedule.weekday === weekday) {
      return true
    }
    
    // 临时时间段（特定日期范围，scheduleType === 2）
    if (schedule.scheduleType === 2 && schedule.startDate && schedule.endDate) {
      const startDate = schedule.startDate.split('T')[0] // 处理可能的日期时间格式
      const endDate = schedule.endDate.split('T')[0]
      // 检查日期是否在范围内
      if (dateStr >= startDate && dateStr <= endDate) {
        // 还需要检查星期几是否匹配（临时时间段也基于星期几）
        const scheduleDate = new Date(schedule.startDate)
        const scheduleWeekday = scheduleDate.getDay() === 0 ? 7 : scheduleDate.getDay()
        return scheduleWeekday === weekday
      }
    }
    
    return false
  })
}

// 检查日期是否被选中
const isSelectedDate = (date) => {
  return selectedDateStr.value === date
}

// 选择日期
const selectDate = (date) => {
  if (isAvailableDate(date)) {
    selectedDateStr.value = date
  } else {
    ElMessage.warning('该日期暂无可预约时间')
  }
}

// 获取选中日期的时间段
const getSchedulesForDate = (date) => {
  if (!teacherInfo.value || !teacherInfo.value.schedules || !date) {
    return []
  }
  
  const dateObj = new Date(date)
  const weekday = dateObj.getDay() === 0 ? 7 : dateObj.getDay() // 转换为1-7（周一到周日）
  const dateStr = dateObj.toISOString().split('T')[0] // yyyy-MM-dd 格式
  
  // 返回该日期的时间段（包括固定周期和临时时间段）
  return teacherInfo.value.schedules
    .filter(schedule => {
      // 固定周期时间段（每周重复）
      if (schedule.scheduleType === 1 && schedule.weekday === weekday) {
        return true
      }
      
      // 临时时间段（特定日期范围）
      if (schedule.scheduleType === 2 && schedule.startDate && schedule.endDate) {
        const startDate = schedule.startDate.split('T')[0]
        const endDate = schedule.endDate.split('T')[0]
        // 检查日期是否在范围内
        if (dateStr >= startDate && dateStr <= endDate) {
          // 检查星期几是否匹配
          const scheduleDate = new Date(schedule.startDate)
          const scheduleWeekday = scheduleDate.getDay() === 0 ? 7 : scheduleDate.getDay()
          return scheduleWeekday === weekday
        }
      }
      
      return false
    })
    .sort((a, b) => {
      // 按开始时间排序
      return a.startTime.localeCompare(b.startTime)
    })
}

// 格式化时间范围
const formatTimeRange = (startTime, endTime) => {
  if (!startTime || !endTime) return ''
  // 从 "HH:mm:ss" 格式提取 "HH:mm"
  const start = startTime.substring(0, 5)
  const end = endTime.substring(0, 5)
  return `${start} - ${end}`
}

// 格式化选中的日期
const formatSelectedDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

// 获取星期几名称
const getWeekdayName = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const weekday = date.getDay()
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[weekday]
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

const goToCreateAppointment = () => {
  router.push(`/appointment/create/${teacherId.value}`)
}

onMounted(() => {
  loadTeacherDetail()
  loadReviews()
})
</script>

<style scoped>
.teacher-detail {
  max-width: var(--container-max-width-desktop, 1200px);
  margin: 0 auto;
  padding: 0 var(--spacing-md, 16px);
}

.back-button {
  margin-bottom: var(--spacing-lg, 20px);
  padding: var(--spacing-sm, 8px) var(--spacing-md, 16px);
  font-size: var(--font-size-body-small, 14px);
  color: #6b7280;
  border-radius: 8px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover {
  color: #3b82f6;
  background: #f3f4f6;
  transform: translateX(-4px);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 20px);
}

.info-card {
  margin-bottom: 0;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.teacher-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.avatar-section {
  position: relative;
  flex-shrink: 0;
}

.online-tag {
  position: absolute;
  bottom: 0;
  right: 0;
  border-radius: 12px;
}

.info-section {
  flex: 1;
}

.teacher-name {
  font-size: var(--font-size-h1, 28px);
  font-weight: 600;
  margin-bottom: var(--spacing-md, 12px);
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.rating-text {
  font-size: var(--font-size-body-small, 14px);
  color: #6b7280;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.teaching-years {
  font-size: var(--font-size-body-small, 14px);
  color: #6b7280;
}

.appointment-button {
  margin-top: 12px;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.appointment-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.tabs-card {
  margin-bottom: 0;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.detail-tabs {
  margin-top: -10px;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
}

.detail-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
}

.tab-content {
  min-height: 200px;
}

.info-section-item {
  margin-bottom: 32px;
}

.info-section-item:last-child {
  margin-bottom: 0;
}

.section-title-small {
  font-size: var(--font-size-h6, 16px);
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 var(--spacing-md, 16px) 0;
  padding-bottom: var(--spacing-md, 12px);
  border-bottom: 2px solid #f3f4f6;
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.introduction {
  line-height: 1.8;
  color: #4b5563;
  white-space: pre-wrap;
  font-size: var(--font-size-body-small, 15px);
}

.teaching-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 600;
  color: #374151;
  min-width: 90px;
  font-size: var(--font-size-body-small, 14px);
  flex-shrink: 0;
}

.info-tag {
  border-radius: 12px;
}

.subjects-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.subject-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.subject-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.subject-tag {
  min-width: 80px;
  border-radius: 12px;
}

.price {
  color: #ef4444;
  font-weight: 600;
  font-size: var(--font-size-h6, 16px);
}

.teaching-style {
  color: #4b5563;
  line-height: 1.8;
  margin: 0;
  font-size: var(--font-size-body-small, 15px);
}

.schedule-section {
  padding: 0;
}

.schedule-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.calendar-wrapper {
  width: 100%;
  background: #ffffff;
  border-radius: 16px;
  border: 2px solid #e5e7eb;
  overflow: hidden;
}

.calendar-wrapper :deep(.el-calendar) {
  border: none;
  background: transparent;
}

.calendar-wrapper :deep(.el-calendar__header) {
  padding: 20px 20px 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: transparent;
}

.calendar-wrapper :deep(.el-calendar__body) {
  padding: 16px 20px 20px 20px;
}

.calendar-wrapper :deep(.el-calendar-table) {
  border: none;
}

.calendar-wrapper :deep(.el-calendar-table thead th) {
  border: none;
  padding: 12px 0;
  color: #6b7280;
  font-weight: 500;
}

.calendar-wrapper :deep(.el-calendar-table td) {
  border: none;
  padding: 4px;
}

.calendar-wrapper :deep(.el-calendar-table .el-calendar-day) {
  padding: 0;
  height: auto;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.calendar-cell:hover {
  background: #f3f4f6;
}

.calendar-cell.has-schedule {
  cursor: pointer;
}

.calendar-cell.has-schedule:hover {
  background: #eff6ff;
}

.calendar-cell.selected {
  background: #3b82f6;
  color: #ffffff;
}

.calendar-cell.selected .date-number {
  color: #ffffff;
  font-weight: 600;
}

.date-number {
  font-size: 14px;
  color: #1a1a1a;
}

.available-dot {
  width: 6px;
  height: 6px;
  background: #10b981;
  border-radius: 50%;
  margin-top: 4px;
}

.calendar-cell.selected .available-dot {
  background: #ffffff;
}

.time-detail {
  background: #f9fafb;
  border-radius: 16px;
  padding: 24px;
  border: 2px solid #e5e7eb;
  width: 100%;
  box-sizing: border-box;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.detail-title {
  font-size: var(--font-size-h4, 20px);
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 var(--spacing-xs, 4px) 0;
}

.detail-subtitle {
  font-size: var(--font-size-body-small, 14px);
  color: #6b7280;
}

.time-slots-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-slot-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px 20px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.time-slot-item:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.time-icon {
  font-size: 20px;
  color: #3b82f6;
}

.time-range-text {
  font-size: var(--font-size-h6, 16px);
  font-weight: 500;
  color: #1a1a1a;
}

.no-time-slots {
  padding: 20px 0;
}

.time-detail-placeholder {
  background: #f9fafb;
  border-radius: 16px;
  padding: 40px 20px;
  border: 2px dashed #d1d5db;
  text-align: center;
  width: 100%;
  box-sizing: border-box;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .schedule-container {
    gap: 16px;
  }
  
  .time-detail {
    padding: 16px;
  }
  
  .detail-title {
    font-size: 18px;
  }
  
  .time-slot-item {
    padding: 12px 16px;
  }
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px;
  background: #f9fafb;
  border-radius: 16px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.review-item:hover {
  background: #f3f4f6;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.review-header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 12px;
}

.review-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-name {
  font-weight: 600;
  color: #1a1a1a;
  font-size: var(--font-size-body-small, 15px);
}

.review-time {
  font-size: var(--font-size-small, 12px);
  color: #9ca3af;
}

.review-content {
  color: #4b5563;
  line-height: 1.6;
  margin: 0;
  font-size: var(--font-size-body-small, 14px);
}

/* 响应式设计 - 平板端 */
@media (min-width: 768px) and (max-width: 1024px) {
  .teacher-detail {
    padding: 0 var(--spacing-md, 16px);
    max-width: var(--container-max-width-tablet, 700px);
  }
  
  .teacher-header {
    gap: var(--spacing-lg, 20px);
  }

  .teacher-name {
    font-size: var(--font-size-h2, 24px);
  }

  .detail-content {
    gap: var(--spacing-md, 16px);
  }
}

/* 响应式设计 - 手机端 */
@media (max-width: 767px) {
  .teacher-detail {
    padding: 0 var(--spacing-md, 12px);
  }

  .back-button {
    margin-bottom: var(--spacing-md, 16px);
    font-size: var(--font-size-body-small, 13px);
    padding: var(--spacing-sm, 8px) var(--spacing-md, 12px);
  }

  .detail-content {
    gap: var(--spacing-md, 16px);
  }

  .teacher-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: var(--spacing-md, 16px);
  }

  .info-section {
    width: 100%;
  }

  .teacher-name {
    font-size: var(--font-size-h3, 20px);
  }

  .appointment-button {
    width: 100%;
    height: var(--button-height, 44px);
    font-size: var(--font-size-button, 16px);
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm, 8px);
  }

  .label {
    min-width: auto;
    margin-bottom: var(--spacing-xs, 4px);
    font-size: var(--font-size-body-small, 13px);
  }

  .tags-wrapper {
    width: 100%;
  }

  .subject-item {
    flex-wrap: wrap;
  }

  .tabs-card {
    border-radius: var(--card-radius-mobile, 12px);
  }

  .detail-tabs :deep(.el-tabs__item) {
    font-size: var(--font-size-body-small, 14px);
    padding: 0 var(--spacing-md, 12px);
  }

  .info-section-item {
    margin-bottom: var(--spacing-xl, 24px);
  }

  .section-title-small {
    font-size: var(--font-size-h6, 15px);
    margin-bottom: var(--spacing-md, 12px);
  }

  .calendar-wrapper {
    border-radius: var(--card-radius-mobile, 12px);
  }

  .calendar-wrapper :deep(.el-calendar__header) {
    padding: var(--spacing-md, 16px) var(--spacing-md, 12px) var(--spacing-md, 12px) var(--spacing-md, 12px);
  }

  .calendar-wrapper :deep(.el-calendar__body) {
    padding: var(--spacing-md, 12px);
  }

  .calendar-wrapper :deep(.el-calendar-table) {
    font-size: var(--font-size-small, 12px);
  }

  .time-detail {
    padding: var(--spacing-md, 16px);
    border-radius: var(--card-radius-mobile, 12px);
  }

  .time-detail-placeholder {
    padding: 30px var(--spacing-md, 16px);
    border-radius: var(--card-radius-mobile, 12px);
  }

  .schedule-container {
    gap: var(--spacing-md, 16px);
  }

  .detail-title {
    font-size: var(--font-size-h5, 18px);
  }

  .time-range-text {
    font-size: var(--font-size-body, 14px);
  }

  .review-item {
    padding: var(--spacing-md, 16px);
  }
}

/* 响应式设计 - 小屏手机 */
@media (max-width: 480px) {
  .teacher-name {
    font-size: var(--font-size-h4, 18px);
  }

  .section-title {
    font-size: var(--font-size-h6, 16px);
  }

  .rating-section {
    justify-content: center;
  }

  .meta-info {
    justify-content: center;
  }
}
</style>

