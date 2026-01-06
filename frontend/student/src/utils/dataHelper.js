/**
 * 数据辅助工具函数
 * 统一处理API返回数据的格式转换
 */

/**
 * 标准化 API 返回的数据格式
 * 处理不同格式的API返回数据（可能是数组，也可能是包含list属性的对象）
 * @param {*} data - API 返回的数据
 * @returns {Array} 标准化后的数组
 */
export const normalizeApiData = (data) => {
  if (Array.isArray(data)) {
    return data
  } else if (data && Array.isArray(data.list)) {
    return data.list
  } else {
    return []
  }
}

/**
 * 创建列表过滤函数
 * @param {Array} list - 要过滤的列表
 * @param {string} keyword - 搜索关键词
 * @param {Function} filterFn - 自定义过滤函数，接收(item, keyword)参数
 * @returns {Array} 过滤后的列表
 */
export const createListFilter = (list, keyword, filterFn) => {
  if (!Array.isArray(list)) {
    return []
  }
  if (!keyword || !keyword.trim()) {
    return list
  }
  const lowerKeyword = keyword.toLowerCase()
  return list.filter(item => filterFn(item, lowerKeyword))
}

/**
 * 创建通用列表搜索过滤函数（用于预约、支付、聊天等列表）
 * @param {Array} list - 要过滤的列表
 * @param {string} keyword - 搜索关键词
 * @param {Array<string>} searchFields - 要搜索的字段名数组
 * @returns {Array} 过滤后的列表
 */
export const createSearchFilter = (list, keyword, searchFields = []) => {
  return createListFilter(list, keyword, (item, lowerKeyword) => {
    return searchFields.some(field => {
      const value = item[field]
      return value && String(value).toLowerCase().includes(lowerKeyword)
    })
  })
}

