# 线上家教接单系统 - API 接口文档

## 一、API 设计规范

### 1.1 基础信息
- **Base URL**: `http://localhost:8080/api`
- **协议**: HTTP/HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8

### 1.2 统一响应格式

所有接口统一使用以下响应格式：

```json
{
  "code": 200,           // 状态码：200-成功，其他-失败
  "message": "success",  // 提示信息
  "data": {}             // 响应数据（可选）
}
```

**状态码说明**：
- `200`: 请求成功
- `400`: 请求参数错误
- `401`: 未登录或Token过期
- `403`: 无权限访问
- `404`: 资源不存在
- `500`: 服务器内部错误

### 1.3 认证方式

需要认证的接口在请求头中携带Token：

```
Authorization: Bearer {token}
```

### 1.4 分页参数

列表接口统一使用以下分页参数：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从1开始，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10，最大100 |

**分页响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],        // 数据列表
    "total": 100,      // 总记录数
    "page": 1,        // 当前页码
    "pageSize": 10    // 每页数量
  }
}
```

---

## 二、认证相关接口

### 2.1 用户登录

**接口地址**: `POST /api/auth/login`

**请求参数**:
```json
{
  "username": "string",  // 用户名（必填）
  "password": "string"   // 密码（必填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "student001",
      "nickname": "学生",
      "avatar": "https://example.com/avatar.jpg",
      "role": 1,
      "phone": "13800138000",
      "email": "student@example.com"
    }
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 2.2 用户注册

**接口地址**: `POST /api/auth/register`

**请求参数**:
```json
{
  "username": "string",      // 用户名（必填，唯一）
  "password": "string",       // 密码（必填，至少6位）
  "nickname": "string",      // 昵称（必填）
  "phone": "string",         // 手机号（选填）
  "email": "string",         // 邮箱（选填）
  "role": 1                  // 角色：1-学生，2-教师（必填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### 2.3 手机号登录

**接口地址**: `POST /api/auth/login/phone`

**请求参数**:
```json
{
  "phone": "string",  // 手机号（必填）
  "code": "string"    // 验证码（必填，6位数字）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "student001",
      "nickname": "学生",
      "avatar": "https://example.com/avatar.jpg",
      "role": 1,
      "phone": "13800138000",
      "email": "student@example.com"
    }
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "验证码错误或已过期",
  "data": null
}
```

**说明**:
- 验证码有效期为5分钟
- 验证码为6位数字
- 如果手机号未注册，系统会自动创建账号

---

### 2.4 发送短信验证码

**接口地址**: `POST /api/auth/sms/send`

**请求参数**:
```json
{
  "phone": "string"  // 手机号（必填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "手机号格式错误",
  "data": null
}
```

**说明**:
- 同一手机号1分钟内只能发送一次
- 验证码有效期为5分钟
- 验证码为6位随机数字

---

### 2.5 微信登录授权

**接口地址**: `GET /api/auth/wechat/authorize`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| redirectUri | String | 是 | 授权回调地址（需要URL编码） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "https://open.weixin.qq.com/connect/qrconnect?appid=xxx&redirect_uri=xxx&response_type=code&scope=snsapi_login&state=xxx#wechat_redirect"
}
```

**说明**:
- 返回微信授权URL，前端需要跳转到该URL
- 用户授权后，微信会回调到 `redirectUri`，并携带 `code` 和 `state` 参数
- 前端需要将 `code` 和 `state` 传递给回调接口完成登录

---

### 2.6 微信登录回调

**接口地址**: `POST /api/auth/wechat/callback`

**请求参数**:
```json
{
  "code": "string",  // 微信返回的授权码（必填）
  "state": "string"  // 状态参数（必填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "student001",
      "nickname": "学生",
      "avatar": "https://example.com/avatar.jpg",
      "role": 1,
      "phone": "13800138000",
      "email": "student@example.com"
    }
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "授权失败或已过期",
  "data": null
}
```

**说明**:
- 如果微信账号未绑定，系统会自动创建账号并绑定
- 如果微信账号已绑定，直接登录
- 首次微信登录需要用户授权获取基本信息

---

### 2.7 QQ登录授权

