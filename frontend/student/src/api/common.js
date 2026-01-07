import request from './request'

// 获取教学阶段列表
export const getTeachingStages = () => {
  return request.get('/common/teaching-stages')
}

// 获取科目列表
export const getSubjects = () => {
  return request.get('/common/subjects')
}

// 上传文件
export const uploadFile = (file, type = 'image') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  return request.post('/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    // 处理返回格式：后端返回 { code: 200, data: { url: "..." } }
    // 但request拦截器已经提取了data，所以这里res就是 { url: "..." }
    return res
  })
}

