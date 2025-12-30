import request from './request'

// 创建预约
export const createAppointment = (data) => {
  return request.post('/appointment', data)
}

// 获取预约列表
export const getAppointmentList = (params) => {
  return request.get('/appointment/list', { params })
}

// 获取预约详情
export const getAppointmentDetail = (id) => {
  return request.get(`/appointment/${id}`)
}

// 取消预约
export const cancelAppointment = (id, reason) => {
  return request.put(`/appointment/${id}/cancel`, { reason })
}