**接口地址**: `GET /api/auth/qq/authorize`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| redirectUri | String | 是 | 授权回调地址（需要URL编码） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "https://graph.qq.com/oauth2.0/authorize?client_id=xxx&redirect_uri=xxx&response_type=code&scope=get_user_info&state=xxx"
}
```

**说明**:
- 返回QQ授权URL，前端需要跳转到该URL
- 用户授权后，QQ会回调到 `redirectUri`，并携带 `code` 和 `state` 参数
- 前端需要将 `code` 和 `state` 传递给回调接口完成登录

---

### 2.8 QQ登录回调

**接口地址**: `POST /api/auth/qq/callback`

**请求参数**:
```json
{
  "code": "string",  // QQ返回的授权码（必填）
  "state": "string"  // 状态参数（必填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "student001",
      "nickname": "学生",
      "avatar": "https://example.com/avatar.jpg",
      "role": 1,
      "phone": "13800138000",
      "email": "student@example.com"
    }
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "授权失败或已过期",
  "data": null
}
```

**说明**:
- 如果QQ账号未绑定，系统会自动创建账号并绑定
- 如果QQ账号已绑定，直接登录
- 首次QQ登录需要用户授权获取基本信息

---

## 三、用户相关接口

### 3.1 获取当前用户信息

**接口地址**: `GET /api/user/info`

**请求头**: 需要Token认证

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "student001",
    "nickname": "学生",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "13800138000",
    "email": "student@example.com",
    "role": 1,
    "status": 1,
    "onlineStatus": 0,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

---

### 3.2 更新用户信息

**接口地址**: `PUT /api/user/info`

**请求头**: 需要Token认证

**请求参数**:
```json
{
  "nickname": "string",  // 昵称（选填）
  "phone": "string",    // 手机号（选填）
  "email": "string",    // 邮箱（选填）
  "studentProfile": {   // 学生扩展信息（选填，仅学生角色）
    "realName": "string",      // 真实姓名
    "grade": "string",         // 年级
    "schoolName": "string",    // 学校名称
    "learningGoals": "string", // 学习目标
    "weakSubjects": [1, 2],     // 薄弱科目ID数组
    "parentName": "string",    // 家长姓名
    "parentPhone": "string"    // 家长电话
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "nickname": "新昵称",
    "phone": "13800138000",
    "email": "new@example.com"
  }
}
```

---

### 3.3 上传头像

**接口地址**: `POST /api/user/avatar`

**请求头**: 
- 需要Token认证
- `Content-Type: multipart/form-data`

**请求参数**:
- `file`: File (图片文件，支持jpg/png，最大5MB)

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://example.com/uploads/avatar/xxx.jpg"
  }
}
```

---

## 四、教师相关接口

### 4.1 获取教师列表

**接口地址**: `GET /api/teacher/list`

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| keyword | String | 否 | 搜索关键词（教师姓名或科目） |
| stageId | Integer | 否 | 教学阶段ID |
| subjectId | Integer | 否 | 科目ID |
| minRating | Decimal | 否 | 最低评分 |
| minPrice | Decimal | 否 | 最低价格 |
| maxPrice | Decimal | 否 | 最高价格 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "nickname": "张老师",
        "realName": "张三",
        "avatar": "https://example.com/avatar.jpg",
        "rating": 4.8,
        "ratingCount": 120,
        "teachingYears": 5,
        "certified": true,
        "onlineStatus": 1,
        "subjects": [
          {"id": 1, "name": "数学"}
        ],
        "minPrice": 100.00
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 12
  }
}
```

---

### 4.2 获取教师详情

**接口地址**: `GET /api/teacher/{id}`

**路径参数**:
- `id`: 教师用户ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "nickname": "张老师",
    "realName": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "introduction": "资深数学教师，教学经验丰富...",
    "teachingYears": 5,
    "teachingStyle": "注重基础，循序渐进",
    "rating": 4.8,
    "ratingCount": 120,
    "certified": true,
    "onlineStatus": 1,
    "stages": [
      {"id": 1, "name": "小学"},
      {"id": 2, "name": "初中"}
    ],
    "teachings": [
      {
        "id": 1,
        "stageId": 1,
        "stageName": "小学",
        "subjectId": 1,
        "subjectName": "数学",
        "pricePerHour": 100.00
      }
    ],
    "schedules": [
      {
        "id": 1,
        "weekday": 1,
        "startTime": "19:00:00",
        "endTime": "21:00:00",
        "scheduleType": 1
      }
    ],
    "certifications": [
      {
        "id": 1,
        "certificateType": 1,
        "certificateImage": "https://example.com/cert.jpg",
        "schoolName": "XX大学",
        "major": "数学",
        "degree": "本科",
        "status": 2
      }
    ]
  }
}
```

