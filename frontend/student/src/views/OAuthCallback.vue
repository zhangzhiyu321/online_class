<template>
  <div class="oauth-callback-container">
    <div class="callback-box">
      <el-icon class="loading-icon" :size="48" color="#409eff">
        <Loading v-if="loading" />
        <CircleCheck v-else-if="success" />
        <CircleClose v-else />
      </el-icon>
      <h2>{{ message }}</h2>
      <p v-if="loading">正在处理登录信息，请稍候...</p>
      <p v-else-if="success">登录成功，即将跳转...</p>
      <p v-else>登录失败，请重试</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { handleOAuthCallback } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const success = ref(false)
const message = ref('正在处理...')

onMounted(async () => {
  // 从路由参数获取登录类型和授权码
  const type = route.params.type // wechat 或 qq
  const code = route.query.code
  const state = route.query.state

  if (!code) {
    loading.value = false
    success.value = false
    message.value = '登录失败'
    ElMessage.error('未获取到授权码')
    setTimeout(() => {
      router.push('/login')
    }, 2000)
    return
  }

  try {
    const res = await handleOAuthCallback(type, code, state)
    userStore.setToken(res.token)
    userStore.setUserInfo(res.userInfo)
    loading.value = false
    success.value = true
    message.value = '登录成功'
    ElMessage.success('登录成功')
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      router.push('/teachers')
    }, 1500)
  } catch (error) {
    loading.value = false
    success.value = false
    message.value = '登录失败'
    ElMessage.error(error.message || '登录失败，请重试')
    
    // 延迟跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  }
})
</script>

<style scoped>
.oauth-callback-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bgSecondary, #f9fafb);
  padding: 20px;
}

.callback-box {
  text-align: center;
  background: var(--color-bgPrimary, #ffffff);
  border-radius: 20px;
  padding: 60px 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--color-borderPrimary, #e5e7eb);
  min-width: 300px;
}

.loading-icon {
  margin-bottom: 20px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.callback-box h2 {
  margin: 16px 0 8px;
  color: var(--color-textPrimary, #1a1a1a);
  font-size: 24px;
  font-weight: 600;
}

.callback-box p {
  color: var(--color-textTertiary, #6b7280);
  font-size: 14px;
  margin-top: 8px;
}
</style>

