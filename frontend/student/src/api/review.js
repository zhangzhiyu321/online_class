import request from './request'

// 创建评价
export const createReview = (data) => {
  return request.post('/review', data)
}

// 获取评价详情
export const getReviewDetail = (id) => {
  return request.get(`/review/${id}`)
}

