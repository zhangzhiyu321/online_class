<!--
  教师列表页面
  功能：
  1. 搜索教师（按姓名或科目）
  2. 筛选教师（教学阶段、科目、最低评分）
  3. 显示教师列表（网格布局）
  4. 分页加载更多
  5. 点击教师卡片跳转到详情页
-->
<template>
  <div class="teacher-list list-page-container">
    <!-- 搜索和筛选栏 -->
    <div class="filter-bar">
      <el-card class="list-page-filter-card">
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
            </el-input>
            <el-button @click="handleSearch" class="search-button">搜索</el-button>
          </div>

          <!-- 筛选条件 -->
          <div class="filter-options">
            <!-- 教学阶段下拉框 -->
            <el-dropdown
              trigger="click"
              placement="bottom-start"
              popper-class="user-dropdown-popper"
              @visible-change="(visible) => dropdownOpen.stage = visible"
              @command="handleStageSelect"
            >
              <div class="filter-dropdown-trigger" :class="{ active: dropdownOpen.stage }">
                <span>{{ selectedStageText }}</span>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                <el-icon
                  v-if="filters.stageId"
                  class="clear-icon"
                  @click.stop="clearStage"
                >
                  <Close />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu">
                  <el-dropdown-item
                    v-for="stage in teachingStages"
                    :key="stage.id"
                    :command="stage.id"
                    :class="{ 'is-selected': filters.stageId === stage.id }"
                  >
                    {{ stage.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 科目下拉框 -->
            <el-dropdown
              trigger="click"
              placement="bottom-start"
              popper-class="user-dropdown-popper"
              @visible-change="(visible) => dropdownOpen.subject = visible"
              @command="handleSubjectSelect"
            >
              <div class="filter-dropdown-trigger" :class="{ active: dropdownOpen.subject }">
                <span>{{ selectedSubjectText }}</span>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                <el-icon
                  v-if="filters.subjectId"
                  class="clear-icon"
                  @click.stop="clearSubject"
                >
                  <Close />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu">
                  <el-dropdown-item
                    v-for="subject in subjects"
                    :key="subject.id"
                    :command="subject.id"
                    :class="{ 'is-selected': filters.subjectId === subject.id }"
                  >
                    {{ subject.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 最低评分下拉框 -->
            <el-dropdown
              trigger="click"
              placement="bottom-start"
              popper-class="user-dropdown-popper"
              @visible-change="(visible) => dropdownOpen.rating = visible"
              @command="handleRatingSelect"
            >
              <div class="filter-dropdown-trigger" :class="{ active: dropdownOpen.rating }">
                <span>{{ selectedRatingText }}</span>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
                <el-icon
                  v-if="filters.minRating"
                  class="clear-icon"
                  @click.stop="clearRating"
                >
                  <Close />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu">
                  <el-dropdown-item
                    v-for="option in ratingOptions"
                    :key="option.value"
                    :command="option.value"
                    :class="{ 'is-selected': filters.minRating === option.value }"
                  >
                    {{ option.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

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

    <!-- 加载更多提示 -->
    <div v-if="hasMore && loading" class="load-more">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>正在加载更多...</span>
    </div>
    <div v-else-if="!hasMore && teacherList.length > 0" class="load-more">
      <span class="no-more-text">没有更多了</span>
    </div>
  </div>
</template>

<script setup>
/**
 * 教师列表页面 - Script Setup
 * 使用 Composition API 实现组件逻辑
 */

import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getTeacherList } from '@/api/teacher'
import { getTeachingStages, getSubjects } from '@/api/common'
import { Search, User, ArrowDown, Close, Loading } from '@element-plus/icons-vue'
import { normalizeApiData } from '@/utils/dataHelper'

// ========== 路由和基础设置 ==========
const router = useRouter() // Vue Router 实例，用于页面跳转

// ========== 响应式数据定义 ==========

// 搜索关键词（用户输入的搜索文本）
const searchKeyword = ref('')

// 教师列表数据
const teacherList = ref([])

// 教学阶段选项列表（用于筛选下拉框）
const teachingStages = ref([])

// 科目选项列表（用于筛选下拉框）
const subjects = ref([])

// 加载状态标识（是否正在请求数据）
const loading = ref(false)

// 是否还有更多数据（用于判断是否显示"加载更多"按钮）
const hasMore = ref(true)

// 筛选条件对象
// stageId: 教学阶段ID
// subjectId: 科目ID
// minRating: 最低评分
// minPrice: 最低价格（预留，暂未使用）
// maxPrice: 最高价格（预留，暂未使用）
const filters = ref({
  stageId: null,
  subjectId: null,
  minRating: null,
  minPrice: null,
  maxPrice: null
})

// 分页参数
// page: 当前页码
// pageSize: 每页显示数量
const pagination = ref({
  page: 1,
  pageSize: 6
})

// 下拉框打开状态管理（用于控制下拉框激活样式）
// stage: 教学阶段下拉框是否打开
// subject: 科目下拉框是否打开
// rating: 评分下拉框是否打开
const dropdownOpen = ref({
  stage: false,
  subject: false,
  rating: false
})

// ========== 计算属性 ==========

/**
 * 获取选中的教学阶段显示文本
 * 如果未选择，返回默认文本"教学阶段"
 * 如果已选择，返回对应的阶段名称
 */
const selectedStageText = computed(() => {
  if (!filters.value.stageId) return '教学阶段'
  const stage = teachingStages.value.find(s => s.id === filters.value.stageId)
  return stage ? stage.name : '教学阶段'
})

/**
 * 获取选中的科目显示文本
 * 如果未选择，返回默认文本"科目"
 * 如果已选择，返回对应的科目名称
 */
const selectedSubjectText = computed(() => {
  if (!filters.value.subjectId) return '科目'
  const subject = subjects.value.find(s => s.id === filters.value.subjectId)
  return subject ? subject.name : '科目'
})

/**
 * 获取选中的最低评分显示文本
 * 如果未选择，返回默认文本"最低评分"
 * 如果已选择，返回格式化的评分文本（如"4.5分以上"）
 */
const selectedRatingText = computed(() => {
  if (!filters.value.minRating) return '最低评分'
  return `${filters.value.minRating}分以上`
})

// 最低评分选项列表（用于评分筛选下拉框）
const ratingOptions = [
  { label: '4.5分以上', value: 4.5 },
  { label: '4.0分以上', value: 4.0 },
  { label: '3.5分以上', value: 3.5 },
  { label: '3.0分以上', value: 3.0 }
]

// ========== 数据加载方法 ==========

/**
 * 加载教学阶段列表
 * 从API获取所有可用的教学阶段选项，用于筛选下拉框
 */
const loadTeachingStages = async () => {
  try {
    const data = await getTeachingStages()
    teachingStages.value = normalizeApiData(data)
  } catch (error) {
    console.error('加载教学阶段失败:', error)
    teachingStages.value = []
  }
}

/**
 * 加载科目列表
 * 从API获取所有可用的科目选项，用于筛选下拉框
 */
const loadSubjects = async () => {
  try {
    const data = await getSubjects()
    subjects.value = normalizeApiData(data)
  } catch (error) {
    console.error('加载科目失败:', error)
    subjects.value = []
  }
}

/**
 * 加载教师列表
 * @param {boolean} reset - 是否重置列表（true: 重置并加载第一页, false: 追加加载当前页）
 */
const loadTeacherList = async (reset = false) => {
  // 如果正在加载中，直接返回，避免重复请求
  if (loading.value) return

  // 如果是重置模式，重置分页和列表数据
  if (reset) {
    pagination.value.page = 1
    teacherList.value = []
  }

  loading.value = true
  try {
    // 构建请求参数
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value || undefined, // 搜索关键词，为空时不传该参数
      ...filters.value // 展开筛选条件
    }

    // 移除空值参数（null、undefined、空字符串），避免不必要的参数传递
    Object.keys(params).forEach((key) => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })

    // 调用API获取教师列表
    const data = await getTeacherList(params)
    
    // 标准化API返回数据
    const list = normalizeApiData(data)
    // 获取总数（用于判断是否还有更多数据）
    const total = (data && typeof data.total === 'number') ? data.total : list.length

    // 根据reset参数决定是重置列表还是追加数据
    if (reset) {
      teacherList.value = list
    } else {
      // 追加模式：将新数据追加到现有列表
      if (Array.isArray(teacherList.value)) {
        teacherList.value.push(...list)
      } else {
        teacherList.value = list
      }
    }

    // 判断是否还有更多数据（当前列表长度小于总数表示还有更多）
    hasMore.value = Array.isArray(teacherList.value) && teacherList.value.length < total
  } catch (error) {
    console.error('加载教师列表失败:', error)
    // 出错时，如果是重置模式，清空列表
    if (reset) {
      teacherList.value = []
    }
  } finally {
    loading.value = false
  }
}

// ========== 事件处理方法 ==========

/**
 * 处理搜索事件
 * 当用户点击搜索按钮或按回车键时触发，重置并重新加载列表
 */
const handleSearch = () => {
  loadTeacherList(true)
}

/**
 * 处理教学阶段选择事件
 * @param {number} stageId - 选中的教学阶段ID
 * 支持切换选择（如果点击已选中的项，则取消选择）
 */
const handleStageSelect = (stageId) => {
  filters.value.stageId = stageId === filters.value.stageId ? null : stageId
  handleFilter()
}

/**
 * 处理科目选择事件
 * @param {number} subjectId - 选中的科目ID
 * 支持切换选择（如果点击已选中的项，则取消选择）
 */
const handleSubjectSelect = (subjectId) => {
  filters.value.subjectId = subjectId === filters.value.subjectId ? null : subjectId
  handleFilter()
}

/**
 * 处理最低评分选择事件
 * @param {number} rating - 选中的最低评分值
 * 支持切换选择（如果点击已选中的项，则取消选择）
 */
const handleRatingSelect = (rating) => {
  filters.value.minRating = rating === filters.value.minRating ? null : rating
  handleFilter()
}

/**
 * 清除教学阶段筛选
 * 点击筛选下拉框中的清除图标时触发
 */
const clearStage = () => {
  filters.value.stageId = null
  handleFilter()
}

/**
 * 清除科目筛选
 * 点击筛选下拉框中的清除图标时触发
 */
const clearSubject = () => {
  filters.value.subjectId = null
  handleFilter()
}

/**
 * 清除最低评分筛选
 * 点击筛选下拉框中的清除图标时触发
 */
const clearRating = () => {
  filters.value.minRating = null
  handleFilter()
}

/**
 * 处理筛选条件变化
 * 当任何筛选条件发生变化时，重新加载列表（重置模式）
 */
const handleFilter = () => {
  loadTeacherList(true)
}

/**
 * 重置所有筛选条件
 * 清空搜索关键词和所有筛选条件，重新加载列表
 */
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

/**
 * 加载更多数据
 * 自动触发，加载下一页数据并追加到列表
 */
const loadMore = () => {
  // 如果正在加载或没有更多数据，直接返回
  if (loading.value || !hasMore.value) return
  
  pagination.value.page++
  loadTeacherList(false) // 追加模式，不清空现有数据
}

/**
 * 处理滚动事件
 * 当滚动接近底部时自动加载更多数据
 * 使用节流优化性能，提前200px开始加载，避免顿挫感
 */
const handleScroll = () => {
  // 如果正在加载或没有更多数据，直接返回
  if (loading.value || !hasMore.value) return

  // 获取滚动相关信息
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
  const windowHeight = window.innerHeight || document.documentElement.clientHeight
  const documentHeight = document.documentElement.scrollHeight || document.body.scrollHeight

  // 计算距离底部的距离
  const distanceToBottom = documentHeight - (scrollTop + windowHeight)

  // 当距离底部小于200px时，开始加载更多（提前加载，避免顿挫感）
  if (distanceToBottom < 200) {
    loadMore()
  }
}

/**
 * 跳转到教师详情页
 * @param {number} id - 教师ID
 */
const goToDetail = (id) => {
  router.push(`/teacher/${id}`)
}

// ========== 生命周期钩子 ==========

/**
 * 组件挂载时执行
 * 初始化加载教学阶段、科目和教师列表数据
 * 添加滚动事件监听，实现自动加载更多
 */
onMounted(() => {
  loadTeachingStages()
  loadSubjects()
  loadTeacherList(true)
  // 添加滚动事件监听，使用 passive 选项优化性能
  window.addEventListener('scroll', handleScroll, { passive: true })
})

/**
 * 组件卸载时执行
 * 移除滚动事件监听器，避免内存泄漏
 */
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* ========== 页面容器样式 ========== */

/* 教师列表页面容器
 * 限制最大宽度，居中显示
 */
/* 筛选栏容器 */
.filter-bar {
  margin-bottom: var(--spacing-lg, 20px);
}

/* 筛选内容容器（垂直布局） */
.filter-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 16px);
}

/* 搜索框容器（水平布局，输入框和按钮并排显示） */
.search-box {
  display: flex;
  gap: 12px;
  width: 100%;
  align-items: center;
}

/* 搜索输入框（占据剩余空间） */
.search-box .el-input {
  flex: 1;
}

/* 搜索输入框边框圆角统一为 12px，统一高度 */
.search-box .el-input__wrapper {
  border-radius: 12px !important;
  min-height: 36px !important;
}

/* 搜索输入框内部元素对齐 */
.search-box :deep(.el-input__inner) {
  height: 36px;
  line-height: 36px;
}

/* 搜索按钮样式（不缩放，圆角统一为 12px，统一高度） */
.search-button {
  flex-shrink: 0;
  border-radius: 12px !important;
  height: 36px !important;
  padding: 0 16px;
}

/* 筛选选项容器（水平布局，所有筛选下拉框和重置按钮并排显示） */
.filter-options {
  display: flex;
  flex-wrap: nowrap;
  gap: 12px;
  align-items: center;
}

/* 筛选下拉框触发器样式（覆盖 main.css 中的通用样式）
 * 用于教学阶段、科目、最低评分三个下拉框
 */
.filter-dropdown-trigger {
  min-width: 120px;
  height: 36px;
  padding: 0 12px;
  line-height: 36px;
  position: relative;
}

/* 筛选选项中的重置按钮统一高度 */
.filter-options .el-button {
  height: 36px !important;
  padding: 0 16px;
}

/* ========== 教师列表网格样式 ========== */

/* 教师卡片网格容器
 * 使用 CSS Grid 布局，自动填充列，每列最小宽度 280px
 */
.teacher-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg, 20px);
  margin-bottom: var(--spacing-lg, 20px);
}