---

### 4.3 获取教师空闲时间

**接口地址**: `GET /api/teacher/{teacherId}/schedule`

**路径参数**:
- `teacherId`: 教师用户ID

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | String | 否 | 开始日期（YYYY-MM-DD） |
| endDate | String | 否 | 结束日期（YYYY-MM-DD） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "weekday": 1,
        "startTime": "19:00:00",
        "endTime": "21:00:00",
        "scheduleType": 1,
        "startDate": null,
        "endDate": null
      }
    ]
  }
}
```

---

### 4.4 获取教师评价列表

**接口地址**: `GET /api/teacher/{teacherId}/reviews`

**路径参数**:
- `teacherId`: 教师用户ID

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "studentId": 2,
        "studentName": "李同学",
        "rating": 5,
        "content": "老师讲得很好，很有耐心",
        "images": [],
        "createdAt": "2024-01-15T10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10
  }
}
```

---

## 五、预约相关接口

### 5.1 创建预约

**接口地址**: `POST /api/appointment`

**请求头**: 需要Token认证

**请求参数**:
```json
{
  "teacherId": 1,              // 教师ID（必填）
  "stageId": 1,                 // 教学阶段ID（必填）
  "subjectId": 1,               // 科目ID（必填）
  "appointmentDate": "2024-01-20",  // 预约日期（必填，YYYY-MM-DD）
  "startTime": "19:00",         // 开始时间（必填，HH:mm）
  "endTime": "21:00",           // 结束时间（必填，HH:mm）
  "duration": 120,              // 时长（分钟）（必填）
  "pricePerHour": 100.00,       // 课时单价（必填）
  "totalAmount": 200.00,        // 总金额（必填）
  "studentName": "李同学",       // 学生姓名（必填）
  "studentGrade": "五年级",      // 学生年级（必填）
  "studentPhone": "13800138000", // 联系方式（必填）
  "remark": "需要重点讲解应用题"  // 备注（选填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "预约成功，等待教师确认",
  "data": {
    "id": 1,
    "orderNo": "APT20240120001",
    "status": 1,
    "appointmentDate": "2024-01-20",
    "startTime": "19:00:00",
    "endTime": "21:00:00",
    "totalAmount": 200.00
  }
}
```

---

### 5.2 获取预约列表

**接口地址**: `GET /api/appointment/list`

**请求头**: 需要Token认证

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| status | Integer | 否 | 预约状态：1-待确认，2-已确认，3-已完成，4-已取消 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "orderNo": "APT20240120001",
        "teacherId": 1,
        "teacherName": "张老师",
        "teacherAvatar": "https://example.com/avatar.jpg",
        "subjectName": "数学",
        "appointmentDate": "2024-01-20",
        "startTime": "19:00:00",
        "endTime": "21:00:00",
        "duration": 120,
        "totalAmount": 200.00,
        "status": 1,
        "createdAt": "2024-01-15T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "pageSize": 10
  }
}
```

---

### 5.3 获取预约详情

**接口地址**: `GET /api/appointment/{id}`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 预约ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "APT20240120001",
    "teacherId": 1,
    "teacherName": "张老师",
    "teacherAvatar": "https://example.com/avatar.jpg",
    "teacherPhone": "13800138001",
    "stageId": 1,
    "stageName": "小学",
    "subjectId": 1,
    "subjectName": "数学",
    "appointmentDate": "2024-01-20",
    "startTime": "19:00:00",
    "endTime": "21:00:00",
    "duration": 120,
    "pricePerHour": 100.00,
    "totalAmount": 200.00,
    "studentName": "李同学",
    "studentGrade": "五年级",
    "studentPhone": "13800138000",
    "remark": "需要重点讲解应用题",
    "status": 2,
    "dingtalkUrl": "https://meeting.dingtalk.com/xxx",
    "confirmedAt": "2024-01-16T10:00:00",
    "createdAt": "2024-01-15T10:00:00"
  }
}
```

