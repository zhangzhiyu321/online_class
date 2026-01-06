/**
 * 时间格式化 composable
 */
export function useTimeFormatter() {
  /**
   * 格式化相对时间（刚刚、几分钟前等）
   */
  const formatRelativeTime = (time) => {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    const diff = now - date
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    return date.toLocaleDateString('zh-CN')
  }

  /**
   * 格式化日期时间
   */
  const formatDateTime = (date, time) => {
    if (!date) return ''
    const dateStr = date.split('T')[0]
    return `${dateStr} ${time || ''}`
  }

  /**
   * 格式化日期
   */
  const formatDate = (time) => {
    if (!time) return ''
    const date = new Date(time)
    return date.toLocaleDateString('zh-CN')
  }

  /**
   * 格式化时间（HH:mm）
   */
  const formatTime = (time) => {
    if (!time) return ''
    const date = new Date(time)
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }

  return {
    formatRelativeTime,
    formatDateTime,
    formatDate,
    formatTime
  }
}

