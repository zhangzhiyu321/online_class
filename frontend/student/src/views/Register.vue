<template>
  <div class="register-container">
    <div class="register-box">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, School } from '@element-plus/icons-vue'

const router = useRouter()
const themeStore = useThemeStore()

// 初始化主题
onMounted(() => {
  themeStore.initTheme()
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  role: 1 // 学生角色
})

const registerFormRef = ref()
const loading = ref(false)

const validatePassword = (rule, value, callback) => {
  if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate((valid) => {
    if (!valid) return
  })

  loading.value = true
  try {
    await register(registerForm.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bgSecondary, #f9fafb);
  padding: 20px;
  will-change: transform;
  transform: translateZ(0);
  transition: background-color 0.3s ease;
}

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
  transform: translateZ(0);
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