---

### 5.4 取消预约

**接口地址**: `PUT /api/appointment/{id}/cancel`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 预约ID

**请求参数**:
```json
{
  "reason": "临时有事，无法上课"  // 取消原因（必填）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "取消成功",
  "data": null
}
```

---

## 六、支付相关接口

### 6.1 获取支付记录列表

**接口地址**: `GET /api/payment/list`

**请求头**: 需要Token认证

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| status | Integer | 否 | 支付状态：1-待支付，2-待确认，3-已完成，4-已拒绝 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "paymentNo": "PAY20240120001",
        "appointmentId": 1,
        "teacherId": 1,
        "teacherName": "张老师",
        "amount": 200.00,
        "status": 1,
        "createdAt": "2024-01-20T10:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 10
  }
}
```

---

### 6.2 获取支付详情

**接口地址**: `GET /api/payment/{id}`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 支付记录ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "paymentNo": "PAY20240120001",
    "appointmentId": 1,
    "teacherId": 1,
    "teacherName": "张老师",
    "amount": 200.00,
    "paymentMethod": 1,
    "status": 1,
    "bankName": "中国工商银行",
    "bankAccount": "6222****1234",
    "accountHolder": "张三",
    "transferImage": null,
    "transferAmount": null,
    "transferTime": null,
    "rejectReason": null,
    "createdAt": "2024-01-20T10:00:00"
  }
}
```

---

### 6.3 上传支付凭证

**接口地址**: `POST /api/payment/{id}/proof`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 支付记录ID

**请求参数**:
```json
{
  "transferImage": "https://example.com/proof.jpg",  // 转账截图URL（必填）
  "transferAmount": 200.00,                          // 转账金额（必填）
  "transferTime": "2024-01-20T10:00:00"             // 转账时间（必填，ISO格式）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "提交成功，等待教师确认",
  "data": {
    "id": 1,
    "status": 2,
    "transferImage": "https://example.com/proof.jpg",
    "transferAmount": 200.00,
    "transferTime": "2024-01-20T10:00:00"
  }
}
```

---

## 七、退款相关接口

### 7.1 申请退款

**接口地址**: `POST /api/refund`

**请求头**: 需要Token认证

**请求参数**:
```json
{
  "paymentId": 1,              // 支付记录ID（必填）
  "refundReason": "string"     // 退款原因（必填，至少10个字符）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "退款申请已提交",
  "data": {
    "id": 1,
    "refundNo": "REF20240120001",
    "refundAmount": 200.00,
    "status": 1
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "该支付记录不支持退款",
  "data": null
}
```

**说明**:
- 只有已完成支付的订单才能申请退款
- 退款申请需要管理员审核
- 退款原因至少10个字符

---

### 7.2 获取退款列表

**接口地址**: `GET /api/refund/list`

**请求头**: 需要Token认证

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| status | Integer | 否 | 退款状态：1-待审核，2-已通过，3-已拒绝，4-已完成 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "refundNo": "REF20240120001",
        "paymentId": 1,
        "appointmentId": 1,
        "teacherId": 1,
        "teacherName": "张老师",
        "refundAmount": 200.00,
        "refundReason": "课程未按时进行",
        "status": 1,
        "createdAt": "2024-01-20T10:00:00"
      }
    ],
    "total": 3,
    "page": 1,
    "pageSize": 10
  }
}
```

---

### 7.3 获取退款详情

**接口地址**: `GET /api/refund/{id}`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 退款记录ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "refundNo": "REF20240120001",
    "paymentId": 1,
    "appointmentId": 1,
    "teacherId": 1,
    "teacherName": "张老师",
    "refundAmount": 200.00,
    "refundReason": "课程未按时进行",
    "status": 2,
    "auditReason": "审核通过",
    "auditAt": "2024-01-21T10:00:00",
    "refundTime": "2024-01-22T10:00:00",
    "refundAccount": "6222****1234",
    "createdAt": "2024-01-20T10:00:00"
  }
}
```

