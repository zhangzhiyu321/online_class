import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 通用数据加载 composable
 * 处理数组格式的统一处理逻辑
 */
export function useDataLoader() {
  const loading = ref(false)

  /**
   * 标准化 API 返回的数据格式
   * @param {*} data - API 返回的数据
   * @returns {Array} 标准化后的数组
   */
  const normalizeData = (data) => {
    if (Array.isArray(data)) {
      return data
    } else if (data && Array.isArray(data.list)) {
      return data.list
    } else {
      return []
    }
  }

  /**
   * 加载数据
   * @param {Function} apiCall - API 调用函数
   * @param {Object} options - 配置选项
   * @returns {Promise<Array>} 数据数组
   */
  const loadData = async (apiCall, options = {}) => {
    const { 
      showLoading = true, 
      showError = true, 
      errorMessage = '加载失败',
      onSuccess,
      onError
    } = options

    if (showLoading) {
      loading.value = true
    }

    try {
      const data = await apiCall()
      const normalizedData = normalizeData(data)
      
      if (onSuccess) {
        onSuccess(normalizedData)
      }
      
      return normalizedData
    } catch (error) {
      if (showError) {
        ElMessage.error(errorMessage)
      }
      
      if (onError) {
        onError(error)
      }
      
      console.error(error)
      return []
    } finally {
      if (showLoading) {
        loading.value = false
      }
    }
  }

  return {
    loading,
    normalizeData,
    loadData
  }
}