/* 教师卡片样式（可点击，悬停时有上浮效果） */
.teacher-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

/* 教师卡片悬停效果（上浮 4px，增加阴影） */
.teacher-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 移动端禁用悬停效果，避免布局问题 */
@media (max-width: 767px) {
  .teacher-card:hover {
    transform: none;
    box-shadow: none;
  }

  .teacher-card:active {
    background-color: #f5f7fa;
  }
}

/* 教师头像容器（居中，相对定位以便放置在线标签） */
.teacher-avatar {
  text-align: center;
  margin-bottom: 16px;
  position: relative;
}

/* 在线状态标签（绝对定位在头像右上角）- 禁用动画，立即显示 */
.online-tag {
  position: absolute;
  top: 0;
  right: calc(50% - 40px);
  transition: none !important;
  animation: none !important;
  opacity: 1 !important;
}

/* 教师信息容器（居中布局） */
.teacher-info {
  text-align: center;
}

/* 教师姓名样式 */
.teacher-name {
  font-size: var(--font-size-h5, 18px);
  font-weight: bold;
  margin-bottom: var(--spacing-sm, 8px);
  color: #303133;
}

/* 教师评分区域（评分星级 + 评价数量） */
.teacher-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

/* 评价数量文字样式 */
.rating-text {
  font-size: var(--font-size-small, 12px);
  color: #909399;
}

