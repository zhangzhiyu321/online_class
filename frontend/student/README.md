# 学生端前端项目

## 项目简介

这是线上家教系统的学生端前端项目，使用 Vue 3 + Element Plus 开发，支持响应式设计（手机端和电脑端）。

## 技术栈

- Vue 3
- Vue Router 4
- Pinia（状态管理）
- Element Plus（UI 组件库）
- Axios（HTTP 请求）
- Vite（构建工具）

## 功能模块

### 1. 用户认证
- 登录
- 注册

### 2. 教师浏览
- 教师列表（支持筛选、搜索、分页）
- 教师详情（基本信息、学历认证、教学信息、空闲时间、学生评价）

### 3. 预约管理
- 创建预约
- 预约列表
- 预约详情

### 4. 支付管理
- 支付列表
- 支付详情
- 上传转账凭证

### 5. 实时聊天
- 聊天列表
- 聊天窗口（支持文本、图片、语音、文件）

### 6. 个人中心
- 个人信息管理
- 头像上传

## 项目结构

```
src/
├── api/              # API 接口
│   ├── request.js    # Axios 封装
│   ├── user.js       # 用户相关接口
│   ├── teacher.js    # 教师相关接口
│   ├── appointment.js # 预约相关接口
│   ├── payment.js    # 支付相关接口
│   ├── chat.js       # 聊天相关接口
│   └── common.js     # 通用接口
├── assets/           # 静态资源
├── components/       # 公共组件
├── layouts/          # 布局组件
│   └── MainLayout.vue # 主布局
├── router/           # 路由配置
│   └── index.js
├── stores/           # 状态管理
│   └── user.js       # 用户状态
├── views/            # 页面组件
│   ├── Login.vue     # 登录页
│   ├── Register.vue  # 注册页
│   ├── Profile.vue   # 个人中心
│   ├── teacher/      # 教师相关页面
│   ├── appointment/  # 预约相关页面
│   ├── payment/      # 支付相关页面
│   └── chat/         # 聊天相关页面
├── App.vue           # 根组件
└── main.js           # 入口文件
```

## 安装和运行

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run dev
```

访问 http://localhost:5173

### 生产环境构建

```bash
npm run build
```

构建产物在 `dist/` 目录

## 环境变量配置

创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 响应式设计

项目采用响应式设计，支持以下断点：

- **移动端**：< 768px
- **平板**：768px - 1024px
- **桌面端**：> 1024px

### 响应式特性

1. **导航栏**：桌面端显示顶部导航，移动端显示底部导航
2. **布局**：自动适配不同屏幕尺寸
3. **组件**：Element Plus 组件自动响应式

## API 接口说明

所有 API 接口都在 `src/api/` 目录下，统一使用 `request.js` 封装的 Axios 实例。

### 请求拦截器
- 自动添加 Token 到请求头
- 统一错误处理

### 响应拦截器
- 统一处理响应数据
- 自动处理 401 未登录状态

## 状态管理

使用 Pinia 进行状态管理，当前有：

- **user store**：用户信息、Token 等

## 路由配置

路由配置在 `src/router/index.js`，包含路由守卫：

- 未登录用户访问需要认证的页面会跳转到登录页
- 已登录用户访问登录/注册页会跳转到首页

## 注意事项

1. **WebSocket 连接**：聊天功能需要后端支持 WebSocket，当前代码中已预留接口，需要根据实际后端实现进行对接

2. **文件上传**：图片、文件上传功能需要后端支持，当前使用 Element Plus 的 Upload 组件

3. **实时消息**：聊天窗口的实时消息接收需要在 `ChatWindow.vue` 中实现 WebSocket 连接

4. **API 地址**：确保后端 API 地址正确配置在环境变量中

## 开发建议

1. 所有 API 调用统一使用 `src/api/` 下的接口文件
2. 公共组件放在 `src/components/` 目录
3. 页面组件放在 `src/views/` 目录
4. 样式使用 scoped，避免样式污染
5. 响应式设计优先考虑移动端体验

## 浏览器支持

- Chrome（推荐）
- Firefox
- Safari
- Edge

## 许可证

MIT