---

## 八、评价相关接口

### 8.1 创建评价

**接口地址**: `POST /api/review`

**请求头**: 需要Token认证

**请求参数**:
```json
{
  "appointmentId": 1,          // 预约ID（必填）
  "rating": 5,                 // 评分：1-5分（必填）
  "content": "string",          // 评价内容（选填，最多500字符）
  "images": ["url1", "url2"]    // 评价图片URL数组（选填，最多3张）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "评价提交成功",
  "data": {
    "id": 1,
    "appointmentId": 1,
    "teacherId": 1,
    "rating": 5,
    "content": "老师很专业，讲解清晰",
    "images": [],
    "createdAt": "2024-01-20T10:00:00"
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "该预约已完成评价或未完成课程",
  "data": null
}
```

**说明**:
- 只有已完成的预约才能评价
- 每个预约只能评价一次
- 评分必填，评价内容选填
- 评价图片最多3张

---

### 8.2 获取评价详情

**接口地址**: `GET /api/review/{id}`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 评价ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "appointmentId": 1,
    "studentId": 1,
    "studentName": "学生",
    "teacherId": 1,
    "teacherName": "张老师",
    "rating": 5,
    "content": "老师很专业，讲解清晰",
    "images": ["url1", "url2"],
    "createdAt": "2024-01-20T10:00:00"
  }
}
```

---

## 九、通知相关接口

### 9.1 获取通知列表

**接口地址**: `GET /api/notification/list`

**请求头**: 需要Token认证

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| type | Integer | 否 | 通知类型：1-系统通知，2-预约通知，3-支付通知，4-评价通知，5-聊天通知 |
| isRead | Integer | 否 | 是否已读：0-未读，1-已读 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "type": 2,
        "title": "预约已确认",
        "content": "您的预约已被教师确认",
        "relatedId": 1,
        "relatedType": "appointment",
        "isRead": 0,
        "createdAt": "2024-01-20T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "pageSize": 20
  }
}
```

---

### 9.2 获取通知详情

**接口地址**: `GET /api/notification/{id}`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 通知ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "type": 2,
    "title": "预约已确认",
    "content": "您的预约已被教师确认，请按时参加课程",
    "relatedId": 1,
    "relatedType": "appointment",
    "isRead": 0,
    "readAt": null,
    "createdAt": "2024-01-20T10:00:00"
  }
}
```

---

### 9.3 标记通知为已读

**接口地址**: `PUT /api/notification/{id}/read`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 通知ID

**响应示例**:
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

---

### 9.4 标记所有通知为已读

**接口地址**: `PUT /api/notification/read-all`

**请求头**: 需要Token认证

**响应示例**:
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

---

### 9.5 获取未读通知数量

**接口地址**: `GET /api/notification/unread-count`

**请求头**: 需要Token认证

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 5
  }
}
```

---

### 9.6 删除通知

**接口地址**: `DELETE /api/notification/{id}`

**请求头**: 需要Token认证

**路径参数**:
- `id`: 通知ID

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 十、聊天相关接口

### 7.1 获取聊天列表

**接口地址**: `GET /api/chat/relationships`

**请求头**: 需要Token认证

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "name": "张老师",
      "avatar": "https://example.com/avatar.jpg",
      "onlineStatus": 1,
      "unreadCount": 5,
      "lastMessage": {
        "id": 100,
        "type": 1,
        "content": "好的，明天见",
        "createdAt": "2024-01-20T10:00:00"
      },
      "lastMessageTime": "2024-01-20T10:00:00"
    }
  ]
}
```

---

### 7.2 获取聊天关系详情

**接口地址**: `GET /api/chat/relationship/{relationshipId}`

**请求头**: 需要Token认证

**路径参数**:
- `relationshipId`: 聊天关系ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "张老师",
    "avatar": "https://example.com/avatar.jpg",
    "onlineStatus": 1
  }
}
```

---