/* 教师元信息区域（认证标签 + 教学年限） */
.teacher-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

/* 教学年限文字样式 */
.teaching-years {
  font-size: var(--font-size-small, 12px);
  color: #909399;
}

/* 教师科目标签容器（标签可换行，居中显示） */
.teacher-subjects {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  margin-bottom: 12px;
}

/* 科目标签样式（移除默认margin）- 禁用动画，立即显示 */
.subject-tag {
  margin: 0;
  transition: none !important;
  animation: none !important;
  opacity: 1 !important;
}

/* 教师价格区域（顶部有分隔线） */
.teacher-price {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

/* 价格标签文字样式 */
.price-label {
  font-size: 12px;
  color: #909399;
}

/* 价格数值样式（大号红色粗体） */
.price-value {
  font-size: var(--font-size-h5, 18px);
  font-weight: bold;
  color: #f56c6c;
  margin-left: var(--spacing-xs, 4px);
}

/* 加载更多提示容器（居中） */
.load-more {
  text-align: center;
  margin-top: var(--spacing-lg, 20px);
  margin-bottom: var(--spacing-lg, 20px);
  padding: var(--spacing-lg, 20px) 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm, 8px);
  color: #909399;
  font-size: var(--font-size-body-small, 14px);
}

/* 加载图标动画 */
.loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 没有更多文本样式 */
.no-more-text {
  color: #c0c4cc;
  font-size: var(--font-size-body-small, 14px);
}

