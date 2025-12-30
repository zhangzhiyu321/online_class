import request from './request'

// 获取聊天列表
export const getChatList = () => {
  return request.get('/chat/relationships')
}

// 获取聊天消息
export const getChatMessages = (relationshipId, params) => {
  return request.get(`/chat/messages/${relationshipId}`, { params })
}

// 发送消息
export const sendMessage = (data) => {
  return request.post('/chat/message', data)
}

// 撤回消息
export const recallMessage = (messageId) => {
  return request.put(`/chat/message/${messageId}/recall`)
}

// 标记消息已读
export const markMessageRead = (relationshipId) => {
  return request.put(`/chat/relationship/${relationshipId}/read`)
}