### 7.3 获取聊天消息列表

**接口地址**: `GET /api/chat/messages/{relationshipId}`

**请求头**: 需要Token认证

**路径参数**:
- `relationshipId`: 聊天关系ID

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "relationshipId": 1,
        "senderId": 2,
        "receiverId": 1,
        "messageType": 1,
        "content": "你好，我想预约明天的课程",
        "fileUrl": null,
        "fileName": null,
        "fileSize": null,
        "duration": null,
        "isRead": 1,
        "isRecalled": 0,
        "createdAt": "2024-01-20T09:00:00"
      },
      {
        "id": 2,
        "relationshipId": 1,
        "senderId": 1,
        "receiverId": 2,
        "messageType": 4,
        "content": "{\"image_url\":\"https://example.com/image.jpg\",\"width\":1920,\"height\":1080}",
        "fileUrl": "https://example.com/image.jpg",
        "fileName": null,
        "fileSize": null,
        "duration": null,
        "isRead": 1,
        "isRecalled": 0,
        "createdAt": "2024-01-20T09:05:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 50
  }
}
```

**消息类型说明**:
- `1`: 文本消息
- `2`: 文件消息
- `3`: 语音消息
- `4`: 图片消息

---

### 7.4 发送消息

**接口地址**: `POST /api/chat/message`

**请求头**: 需要Token认证

**请求参数**:
```json
{
  "relationshipId": 1,        // 聊天关系ID（必填）
  "receiverId": 1,            // 接收者ID（必填）
  "messageType": 1,           // 消息类型：1-文本，2-文件，3-语音，4-图片（必填）
  "content": "消息内容"       // 消息内容（必填）
}
```

**不同消息类型的content格式**:

- **文本消息** (messageType=1):
```json
{
  "content": "你好"
}
```

- **图片消息** (messageType=4):
```json
{
  "content": "{\"image_url\":\"https://example.com/image.jpg\",\"thumbnail_url\":\"https://example.com/thumb.jpg\",\"width\":1920,\"height\":1080}"
}
```

- **语音消息** (messageType=3):
```json
{
  "content": "{\"voice_url\":\"https://example.com/voice.mp3\",\"duration\":30}"
}
```

- **文件消息** (messageType=2):
```json
{
  "content": "{\"file_url\":\"https://example.com/file.pdf\",\"file_name\":\"学习资料.pdf\",\"file_size\":1024000}"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "id": 1,
    "relationshipId": 1,
    "senderId": 2,
    "receiverId": 1,
    "messageType": 1,
    "content": "你好",
    "createdAt": "2024-01-20T10:00:00"
  }
}
```

---

### 7.5 撤回消息

**接口地址**: `PUT /api/chat/message/{messageId}/recall`

**请求头**: 需要Token认证

**路径参数**:
- `messageId`: 消息ID

**响应示例**:
```json
{
  "code": 200,
  "message": "撤回成功",
  "data": null
}
```

**注意**: 只能撤回2分钟内自己发送的消息

---

### 7.6 标记消息已读

**接口地址**: `PUT /api/chat/relationship/{relationshipId}/read`

**请求头**: 需要Token认证

**路径参数**:
- `relationshipId`: 聊天关系ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 7.7 获取未读消息数

**接口地址**: `GET /api/chat/unread-count`

**请求头**: 需要Token认证

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 5,
    "total": 5
  }
}
```

---

## 十一、公告相关接口

### 8.1 获取已发布公告列表

**接口地址**: `GET /api/announcements/published`

**请求参数** (Query参数):
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | Integer | 否 | 公告类型：1-系统公告，2-活动公告，3-维护公告 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "系统维护通知",
      "content": "系统将于今晚进行维护...",
      "type": 3,
      "priority": 3,
      "publishTime": "2024-01-15T10:00:00",
      "viewCount": 100
    }
  ]
}
```

---

### 8.2 获取公告详情

**接口地址**: `GET /api/announcements/{id}`

**路径参数**:
- `id`: 公告ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "系统维护通知",
    "content": "系统将于今晚进行维护，预计持续2小时...",
    "type": 3,
    "priority": 3,
    "publishTime": "2024-01-15T10:00:00",
    "expireTime": null,
    "viewCount": 100,
    "createdAt": "2024-01-15T09:00:00"
  }
}
```

