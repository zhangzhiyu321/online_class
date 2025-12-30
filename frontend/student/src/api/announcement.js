import request from './request'

/**
 * 获取已发布的公告列表
 */
export const getPublishedAnnouncements = (params = {}) => {
  return request.get('/announcements/published', { params })
}

/**
 * 获取公告详情
 */
export const getAnnouncement = (id) => {
  return request.get(`/announcements/${id}`)
}

