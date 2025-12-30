import request from './request'

// 登录
export const login = (data) => {
  return request.post('/auth/login', data)
}

// 手机号登录
export const phoneLogin = (data) => {
  return request.post('/auth/login/phone', data)
}

// 发送短信验证码
export const sendSmsCode = (phone) => {
  return request.post('/auth/sms/send', { phone })
}

// 微信登录 - 获取授权URL
export const wechatLogin = (redirectUri) => {
  return request.get('/auth/wechat/authorize', {
    params: { redirectUri }
  })
}

// QQ登录 - 获取授权URL
export const qqLogin = (redirectUri) => {
  return request.get('/auth/qq/authorize', {
    params: { redirectUri }
  })
}

// 第三方登录回调处理
export const handleOAuthCallback = (type, code, state) => {
  return request.post(`/auth/${type}/callback`, { code, state })
}

// 注册
export const register = (data) => {
  return request.post('/auth/register', data)
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/user/info')
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return request.put('/user/info', data)
}

// 上传头像
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

