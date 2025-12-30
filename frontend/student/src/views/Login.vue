<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="48" color="#409eff"><School /></el-icon>
        <h2>****</h2>
        <p>学生登录</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { login } from '@/api/user'
import { ElMessage } from 'element-plus'
import { User, Lock, School } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

// 初始化主题
onMounted(() => {
  themeStore.initTheme()
})

const loginForm = ref({
  username: '',
  password: ''
})

const loginFormRef = ref()
const loading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate((valid) => {
    if (!valid) return
  })

  loading.value = true
  try {
    const res = await login(loginForm.value)
    userStore.setToken(res.token)
    userStore.setUserInfo(res.userInfo)
    ElMessage.success('登录成功')
    router.push('/teachers')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
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

.login-box {
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

.login-box:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px) translateZ(0);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header .el-icon {
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  transform: translateZ(0);
}

.login-box:hover .login-header .el-icon {
  transform: scale(1.05) rotate(5deg) translateZ(0);
}

.login-header h2 {
  margin: 16px 0 8px;
  color: var(--color-textPrimary, #1a1a1a);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: -0.5px;
  transition: color 0.3s ease;
}

.login-header p {
  color: var(--color-textTertiary, #6b7280);
  font-size: 14px;
  font-weight: 400;
  transition: color 0.3s ease;
}

.login-form {
  margin-top: 30px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
}

.login-form :deep(.el-input__wrapper:hover) {
  transform: translateY(-1px);
}

.login-button {
  width: 100%;
  margin-top: 10px;
  border-radius: 12px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform;
  transform: translateZ(0);
}

.login-button:hover {
  transform: translateY(-1px) scale(1.02) translateZ(0);
}

.login-button:active {
  transform: translateY(0) scale(0.98) translateZ(0);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--color-textTertiary, #6b7280);
  font-size: 14px;
  transition: color 0.3s ease;
}

.login-footer .el-link {
  margin-left: 4px;
  transition: color 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-footer .el-link:hover {
  color: var(--color-primary, #667eea);
}

@media (max-width: 767px) {
  .login-box {
    padding: 30px 20px;
    border-radius: 16px;
  }

  .login-header h2 {
    font-size: 20px;
  }
}
</style>

