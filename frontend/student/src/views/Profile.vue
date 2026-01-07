<template>
  <div class="profile">
    <el-card class="profile-card">
      <div class="profile-header">
        <el-upload
          :http-request="handleCustomAvatarUpload"
          :before-upload="beforeAvatarUpload"
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

    <el-card class="form-card">
      <template #header>
        <span>学生信息</span>
      </template>
      <el-form
        ref="studentFormRef"
        :model="studentForm"
        :rules="studentRules"
        label-width="120px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="studentForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="studentForm.grade" placeholder="请输入年级，如：初一、高一" />
        </el-form-item>
        <el-form-item label="学校名称" prop="schoolName">
          <el-input v-model="studentForm.schoolName" placeholder="请输入学校名称" />
        </el-form-item>
        <el-form-item label="学习目标" prop="learningGoals">
          <el-input
            v-model="studentForm.learningGoals"
            type="textarea"
            :rows="4"
            placeholder="请输入学习目标"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="薄弱科目">
          <el-select
            v-model="studentForm.weakSubjects"
            multiple
            placeholder="请选择薄弱科目"
            style="width: 100%"
          >
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="家长姓名" prop="parentName">
          <el-input v-model="studentForm.parentName" placeholder="请输入家长姓名" />
        </el-form-item>
        <el-form-item label="家长电话" prop="parentPhone">
          <el-input v-model="studentForm.parentPhone" placeholder="请输入家长电话" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmitStudent" :loading="submittingStudent">
            保存学生信息
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
import { updateUserInfo, uploadAvatar, getUserInfo } from '@/api/user'
import { uploadFile } from '@/api/common'
import { getSubjects } from '@/api/common'
import { User, Camera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  nickname: '',
  phone: '',
  email: ''
})

const studentForm = ref({
  realName: '',
  grade: '',
  schoolName: '',
  learningGoals: '',
  weakSubjects: [],
  parentName: '',
  parentPhone: ''
})

const subjects = ref([])
const formRef = ref()
const studentFormRef = ref()
const submitting = ref(false)
const submittingStudent = ref(false)

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

const studentRules = {
  parentPhone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ]
}

// 上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('头像大小不能超过 5MB！')
    return false
  }
  return true
}

// 自定义头像上传
const handleCustomAvatarUpload = async (options) => {
  const { file } = options
  try {
    const result = await uploadAvatar(file)
    // 处理不同的返回格式
    const avatarUrl = result?.url || result?.data?.url || (typeof result === 'string' ? result : '')
    if (!avatarUrl) {
      throw new Error('上传失败：未获取到头像URL')
    }
    await updateUserInfo({ avatar: avatarUrl })
    userStore.loadUserInfo()
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  }
}

const loadUserInfo = async () => {
  try {
    const info = await getUserInfo()
    if (info) {
      form.value = {
        nickname: info.nickname || '',
        phone: info.phone || '',
        email: info.email || ''
      }
      // 加载学生扩展信息
      if (info.studentProfile) {
        studentForm.value = {
          realName: info.studentProfile.realName || '',
          grade: info.studentProfile.grade || '',
          schoolName: info.studentProfile.schoolName || '',
          learningGoals: info.studentProfile.learningGoals || '',
          weakSubjects: info.studentProfile.weakSubjects || [],
          parentName: info.studentProfile.parentName || '',
          parentPhone: info.studentProfile.parentPhone || ''
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadSubjects = async () => {
  try {
    const data = await getSubjects()
    subjects.value = Array.isArray(data) ? data : (data.list || [])
  } catch (error) {
    console.error('加载科目列表失败:', error)
  }
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

const handleSubmitStudent = async () => {
  if (!studentFormRef.value) return
  await studentFormRef.value.validate((valid) => {
    if (!valid) return
  })

  submittingStudent.value = true
  try {
    await updateUserInfo({
      studentProfile: studentForm.value
    })
    userStore.loadUserInfo()
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submittingStudent.value = false
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
  loadSubjects()
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

