<template>
  <div class="profile">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
    </div>

    <el-card class="profile-card">
      <div class="profile-header">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarError"
          :show-file-list="false"
        >
          <el-avatar :src="userInfo.avatar" :size="100" class="profile-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="avatar-overlay">
            <el-icon><Camera /></el-icon>
          </div>
        </el-upload>
        <div class="profile-info">
          <h2>{{ userInfo.nickname || '未设置昵称' }}</h2>
          <p class="username">用户名：{{ userInfo.username }}</p>
        </div>
      </div>
    </el-card>

    <el-card class="form-card">
      <template #header>
        <span>基本信息</span>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="actions-card">
      <template #header>
        <span>其他操作</span>
      </template>
      <div class="actions-list">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, uploadAvatar } from '@/api/user'
import { User, Camera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  nickname: '',
  phone: '',
  email: ''
})

const formRef = ref()
const submitting = ref(false)

const userInfo = computed(() => userStore.userInfo || {})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: 'blur'
    }
  ]
}

const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/user/avatar`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

const loadUserInfo = () => {
  if (userInfo.value) {
    form.value = {
      nickname: userInfo.value.nickname || '',
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || ''
    }
  } else {
    userStore.loadUserInfo().then((info) => {
      if (info) {
        form.value = {
          nickname: info.nickname || '',
          phone: info.phone || '',
          email: info.email || ''
        }
      }
    })
  }
}

const handleAvatarSuccess = async (response) => {
  const avatarUrl = response.data || response.url
  await updateUserInfo({ avatar: avatarUrl })
  userStore.loadUserInfo()
  ElMessage.success('头像上传成功')
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
  })

  submitting.value = true
  try {
    await updateUserInfo(form.value)
    userStore.loadUserInfo()
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
}

.profile-avatar {
  position: relative;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.profile-avatar:hover .avatar-overlay {
  opacity: 1;
}

.profile-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.username {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.form-card,
.actions-card {
  margin-bottom: 20px;
}

.actions-list {
  display: flex;
  gap: 12px;
}

@media (max-width: 767px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .profile-info h2 {
    font-size: 20px;
  }
}
</style>

