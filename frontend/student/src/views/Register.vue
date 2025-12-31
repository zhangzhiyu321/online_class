<!--
  注册页面组件 (Register)
  功能：
  1. 用户注册（用户名、密码、昵称、手机号等）
  2. 表单验证（密码长度、确认密码一致性、手机号格式等）
  3. 注册成功后的路由跳转
-->
<template>
  <div class="register-container">
    <div class="register-box">
      <!-- 注册页头部（Logo和标题） -->
      <div class="register-header">
        <el-icon :size="48" color="#409eff"><School /></el-icon>
        <h2>学生注册</h2>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            size="large"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号（选填）"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 注册页面 - Script Setup
 * 使用 Composition API 实现组件逻辑
 */

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, School } from '@element-plus/icons-vue'

// ========== 路由和状态管理 ==========
const router = useRouter() // Vue Router 实例，用于页面跳转
const themeStore = useThemeStore() // 主题状态管理 Store

// ========== 生命周期钩子 ==========

/**
 * 组件挂载时初始化主题
 * 从本地存储中恢复用户的主题偏好设置
 */
onMounted(() => {
  themeStore.initTheme()
})

// ========== 响应式数据定义 ==========

/**
 * 注册表单数据
 */
const registerForm = ref({
  username: '',        // 用户名（必填）
  password: '',        // 密码（必填，至少6位）
  confirmPassword: '', // 确认密码（必填，需与密码一致）
  nickname: '',        // 昵称（必填）
  phone: '',           // 手机号（选填，如填写需符合格式）
  role: 1              // 用户角色（1: 学生角色）
})

const registerFormRef = ref() // 注册表单的引用（用于表单验证）
const loading = ref(false) // 注册按钮加载状态

// ========== 表单验证方法 ==========

/**
 * 自定义密码验证器
 * 验证密码长度是否至少6位
 * @param {Object} rule - 验证规则对象
 * @param {string} value - 密码值
 * @param {Function} callback - 验证回调函数
 */
const validatePassword = (rule, value, callback) => {
  if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

/**
 * 自定义确认密码验证器
 * 验证确认密码是否与密码一致
 * @param {Object} rule - 验证规则对象
 * @param {string} value - 确认密码值
 * @param {Function} callback - 验证回调函数
 */
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

/**
 * 注册表单验证规则
 */
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' } // 使用自定义验证器
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' } // 使用自定义验证器
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/, // 手机号格式验证（11位，以1开头，第二位为3-9）
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ]
}

// ========== 事件处理方法 ==========

/**
 * 处理用户注册
 * 验证表单 -> 调用注册API -> 提示成功 -> 跳转到登录页
 */
const handleRegister = async () => {
  if (!registerFormRef.value) return
  // 表单验证
  await registerFormRef.value.validate((valid) => {
    if (!valid) return
  })

  loading.value = true
  try {
    // 调用注册API
    await register(registerForm.value)
    ElMessage.success('注册成功，请登录')
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ========== 注册容器样式 ========== */

/* 注册页面容器（居中显示，占满视口高度） */
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bgSecondary, #f9fafb);
  padding: 20px;
  will-change: transform;
  transform: translateZ(0); /* GPU加速 */
  transition: background-color 0.3s ease; /* 主题切换时的平滑过渡 */
}

/* 注册框容器（卡片样式，带悬停效果） */
.register-box {
  width: 100%;
  max-width: 400px;
  background: var(--color-bgPrimary, #ffffff);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--color-borderPrimary, #e5e7eb);
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.15s cubic-bezier(0.4, 0, 0.2, 1),
              background-color 0.3s ease,
              border-color 0.3s ease;
  will-change: transform;
  transform: translateZ(0); /* GPU加速 */
}

.register-box:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px) translateZ(0);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header .el-icon {
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  transform: translateZ(0);
}

.register-box:hover .register-header .el-icon {
  transform: scale(1.05) rotate(5deg) translateZ(0);
}

.register-header h2 {
  margin: 16px 0;
  color: var(--color-textPrimary, #1a1a1a);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: -0.5px;
  transition: color 0.3s ease;
}

.register-form {
  margin-top: 20px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
}

.register-form :deep(.el-input__wrapper:hover) {
  transform: translateY(-1px);
}

.register-button {
  width: 100%;
  margin-top: 10px;
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  transform: translateZ(0);
}

.register-button:hover {
  transform: translateY(-1px) scale(1.02) translateZ(0);
}

.register-button:active {
  transform: translateY(0) scale(0.98) translateZ(0);
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--color-textTertiary, #6b7280);
  font-size: 14px;
  transition: color 0.3s ease;
}

.register-footer .el-link {
  margin-left: 4px;
  transition: color 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.register-footer .el-link:hover {
  color: var(--color-primary, #667eea);
}

@media (max-width: 767px) {
  .register-box {
    padding: 30px 20px;
    border-radius: 16px;
  }

  .register-header h2 {
    font-size: 20px;
  }
}
</style>

