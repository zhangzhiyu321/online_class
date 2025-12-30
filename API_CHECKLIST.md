# API 接口实现清单

## 快速检查清单

### ✅ 认证相关（8个接口）
- [ ] `POST /api/auth/login` - 用户登录
- [ ] `POST /api/auth/register` - 用户注册
- [ ] `POST /api/auth/login/phone` - 手机号登录
- [ ] `POST /api/auth/sms/send` - 发送短信验证码
- [ ] `GET /api/auth/wechat/authorize` - 微信登录授权
- [ ] `POST /api/auth/wechat/callback` - 微信登录回调
- [ ] `GET /api/auth/qq/authorize` - QQ登录授权
- [ ] `POST /api/auth/qq/callback` - QQ登录回调

### ✅ 用户相关（3个接口）
- [ ] `GET /api/user/info` - 获取当前用户信息
- [ ] `PUT /api/user/info` - 更新用户信息
- [ ] `POST /api/user/avatar` - 上传头像

### ✅ 教师相关（4个接口）
- [ ] `GET /api/teacher/list` - 获取教师列表（支持筛选、搜索、分页）
- [ ] `GET /api/teacher/{id}` - 获取教师详情
- [ ] `GET /api/teacher/{teacherId}/schedule` - 获取教师空闲时间
- [ ] `GET /api/teacher/{teacherId}/reviews` - 获取教师评价列表

### ✅ 预约相关（4个接口）
- [ ] `POST /api/appointment` - 创建预约
- [ ] `GET /api/appointment/list` - 获取预约列表
- [ ] `GET /api/appointment/{id}` - 获取预约详情
- [ ] `PUT /api/appointment/{id}/cancel` - 取消预约

### ✅ 支付相关（3个接口）
- [ ] `GET /api/payment/list` - 获取支付记录列表
- [ ] `GET /api/payment/{id}` - 获取支付详情
- [ ] `POST /api/payment/{id}/proof` - 上传支付凭证

### ✅ 聊天相关（7个接口）
- [ ] `GET /api/chat/relationships` - 获取聊天列表
- [ ] `GET /api/chat/relationship/{relationshipId}` - 获取聊天关系详情
- [ ] `GET /api/chat/messages/{relationshipId}` - 获取聊天消息列表
- [ ] `POST /api/chat/message` - 发送消息
- [ ] `PUT /api/chat/message/{messageId}/recall` - 撤回消息
- [ ] `PUT /api/chat/relationship/{relationshipId}/read` - 标记消息已读
- [ ] `GET /api/chat/unread-count` - 获取未读消息数

### ✅ 公告相关（2个接口）
- [ ] `GET /api/announcements/published` - 获取已发布公告列表（已有实现）
- [ ] `GET /api/announcements/{id}` - 获取公告详情（已有实现）

### ✅ 通用接口（3个接口）
- [ ] `GET /api/common/teaching-stages` - 获取教学阶段列表
- [ ] `GET /api/common/subjects` - 获取科目列表
- [ ] `POST /api/common/upload` - 文件上传

---

## 总计：34个接口

### 已实现：2个
- ✅ 公告列表和详情（已有实现）

### 待实现：32个

---

## 开发顺序建议

### 第1步：基础功能（14个接口）
1. 认证接口（账号登录、注册、手机号登录、短信验证码、微信登录、QQ登录）
2. 用户信息接口（获取、更新、上传头像）
3. 通用接口（教学阶段、科目、文件上传）

**预计时间**：3-4天

### 第2步：核心功能（8个接口）
4. 教师相关接口（列表、详情、空闲时间、评价）
5. 预约相关接口（创建、列表、详情、取消）

**预计时间**：3-4天

### 第3步：业务功能（10个接口）
6. 支付相关接口（列表、详情、上传凭证）
7. 聊天相关接口（列表、消息、发送、撤回、已读、未读数）

**预计时间**：4-5天

---

## 响应格式规范

所有接口统一返回格式：
```json
{
  "code": 200,        // 200-成功，其他-失败
  "message": "success",
  "data": {}          // 响应数据
}
```

---

## 认证方式

需要认证的接口在请求头中携带：
```
Authorization: Bearer {token}
```

---

详细API文档请查看：`API_DOCUMENTATION.md`

