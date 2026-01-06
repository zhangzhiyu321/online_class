/**
 * 列表页面通用 composable
 * 统一处理列表页面的通用逻辑：搜索、过滤、加载等
 */
import { ref, computed } from 'vue'
import { normalizeApiData, createSearchFilter } from '@/utils/dataHelper'
import { ElMessage } from 'element-plus'

export function useListPage(options = {}) {
  const {
    apiCall, // API调用函数
    searchFields = [], // 搜索字段
    defaultParams = {} // 默认参数
  } = options

  // 响应式数据
  const list = ref([])
  const searchKeyword = ref('')
  const loading = ref(false)
  const statusFilter = ref('')

  /**
   * 加载列表数据
   * @param {Object} params - 额外的请求参数
   */
  const loadList = async (params = {}) => {
    if (loading.value) return

    loading.value = true
    try {
      const requestParams = {
        ...defaultParams,
        ...params
      }

      // 如果有状态筛选，添加到参数中
      if (statusFilter.value) {
        requestParams.status = statusFilter.value
      }

      // 如果有搜索关键词，添加到参数中（后端搜索）
      // 如果不需要后端搜索，可以注释掉这部分
      // if (searchKeyword.value) {
      //   requestParams.keyword = searchKeyword.value
      // }

      const data = await apiCall(requestParams)
      list.value = normalizeApiData(data)
    } catch (error) {
      ElMessage.error('加载数据失败')
      console.error(error)
      list.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 过滤后的列表（前端搜索）
   */
  const filteredList = computed(() => {
    if (searchFields.length > 0) {
      return createSearchFilter(list.value, searchKeyword.value, searchFields)
    }
    return list.value
  })

  /**
   * 处理搜索
   */
  const handleSearch = () => {
    // 前端过滤已通过 computed 实现，这里可以添加其他逻辑
  }

  /**
   * 重置筛选
   */
  const resetFilter = () => {
    searchKeyword.value = ''
    statusFilter.value = ''
    loadList()
  }

  return {
    list,
    searchKeyword,
    loading,
    statusFilter,
    filteredList,
    loadList,
    handleSearch,
    resetFilter
  }
}