/* ========== 响应式设计 ========== */

/* 移动端通用样式（屏幕宽度 <= 767px） */
@media (max-width: 767px) {
  .teacher-list {
    max-width: 100%;
  }

  .filter-bar {
    margin-bottom: var(--spacing-md, 16px);
  }

  .filter-content {
    gap: var(--spacing-sm, 12px);
  }

  .search-box {
    flex-direction: row;
    justify-content: space-between;
    gap: var(--spacing-sm, 8px);
  }

  .search-button {
    margin-left: auto;
    font-size: var(--font-size-button, 14px);
    padding: 0 12px;
  }

  .filter-dropdown-trigger {
    width: 100%;
    min-width: 0;
    font-size: var(--font-size-body-small, 13px);
    padding: 0 10px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .filter-options .el-button {
    width: 100%;
    font-size: var(--font-size-button, 14px);
    justify-content: center;
    padding: 0 12px;
  }

  /* 移动端教师卡片：横向布局 */
  .teacher-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm, 12px);
  }

  .teacher-card {
    padding: var(--spacing-sm, 12px) !important;
  }

  .teacher-card :deep(.el-card__body) {
    padding: 0 !important;
    display: flex;
    align-items: flex-start;
    gap: var(--spacing-md, 16px);
  }

  .teacher-avatar {
    flex-shrink: 0;
    margin-bottom: 0;
    text-align: left;
  }

  .teacher-avatar :deep(.el-avatar) {
    width: 60px !important;
    height: 60px !important;
  }

  .online-tag {
    top: -2px;
    right: -2px;
    font-size: 10px;
    padding: 2px 4px;
  }

  .teacher-info {
    flex: 1;
    text-align: left;
    min-width: 0;
    padding-left: 4px;
  }

  .teacher-name {
    font-size: var(--font-size-body, 15px);
    font-weight: 600;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .teacher-rating {
    justify-content: flex-start;
    margin-bottom: 6px;
    gap: 4px;
  }

  .teacher-rating :deep(.el-rate) {
    --el-rate-height: 14px;
  }

  .teacher-rating :deep(.el-rate__item) {
    font-size: 14px;
  }

  .rating-text {
    font-size: 11px;
  }

  .teacher-meta {
    justify-content: flex-start;
    margin-bottom: 6px;
    gap: 6px;
  }

  .teaching-years {
    font-size: 11px;
  }

  .teacher-subjects {
    justify-content: flex-start;
    margin-bottom: 6px;
    gap: 4px;
  }

  .subject-tag {
    font-size: 11px;
    padding: 2px 6px;
    height: auto;
    line-height: 1.4;
  }

  .teacher-price {
    margin-top: 6px;
    padding-top: 6px;
    border-top: 1px solid #ebeef5;
    display: flex;
    align-items: baseline;
    gap: 4px;
  }

  .price-label {
    font-size: 11px;
  }

  .price-value {
    font-size: var(--font-size-body, 15px);
    font-weight: 600;
    margin-left: 0;
  }
}

