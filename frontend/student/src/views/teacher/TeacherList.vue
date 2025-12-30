<template>
  <div class="teacher-list">
    <div class="page-header">
      <h1 class="page-title">找教师</h1>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="filter-bar">
      <el-card class="filter-card">
        <div class="filter-content">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索教师姓名或科目"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>

          <!-- 筛选条件 -->
          <div class="filter-options">
            <el-select
              v-model="filters.stageId"
              placeholder="教学阶段"
              clearable
              style="width: 120px"
              @change="handleFilter"
            >
              <el-option
                v-for="stage in teachingStages"
                :key="stage.id"
                :label="stage.name"
                :value="stage.id"
              />
            </el-select>

            <el-select
              v-model="filters.subjectId"
              placeholder="科目"
              clearable
              style="width: 120px"
              @change="handleFilter"
            >
              <el-option
                v-for="subject in subjects"
                :key="subject.id"
                :label="subject.name"
                :value="subject.id"
              />
            </el-select>

            <el-select
              v-model="filters.minRating"
              placeholder="最低评分"
              clearable
              style="width: 120px"
              @change="handleFilter"
            >
              <el-option label="4.5分以上" :value="4.5" />
              <el-option label="4.0分以上" :value="4.0" />
              <el-option label="3.5分以上" :value="3.5" />
              <el-option label="3.0分以上" :value="3.0" />
            </el-select>

            <el-button @click="resetFilter">重置</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 教师列表 -->
    <div class="teacher-grid">
      <el-card
        v-for="teacher in teacherList"
        :key="teacher.id"
        class="teacher-card"
        shadow="hover"
        @click="goToDetail(teacher.id)"
      >
        <div class="teacher-avatar">
          <el-avatar :src="teacher.avatar" :size="80">
            <el-icon><User /></el-icon>
          </el-avatar>
          <el-tag
            v-if="teacher.onlineStatus === 1"
            type="success"
            size="small"
            class="online-tag"
          >
            在线
          </el-tag>
        </div>
        <div class="teacher-info">
          <h3 class="teacher-name">{{ teacher.nickname || teacher.realName }}</h3>
          <div class="teacher-rating">
            <el-rate
              v-model="teacher.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
            <span class="rating-text">({{ teacher.ratingCount }}条评价)</span>
          </div>
          <div class="teacher-meta">
            <el-tag v-if="teacher.certified" type="success" size="small">已认证</el-tag>
            <span class="teaching-years">教学{{ teacher.teachingYears }}年</span>
          </div>
          <div class="teacher-subjects">
            <el-tag
              v-for="subject in teacher.subjects"
              :key="subject.id"
              size="small"
              class="subject-tag"
            >
              {{ subject.name }}
            </el-tag>
          </div>
          <div class="teacher-price">
            <span class="price-label">课时价格：</span>
            <span class="price-value">¥{{ teacher.minPrice }}/小时起</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && (!teacherList || teacherList.length === 0)" description="暂无教师" />

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTeacherList } from '@/api/teacher'
import { getTeachingStages, getSubjects } from '@/api/common'
import { Search, User } from '@element-plus/icons-vue'

const router = useRouter()

const searchKeyword = ref('')
const teacherList = ref([])
const teachingStages = ref([])
const subjects = ref([])
const loading = ref(false)
const hasMore = ref(true)

const filters = ref({
  stageId: null,
  subjectId: null,
  minRating: null,
  minPrice: null,
  maxPrice: null
})

const pagination = ref({
  page: 1,
  pageSize: 12
})

const loadTeachingStages = async () => {
  try {
    const data = await getTeachingStages()
    // 确保返回的是数组类型
    if (Array.isArray(data)) {
      teachingStages.value = data
    } else if (data && Array.isArray(data.list)) {
      teachingStages.value = data.list
    } else {
      teachingStages.value = []
    }
  } catch (error) {
    console.error('加载教学阶段失败:', error)
    teachingStages.value = []
  }
}

const loadSubjects = async () => {
  try {
    const data = await getSubjects()
    // 确保返回的是数组类型
    if (Array.isArray(data)) {
      subjects.value = data
    } else if (data && Array.isArray(data.list)) {
      subjects.value = data.list
    } else {
      subjects.value = []
    }
  } catch (error) {
    console.error('加载科目失败:', error)
    subjects.value = []
  }
}

const loadTeacherList = async (reset = false) => {
  if (loading.value) return

  if (reset) {
    pagination.value.page = 1
    teacherList.value = []
  }

  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value || undefined,
      ...filters.value
    }

    // 移除空值
    Object.keys(params).forEach((key) => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })

    const data = await getTeacherList(params)
    // 确保返回的是数组类型
    let list = []
    if (Array.isArray(data)) {
      list = data
    } else if (data && Array.isArray(data.list)) {
      list = data.list
    }
    const total = (data && typeof data.total === 'number') ? data.total : list.length

    if (reset) {
      teacherList.value = list
    } else {
      if (Array.isArray(teacherList.value)) {
        teacherList.value.push(...list)
      } else {
        teacherList.value = list
      }
    }

    hasMore.value = Array.isArray(teacherList.value) && teacherList.value.length < total
  } catch (error) {
    console.error('加载教师列表失败:', error)
    if (reset) {
      teacherList.value = []
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadTeacherList(true)
}

const handleFilter = () => {
  loadTeacherList(true)
}

const resetFilter = () => {
  searchKeyword.value = ''
  filters.value = {
    stageId: null,
    subjectId: null,
    minRating: null,
    minPrice: null,
    maxPrice: null
  }
  loadTeacherList(true)
}

const loadMore = () => {
  pagination.value.page++
  loadTeacherList(false)
}

const goToDetail = (id) => {
  router.push(`/teacher/${id}`)
}

onMounted(() => {
  loadTeachingStages()
  loadSubjects()
  loadTeacherList(true)
})
</script>

<style scoped>
.teacher-list {
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

.filter-bar {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-box {
  width: 100%;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.teacher-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.teacher-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.teacher-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.teacher-avatar {
  text-align: center;
  margin-bottom: 16px;
  position: relative;
}

.online-tag {
  position: absolute;
  top: 0;
  right: calc(50% - 40px);
}

.teacher-info {
  text-align: center;
}

.teacher-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
}

.teacher-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.rating-text {
  font-size: 12px;
  color: #909399;
}

.teacher-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.teaching-years {
  font-size: 12px;
  color: #909399;
}

.teacher-subjects {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  margin-bottom: 12px;
}

.subject-tag {
  margin: 0;
}

.teacher-price {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.price-label {
  font-size: 12px;
  color: #909399;
}

.price-value {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  margin-left: 4px;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .teacher-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-options .el-select,
  .filter-options .el-button {
    width: 100%;
  }

  .page-title {
    font-size: 20px;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .teacher-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

