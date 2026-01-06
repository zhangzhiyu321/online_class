import { STATUS_TEXT, STATUS_TYPE } from './constants'

/**
 * 获取状态文本
 */
export const getStatusText = (type, status) => {
  return STATUS_TEXT[type]?.[status] || '未知'
}

/**
 * 获取状态类型（用于 Element Plus Tag）
 */
export const getStatusType = (type, status) => {
  return STATUS_TYPE[type]?.[status] || 'info'
}

