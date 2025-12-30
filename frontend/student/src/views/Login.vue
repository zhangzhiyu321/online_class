<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="48" color="#409eff"><School /></el-icon>
        <h2>****</h2>
        <p>学生登录</p>
      </div>

      <!-- 登录方式切换标签 -->
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="账号登录" name="account">
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
        </el-tab-pane>

        <el-tab-pane label="手机登录" name="phone">
          <el-form
            ref="phoneFormRef"
            :model="phoneForm"
            :rules="phoneRules"
            class="login-form"
            @submit.prevent="handlePhoneLogin"
          >
            <el-form-item prop="phone">
              <el-input
                v-model="phoneForm.phone"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-input-wrapper">
                <el-input
                  v-model="phoneForm.code"
                  placeholder="请输入验证码"
                  size="large"
                  :prefix-icon="Message"
                  @keyup.enter="handlePhoneLogin"
                />
                <el-button
                  :disabled="codeCountdown > 0"
                  :loading="sendingCode"
                  size="large"
                  @click="sendCode"
                  class="code-button"
                >
                  {{ codeCountdown > 0 ? `${codeCountdown}秒后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-button"
                @click="handlePhoneLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 第三方登录 -->
      <div class="social-login">
        <div class="divider">
          <span>其他登录方式</span>
        </div>
        <div class="social-buttons">
          <el-button
            class="social-button wechat"
            @click="handleWechatLogin"
            :loading="socialLoading === 'wechat'"
          >
            <svg class="social-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm193.5 301.7l-210.6 292a31.8 31.8 0 0 1-51.7 0L318.5 484.9c-3.8-5.3 0-12.7 6.5-12.7h46.9c10.2 0 19.9 4.9 25.9 13.3l71.2 98.8 157.2-218c6-8.3 15.6-13.3 25.9-13.3H699c6.5 0 10.3 7.4 6.5 12.7z" fill="#07c160"/>
            </svg>
            <span>微信登录</span>
          </el-button>
          <el-button
            class="social-button qq"
            @click="handleQQLogin"
            :loading="socialLoading === 'qq'"
          >
            <svg class="social-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <path d="M824.8 613.2c-16-51.4-34.4-94.6-53.6-138.6-4.8-10.6-10-21.2-15.2-31.8-80.6 57.4-160 91.2-253.4 91.2-93.4 0-172.8-33.8-253.4-91.2-5.2 10.6-10.4 21.2-15.2 31.8-19.2 44-37.6 87.2-53.6 138.6-16.6 53.4-25.8 107.2-25.8 161.6 0 23.6 19.2 42.8 42.8 42.8 6.6 0 12.6-1.6 18-4.2-1.6-7.2-2.4-14.6-2.4-22.2 0-141.4 114.6-256 256-256s256 114.6 256 256c0 7.6-0.8 15-2.4 22.2 5.4 2.6 11.4 4.2 18 4.2 23.6 0 42.8-19.2 42.8-42.8 0-54.4-9.2-108.2-25.8-161.6zM512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372S306.6 140 512 140s372 166.6 372 372-166.6 372-372 372z" fill="#12b7f5"/>
            </svg>
            <span>QQ登录</span>
          </el-button>
        </div>
      </div>

      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { login, phoneLogin, sendSmsCode, wechatLogin, qqLogin } from '@/api/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message, School } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

// 初始化主题
onMounted(() => {
  themeStore.initTheme()
})

// 登录方式切换
const activeTab = ref('phone')

// 账号登录表单
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

// 手机号登录表单
const phoneForm = ref({
  phone: '',
  code: ''
})

const phoneFormRef = ref()
const sendingCode = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 第三方登录loading状态
const socialLoading = ref('')

// 账号登录
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

// 发送验证码
const sendCode = async () => {
  if (!phoneFormRef.value) return
  
  // 先验证手机号
  await phoneFormRef.value.validateField('phone', async (valid) => {
    if (!valid) return
    
    sendingCode.value = true
    try {
      await sendSmsCode(phoneForm.value.phone)
      ElMessage.success('验证码已发送')
      
      // 开始倒计时
      codeCountdown.value = 60
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      countdownTimer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
      }, 1000)
    } catch (error) {
      ElMessage.error(error.message || '发送验证码失败')
    } finally {
      sendingCode.value = false
    }
  })
}

// 手机号登录
const handlePhoneLogin = async () => {
  if (!phoneFormRef.value) return
  await phoneFormRef.value.validate((valid) => {
    if (!valid) return
  })

  loading.value = true
  try {
    const res = await phoneLogin(phoneForm.value)
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

// 微信登录
const handleWechatLogin = async () => {
  socialLoading.value = 'wechat'
  try {
    const redirectUri = encodeURIComponent(`${window.location.origin}/auth/wechat/callback`)
    const wechatAuthUrl = await wechatLogin(redirectUri)
    // 跳转到微信授权页面
    window.location.href = wechatAuthUrl
  } catch (error) {
    ElMessage.error(error.message || '微信登录失败')
    socialLoading.value = ''
  }
}

// QQ登录
const handleQQLogin = async () => {
  socialLoading.value = 'qq'
  try {
    const redirectUri = encodeURIComponent(`${window.location.origin}/auth/qq/callback`)
    const qqAuthUrl = await qqLogin(redirectUri)
    // 跳转到QQ授权页面
    window.location.href = qqAuthUrl
  } catch (error) {
    ElMessage.error(error.message || 'QQ登录失败')
    socialLoading.value = ''
  }
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
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

.login-tabs {
  margin-top: 20px;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
  align-items: stretch;
  width: 100%;
}

.code-input-wrapper .el-input {
  flex: 1;
  min-width: 0;
}

.code-button {
  flex-shrink: 0;
  white-space: nowrap;
}

.code-input-wrapper :deep(.el-input__wrapper) {
  box-sizing: border-box;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.el-form-item__content) {
  width: 100%;
}

.social-login {
  margin-top: 30px;
}

.divider {
  position: relative;
  text-align: center;
  margin: 20px 0;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background: var(--color-borderPrimary, #e5e7eb);
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  position: relative;
  padding: 0 12px;
  background: var(--color-bgPrimary, #ffffff);
  color: var(--color-textTertiary, #6b7280);
  font-size: 12px;
}

.social-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.social-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid var(--color-borderPrimary, #e5e7eb);
  background: var(--color-bgPrimary, #ffffff);
  transition: all 0.3s;
  color: var(--color-textPrimary, #1a1a1a);
}

.social-button:hover {
  border-color: var(--color-primary, #409eff);
  background: var(--color-bgSecondary, #f9fafb);
}

.social-button.wechat {
  color: #07c160;
}

.social-button.wechat:hover {
  border-color: #07c160;
}

.social-button.qq {
  color: #12b7f5;
}

.social-button.qq:hover {
  border-color: #12b7f5;
}

.social-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

@media (max-width: 767px) {
  .login-box {
    padding: 30px 20px;
    border-radius: 16px;
  }

  .login-header h2 {
    font-size: 20px;
  }

  .social-buttons {
    display: flex;
    flex-direction: row;
    gap: 12px;
    justify-content: center;
    align-items: stretch;
  }
  
  .social-button {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    height: auto;
  }
  
  .code-input-wrapper {
    display: flex;
    flex-direction: row;
    gap: 12px;
    align-items: stretch;
    width: 100%;
  }
  
  .code-input-wrapper .el-input {
    flex: 1;
    min-width: 0;
  }
  
  .code-button {
    flex-shrink: 0;
    white-space: nowrap;
  }
  
  .code-input-wrapper :deep(.el-input__wrapper) {
    box-sizing: border-box;
  }
  
  .login-form :deep(.el-form-item__content) {
    width: 100%;
  }
  
  .code-input-wrapper :deep(.el-input__wrapper) {
    height: 100%;
  }
}
</style>

