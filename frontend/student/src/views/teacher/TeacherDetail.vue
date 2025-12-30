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

      <!-- 详细信息 -->
      <div class="detail-sections">
        <!-- 个人简介 -->
        <el-card class="section-card">
          <template #header>
            <span class="section-title">个人简介</span>
          </template>
          <p class="introduction">{{ teacherInfo.introduction || '暂无简介' }}</p>
        </el-card>

        <!-- 教学信息 -->
        <el-card class="section-card">
          <template #header>
            <span class="section-title">教学信息</span>
          </template>
          <div class="teaching-info">
            <div class="info-item">
              <span class="label">教学阶段：</span>
              <el-tag
                v-for="stage in teacherInfo.stages"
                :key="stage.id"
                class="info-tag"
              >
                {{ stage.name }}
              </el-tag>
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
                    {{ teaching.subjectName }}
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
        </el-card>

        <!-- 学历认证 -->
        <el-card v-if="teacherInfo.certifications && teacherInfo.certifications.length > 0" class="section-card">
          <template #header>
            <span class="section-title">学历认证</span>
          </template>
          <div class="certifications">
            <div
              v-for="cert in teacherInfo.certifications"
              :key="cert.id"
              class="cert-item"
            >
              <el-image
                :src="cert.certificateImage"
                :preview-src-list="[cert.certificateImage]"
                fit="cover"
                class="cert-image"
              />
              <div class="cert-info">
                <p><strong>学校：</strong>{{ cert.schoolName }}</p>
                <p><strong>专业：</strong>{{ cert.major }}</p>
                <p><strong>学历：</strong>{{ cert.degree }}</p>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 空闲时间表 -->
        <el-card class="section-card">
          <template #header>
            <span class="section-title">可授课时间</span>
          </template>
          <div class="schedule-section">
            <el-calendar v-model="selectedDate">
              <template #date-cell="{ data }">
                <div class="calendar-cell">
                  <div class="date-number">{{ data.day.split('-').slice(2).join('-') }}</div>
                  <div v-if="isAvailableDate(data.day)" class="available-mark">可预约</div>
                </div>
              </template>
            </el-calendar>
          </div>
        </el-card>

        <!-- 学生评价 -->
        <el-card class="section-card">
          <template #header>
            <span class="section-title">学生评价 ({{ reviews.length }})</span>
          </template>
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
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeacherDetail, getTeacherReviews } from '@/api/teacher'
import { ArrowLeft, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const teacherId = ref(route.params.id)
const teacherInfo = ref(null)
const reviews = ref([])
const loading = ref(false)
const selectedDate = ref(new Date())

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

const isAvailableDate = (date) => {
  // TODO: 根据实际的时间表数据判断
  return true
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.back-button {
  margin-bottom: 20px;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover {
  background: #f3f4f6;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 12px;
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
  font-size: 14px;
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
  font-size: 14px;
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

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  margin-bottom: 0;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.introduction {
  line-height: 1.8;
  color: #4b5563;
  white-space: pre-wrap;
  font-size: 15px;
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
  flex-wrap: wrap;
}

.label {
  font-weight: 600;
  color: #374151;
  min-width: 100px;
  font-size: 15px;
}

.info-tag {
  margin-right: 8px;
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
  font-size: 16px;
}

.teaching-style {
  color: #4b5563;
  line-height: 1.8;
  margin: 0;
  font-size: 15px;
}

.certifications {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cert-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 16px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.cert-item:hover {
  background: #f3f4f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.cert-image {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  flex-shrink: 0;
  object-fit: cover;
}

.cert-info {
  flex: 1;
}

.cert-info p {
  margin: 8px 0;
  color: #4b5563;
  font-size: 14px;
}

.schedule-section {
  padding: 12px 0;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.date-number {
  font-size: 14px;
}

.available-mark {
  font-size: 10px;
  color: #10b981;
  margin-top: 4px;
  font-weight: 500;
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
  font-size: 15px;
}

.review-time {
  font-size: 12px;
  color: #9ca3af;
}

.review-content {
  color: #4b5563;
  line-height: 1.6;
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .teacher-detail {
    padding: 0 12px;
  }
  
  .teacher-header {
    gap: 20px;
  }
}

@media (max-width: 767px) {
  .teacher-detail {
    padding: 0 12px;
  }

  .teacher-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 16px;
  }

  .info-section {
    width: 100%;
  }

  .teacher-name {
    font-size: 24px;
  }

  .appointment-button {
    width: 100%;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .label {
    min-width: auto;
  }

  .cert-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .cert-image {
    width: 100%;
    max-width: 200px;
    height: auto;
    aspect-ratio: 1;
  }

  .subject-item {
    flex-wrap: wrap;
  }

  .detail-sections {
    gap: 16px;
  }

  .section-card {
    border-radius: 16px;
  }
}

@media (max-width: 480px) {
  .teacher-name {
    font-size: 20px;
  }

  .section-title {
    font-size: 16px;
  }

  .rating-section {
    justify-content: center;
  }

  .meta-info {
    justify-content: center;
  }
}
</style>

