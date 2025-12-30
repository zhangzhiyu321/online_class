import request from './request'

// 获取通知列表
export const getNotificationList = (params) => {
  return request.get('/notification/list', { params })
}

// 获取通知详情
export const getNotificationDetail = (id) => {
  return request.get(`/notification/${id}`)
}

// 标记通知为已读
export const markNotificationRead = (id) => {
  return request.put(`/notification/${id}/read`)
}

// 标记所有通知为已读
export const markAllNotificationsRead = () => {
  return request.put('/notification/read-all')
}

// 获取未读通知数量
export const getUnreadCount = () => {
  return request.get('/notification/unread-count')
}

// 删除通知
export const deleteNotification = (id) => {
  return request.delete(`/notification/${id}`)
}

