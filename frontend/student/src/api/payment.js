import request from './request'

// 创建支付记录
export const createPayment = (data) => {
  return request.post('/payment', data)
}

// 获取支付记录列表
export const getPaymentList = (params) => {
  return request.get('/payment/list', { params })
}

// 获取支付详情
export const getPaymentDetail = (id) => {
  return request.get(`/payment/${id}`)
}

// 上传支付凭证
export const uploadPaymentProof = (id, data) => {
  return request.post(`/payment/${id}/proof`, data)
}

// 更新支付方式
export const updatePaymentMethod = (id, paymentMethod) => {
  return request.put(`/payment/${id}/method`, null, { params: { paymentMethod } })
}

// 申请退款
export const createRefund = (data) => {
  return request.post('/refund', data)
}

// 获取退款列表
export const getRefundList = (params) => {
  return request.get('/refund/list', { params })
}

// 获取退款详情
export const getRefundDetail = (id) => {
  return request.get(`/refund/${id}`)
}

