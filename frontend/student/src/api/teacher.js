import request from './request'

// 获取教师列表
export const getTeacherList = (params) => {
  return request.get('/teacher/list', { params })
}

// 获取教师详情
export const getTeacherDetail = (id) => {
  return request.get(`/teacher/${id}`)
}

// 获取教师空闲时间
export const getTeacherSchedule = (teacherId, params) => {
  return request.get(`/teacher/${teacherId}/schedule`, { params })
}

// 获取教师评价
export const getTeacherReviews = (teacherId, params) => {
  return request.get(`/teacher/${teacherId}/reviews`, { params })
}