/* 小屏手机（屏幕宽度 <= 359px）- 筛选选项单列布局 */
@media (max-width: 359px) {
  .filter-options {
    display: grid;
    grid-template-columns: 1fr;
    gap: var(--spacing-sm, 10px);
    align-items: stretch;
  }
}

/* 稍宽移动端（屏幕宽度 360px ~ 767px）- 筛选选项两列布局 */
@media (min-width: 360px) and (max-width: 767px) {
  .filter-options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-sm, 10px);
    align-items: stretch;
  }

  .filter-options .el-button:last-child {
    grid-column: 1 / -1;
  }

  .filter-dropdown-trigger {
    font-size: var(--font-size-body-small, 14px);
  }
}

/* 平板样式（屏幕宽度 768px ~ 1024px） */
@media (min-width: 768px) and (max-width: 1024px) {
  .teacher-list {
    max-width: var(--container-max-width-tablet, 700px);
  }

  .teacher-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-lg, 20px);
  }

  .filter-content {
    gap: var(--spacing-md, 16px);
  }

  .filter-dropdown-trigger {
    font-size: var(--font-size-body-small, 14px);
  }
}

/* 电脑端样式（屏幕宽度 >= 1025px） */
@media (min-width: 1025px) {
  .teacher-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: var(--spacing-xl, 24px);
  }
}
</style>