---

## 十二、通用接口

### 9.1 获取教学阶段列表

**接口地址**: `GET /api/common/teaching-stages`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "PRIMARY",
      "name": "小学",
      "sortOrder": 1
    },
    {
      "id": 2,
      "code": "MIDDLE",
      "name": "初中",
      "sortOrder": 2
    },
    {
      "id": 3,
      "code": "HIGH",
      "name": "高中",
      "sortOrder": 3
    }
  ]
}
```

---

### 9.2 获取科目列表

**接口地址**: `GET /api/common/subjects`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "code": "MATH",
      "name": "数学",
      "category": "理科",
      "sortOrder": 1
    },
    {
      "id": 2,
      "code": "CHINESE",
      "name": "语文",
      "category": "文科",
      "sortOrder": 2
    }
  ]
}
```

---

### 9.3 文件上传

**接口地址**: `POST /api/common/upload`

**请求头**: 
- 需要Token认证
- `Content-Type: multipart/form-data`

**请求参数**:
- `file`: File (文件，必填)
- `type`: String (文件类型，选填，可选值：image/document/video/audio)

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://example.com/uploads/2024/01/xxx.jpg",
    "fileName": "image.jpg",
    "fileSize": 1024000,
    "fileType": "image"
  }
}
```

**文件大小限制**:
- 图片：最大10MB
- 文件：最大50MB
- 语音：最大10MB

**支持的文件格式**:
- 图片：jpg, jpeg, png, gif
- 文件：pdf, doc, docx, xls, xlsx, ppt, pptx
- 语音：mp3, m4a, wav

---

## 十三、接口开发优先级建议

### 第一阶段（基础功能 - 必须实现）
1. ✅ 认证接口（登录、注册）
2. ✅ 用户信息接口（获取、更新、上传头像）
3. ✅ 通用接口（教学阶段、科目、文件上传）

### 第二阶段（核心功能 - 必须实现）
4. ✅ 教师相关接口（列表、详情、空闲时间、评价）
5. ✅ 预约相关接口（创建、列表、详情、取消）

### 第三阶段（业务功能 - 必须实现）
6. ✅ 支付相关接口（列表、详情、上传凭证）
7. ✅ 聊天相关接口（列表、消息、发送、撤回、已读）

### 第四阶段（辅助功能 - 可选）
8. ✅ 公告相关接口（已有实现）

---

## 十一、注意事项

### 11.1 权限控制
- 学生只能查看和操作自己的数据
- 教师只能查看和操作自己的数据
- 预约、支付等操作需要验证用户身份

### 11.2 数据验证
- 所有必填参数必须验证
- 手机号、邮箱等格式验证
- 文件大小和格式验证

### 11.3 错误处理
- 统一使用Result类返回结果
- 错误信息要清晰明确
- 记录错误日志便于排查

### 11.4 性能优化
- 列表接口必须支持分页
- 复杂查询使用索引优化
- 文件上传使用异步处理

### 11.5 安全性
- 密码必须加密存储（BCrypt）
- Token设置合理的过期时间
- 防止SQL注入和XSS攻击
- 文件上传验证文件类型和大小

---

## 十二、WebSocket 接口（可选）

如果需要实时聊天功能，可以添加WebSocket支持：

**连接地址**: `ws://localhost:8080/ws?token={token}`

**消息格式**:
```json
{
  "type": "receiveMsg",
  "code": 1,
  "msg": "收到新消息",
  "data": {
    "id": 1,
    "relationshipId": 1,
    "senderId": 123,
    "receiverId": 456,
    "messageType": 1,
    "content": "消息内容",
    "createdAt": 1234567890
  }
}
```

---

## 十三、接口测试建议

1. 使用Postman或Swagger进行接口测试
2. 先测试认证接口，获取Token
3. 按照功能模块顺序测试
4. 测试正常流程和异常流程
5. 验证权限控制是否正确

---

**文档版本**: v1.0  
**最后更新**: 2024-01-20  
**维护者**: 开发团队

