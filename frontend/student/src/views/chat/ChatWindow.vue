<template>
  <div class="chat-window">
    <div class="chat-container">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <!-- 返回按钮 -->
        <el-button
          type="text"
          :icon="ArrowLeft"
          @click="$router.back()"
          class="header-back-button"
        >
          返回
        </el-button>
        <!-- 居中显示的名字 -->
        <div class="header-center">
          <h3 class="header-title">{{ chatInfo.name }}</h3>
          <span v-if="chatInfo.onlineStatus === 1" class="online-status">在线</span>
        </div>
        <!-- 右侧占位，保持居中 -->
        <div class="header-right-placeholder"></div>
      </div>

      <!-- 消息列表 -->
      <div ref="messagesContainer" class="messages-container">
        <template v-for="(message, index) in messages" :key="message.id">
          <!-- 日期分隔符 -->
          <div v-if="shouldShowDateDivider(message, index)" class="date-divider">
            {{ formatDateDivider(message.createdAt) }}
          </div>
          <!-- 时间显示（居中显示在消息上方） -->
          <div v-if="shouldShowTime(message, index)" class="message-time-center">
            {{ formatMessageTime(message.createdAt) }}
          </div>
          <div
            class="message-item"
            :class="{ 'message-right': message.senderId === currentUserId }"
          >
            <!-- 对方头像（左侧消息） -->
            <el-avatar
              v-if="message.senderId !== currentUserId"
              :src="chatInfo.avatar"
              :size="40"
              class="message-avatar"
            />
            <!-- 消息内容 -->
            <div class="message-content">
              <div class="message-bubble" :class="getMessageClass(message)">
                <!-- 文本消息 -->
                <p v-if="message.messageType === 1" class="message-text">
                  {{ message.content }}
                </p>
                <!-- 图片消息 -->
                <div v-else-if="message.messageType === 4" class="message-image-wrapper">
                  <el-image
                    :src="getImageUrl(message)"
                    :preview-src-list="[getImageUrl(message)]"
                    fit="cover"
                    class="message-image"
                    :lazy="true"
                    loading="lazy"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>图片加载失败</span>
                      </div>
                    </template>
                  </el-image>
                </div>
                <!-- 语音消息 -->
                <div v-else-if="message.messageType === 3" class="message-voice">
                  <el-button
                    :icon="isPlayingVoice === message.id ? VideoPause : VideoPlay"
                    circle
                    size="default"
                    @click="toggleVoicePlay(message)"
                    class="voice-play-btn"
                    :class="{ playing: isPlayingVoice === message.id }"
                  />
                  <div class="voice-info">
                    <div class="voice-waveform" :class="{ playing: isPlayingVoice === message.id }">
                      <span v-for="i in 5" :key="i" class="wave-bar" :style="{ animationDelay: i * 0.1 + 's' }"></span>
                    </div>
                    <span class="voice-duration">{{ message.duration || 0 }}"</span>
                  </div>
                  <audio
                    ref="voiceAudioRefs"
                    :data-message-id="message.id"
                    :src="getVoiceUrl(message)"
                    @ended="handleVoiceEnded(message.id)"
                    class="voice-audio"
                  />
                </div>
                <!-- 文件消息 -->
                <div v-else-if="message.messageType === 2" class="message-file">
                  <div class="file-icon-wrapper">
                    <el-icon class="file-icon"><Document /></el-icon>
                  </div>
                  <div class="file-info">
                    <div class="file-name">{{ message.fileName || '未知文件' }}</div>
                    <div class="file-size" v-if="message.fileSize">
                      {{ formatFileSize(message.fileSize) }}
                    </div>
                  </div>
                  <el-button
                    :icon="Download"
                    circle
                    size="small"
                    @click="downloadFile(message)"
                    class="file-download-btn"
                    title="下载文件"
                  />
                </div>
              </div>
              <!-- 撤回按钮（显示在消息气泡下方） -->
              <div v-if="message.senderId === currentUserId && canRecall(message)" class="message-recall">
                <el-button
                  type="text"
                  size="small"
                  @click="handleRecall(message.id)"
                >
                  撤回
                </el-button>
              </div>
            </div>
            <!-- 本人头像（右侧消息） -->
            <el-avatar
              v-if="message.senderId === currentUserId"
              :src="currentUserAvatar"
              :size="40"
              class="message-avatar"
            />
          </div>
        </template>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <!-- 语音录制区域 -->
        <div v-if="isRecording" class="voice-record-area">
          <div class="record-indicator">
            <div class="record-dot"></div>
            <span class="record-time">{{ formatRecordTime(recordTime) }}</span>
          </div>
          <div class="record-actions">
            <el-button @click="cancelVoiceRecord">取消</el-button>
            <el-button type="primary" @click="stopVoiceRecord">发送</el-button>
          </div>
        </div>
        <div v-else class="input-box">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入消息，按回车发送..."
            @keydown.enter="handleKeyDown"
            enterkeyhint="send"
            clearable
            class="message-input"
          />
          <!-- 加号按钮菜单 -->
          <el-dropdown
            trigger="click"
            placement="top"
            :hide-on-click="true"
            class="add-button-dropdown"
          >
            <el-button
              :icon="Plus"
              circle
              class="add-button"
              title="更多"
            />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleImageUpload">
                  <el-icon><Picture /></el-icon>
                  <span>图片</span>
                </el-dropdown-item>
                <el-dropdown-item @click="handleFileUpload">
                  <el-icon><Document /></el-icon>
                  <span>文件</span>
                </el-dropdown-item>
                <el-dropdown-item @click="toggleVoiceRecord">
                  <el-icon><Microphone /></el-icon>
                  <span>语音</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getChatMessages, sendMessage, recallMessage, getChatRelationship, markMessageRead } from '@/api/chat'
import { uploadFile } from '@/api/common'
import { getTeacherDetail } from '@/api/teacher'
import {
  ArrowLeft,
  User,
  Microphone,
  Document,
  Picture,
  Phone,
  VideoPlay,
  VideoPause,
  Promotion,
  Download,
  Plus
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 处理路由参数：如果是 /chat/new，relationshipId 为 'new'，否则为实际 ID
const relationshipId = ref(
  route.params.relationshipId 
    ? route.params.relationshipId 
    : (route.path === '/chat/new' || route.name === 'NewChat' ? 'new' : null)
)
const chatInfo = ref({})
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const messagesContainer = ref(null)

// 语音相关
const isRecording = ref(false)
const recordTime = ref(0)
const recordTimer = ref(null)
const mediaRecorder = ref(null)
const audioChunks = ref([])
const isPlayingVoice = ref(null)
const voiceAudioRefs = ref([])

const currentUserId = computed(() => userStore.userInfo?.id)
const currentUserAvatar = computed(() => userStore.userInfo?.avatar)

const loadMessages = async () => {
  // 如果是新建聊天或 relationshipId 无效，不加载消息
  if (!relationshipId.value || relationshipId.value === 'new') {
    messages.value = []
    return
  }
  
  try {
    // 确保 relationshipId 是数字类型
    const id = typeof relationshipId.value === 'string' ? parseInt(relationshipId.value) : relationshipId.value
    if (isNaN(id)) {
      console.error('无效的 relationshipId:', relationshipId.value)
      messages.value = []
      return
    }
    
    const data = await getChatMessages(id, {
      page: 1,
      pageSize: 50
    })
    // 后端返回的是数组，不是包含 list 的对象
    const messageList = Array.isArray(data) ? data : (data?.list || [])
    // 按时间从早到晚排序（新消息在底部）
    messages.value = messageList.sort((a, b) => {
      const timeA = new Date(a.createdAt || 0).getTime()
      const timeB = new Date(b.createdAt || 0).getTime()
      return timeA - timeB
    })
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载消息失败')
    console.error(error)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const getMessageClass = (message) => {
  return message.senderId === currentUserId.value ? 'bubble-right' : 'bubble-left'
}

const getImageUrl = (message) => {
  // 优先使用 fileUrl 字段（新格式）
  if (message.fileUrl) {
    return message.fileUrl
  }
  // 兼容旧格式（content 中存储 JSON）
  try {
    const content = JSON.parse(message.content)
    return content.image_url || content.file_url || ''
  } catch {
    return ''
  }
}

const getVoiceUrl = (message) => {
  try {
    const content = JSON.parse(message.content)
    return content.voice_url || content.file_url
  } catch {
    return message.fileUrl || ''
  }
}

const getFileUrl = (message) => {
  try {
    const content = JSON.parse(message.content)
    return content.file_url
  } catch {
    return message.fileUrl || ''
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 下载文件
const downloadFile = (message) => {
  const fileUrl = getFileUrl(message)
  if (fileUrl) {
    window.open(fileUrl, '_blank')
  } else {
    ElMessage.error('文件链接不存在')
  }
}

// 格式化消息时间（类似微信的显示方式）
const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const messageDate = new Date(date)
  messageDate.setHours(0, 0, 0, 0)
  
  // 格式化时间部分（HH:mm）
  const timeStr = date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  
  // 如果是今天，只显示时间（不显示"今天"）
  if (messageDate.getTime() === today.getTime()) {
    return timeStr
  }
  
  // 如果是昨天，显示"昨天 HH:mm"
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  if (messageDate.getTime() === yesterday.getTime()) {
    return `昨天 ${timeStr}`
  }
  
  // 如果是前天，显示"前天 HH:mm"
  const dayBeforeYesterday = new Date(today)
  dayBeforeYesterday.setDate(dayBeforeYesterday.getDate() - 2)
  if (messageDate.getTime() === dayBeforeYesterday.getTime()) {
    return `前天 ${timeStr}`
  }
  
  // 如果是今年，显示"MM月DD日 HH:mm"
  const currentYear = today.getFullYear()
  const messageYear = date.getFullYear()
  if (messageYear === currentYear) {
    return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
  }
  
  // 否则显示完整日期时间 "YYYY年MM月DD日 HH:mm"
  return `${messageYear}年${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
}

// 格式化日期分隔符
const formatDateDivider = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const messageDate = new Date(date)
  messageDate.setHours(0, 0, 0, 0)
  
  // 如果是今天
  if (messageDate.getTime() === today.getTime()) {
    return '今天'
  }
  
  // 昨天
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  if (messageDate.getTime() === yesterday.getTime()) {
    return '昨天'
  }
  
  // 前天
  const dayBeforeYesterday = new Date(today)
  dayBeforeYesterday.setDate(dayBeforeYesterday.getDate() - 2)
  if (messageDate.getTime() === dayBeforeYesterday.getTime()) {
    return '前天'
  }
  
  // 更早的日期
  const currentYear = today.getFullYear()
  const messageYear = date.getFullYear()
  if (messageYear === currentYear) {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  } else {
    return `${messageYear}年${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 判断是否显示日期分隔符
const shouldShowDateDivider = (message, index) => {
  if (index === 0) return true
  if (!message.createdAt) return false
  
  const currentDate = new Date(message.createdAt)
  currentDate.setHours(0, 0, 0, 0)
  
  const prevMessage = messages.value[index - 1]
  if (!prevMessage || !prevMessage.createdAt) return false
  
  const prevDate = new Date(prevMessage.createdAt)
  prevDate.setHours(0, 0, 0, 0)
  
  return currentDate.getTime() !== prevDate.getTime()
}

// 判断是否显示时间（类似微信的逻辑）
const shouldShowTime = (message, index) => {
  if (!message.createdAt) return false
  
  // 第一条消息显示时间
  if (index === 0) return true
  
  const prevMessage = messages.value[index - 1]
  // 如果上一条消息不存在或没有时间，显示时间
  if (!prevMessage || !prevMessage.createdAt) return true
  
  // 检查日期是否变化
  const currentDate = new Date(message.createdAt)
  currentDate.setHours(0, 0, 0, 0)
  const prevDate = new Date(prevMessage.createdAt)
  prevDate.setHours(0, 0, 0, 0)
  
  // 如果日期不同，显示时间
  if (currentDate.getTime() !== prevDate.getTime()) return true
  
  // 如果与上一条消息间隔超过5分钟，显示时间
  const currentTime = new Date(message.createdAt).getTime()
  const prevTime = new Date(prevMessage.createdAt).getTime()
  const diffMinutes = (currentTime - prevTime) / 1000 / 60
  
  return diffMinutes > 5
}

const canRecall = (message) => {
  if (!message.createdAt) return false
  const now = new Date()
  const msgTime = new Date(message.createdAt)
  const diff = (now - msgTime) / 1000 / 60 // 分钟
  return diff < 2 && !message.isRecalled
}

// 处理键盘事件
const handleKeyDown = (event) => {
  // 如果按的是回车键（没有按Shift）
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
  // 如果按的是 Shift+Enter，允许换行（默认行为）
}

const handleSend = async () => {
  if (!inputMessage.value.trim()) return
  if (!chatInfo.value.userId) {
    ElMessage.error('无法获取接收者信息，请刷新页面重试')
    return
  }

  sending.value = true
  try {
    const response = await sendMessage({
      receiverId: chatInfo.value.userId,
      messageType: 1,
      content: inputMessage.value.trim()
    })
    // 如果返回了新的 relationshipId，更新它（新建聊天的情况）
    const isNewChat = !relationshipId.value || relationshipId.value === 'new'
    if (response && response.relationshipId && (isNewChat || response.relationshipId !== relationshipId.value)) {
      relationshipId.value = response.relationshipId
      // 更新路由但不刷新页面
      router.replace(`/chat/${response.relationshipId}`)
      // 重新加载消息列表
      await loadMessages()
    } else {
      // 直接重新加载消息列表
      await loadMessages()
    }
    inputMessage.value = ''
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const handleRecall = async (messageId) => {
  try {
    await recallMessage(messageId)
    ElMessage.success('撤回成功')
    loadMessages()
  } catch (error) {
    ElMessage.error(error.message || '撤回失败')
  }
}

const handleImageUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    
    // 检查文件大小（最大10MB）
    if (file.size > 10 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过10MB')
      return
    }
    
    try {
      sending.value = true
      const uploadResult = await uploadFile(file, 'image')
      const imageUrl = uploadResult.url || uploadResult.data?.url || uploadResult
      
      // 获取图片尺寸
      const img = new Image()
      img.onload = async () => {
        try {
          const response = await sendMessage({
            receiverId: chatInfo.value.userId,
            messageType: 4,
            fileUrl: imageUrl,
            imageWidth: img.width,
            imageHeight: img.height,
            thumbnailUrl: imageUrl,
            fileSize: file.size
          })
          // 如果返回了新的 relationshipId，更新它（新建聊天的情况）
          if (response && response.relationshipId && (relationshipId.value === 'new' || response.relationshipId !== relationshipId.value)) {
            relationshipId.value = response.relationshipId
            // 更新路由但不刷新页面
            router.replace(`/chat/${response.relationshipId}`)
            // 重新加载消息列表
            await loadMessages()
          } else {
            // 直接重新加载消息列表
            await loadMessages()
          }
          ElMessage.success('图片发送成功')
        } catch (error) {
          ElMessage.error(error.message || '图片发送失败')
        } finally {
          sending.value = false
        }
      }
      img.onerror = () => {
        ElMessage.error('图片加载失败')
        sending.value = false
      }
      img.src = imageUrl
    } catch (error) {
      ElMessage.error(error.message || '图片上传失败')
      sending.value = false
    }
  }
  input.click()
}

const handleFileUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    
    // 检查文件大小（最大50MB）
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过50MB')
      return
    }
    
    try {
      sending.value = true
      const uploadResult = await uploadFile(file, 'document')
      const fileUrl = uploadResult.url || uploadResult.data?.url || uploadResult
      
      await sendMessage({
        relationshipId: relationshipId.value,
        receiverId: chatInfo.value.userId,
        messageType: 2,
        content: JSON.stringify({
          file_url: fileUrl,
          file_name: file.name,
          file_size: file.size
        })
      })
      ElMessage.success('文件发送成功')
      loadMessages()
    } catch (error) {
      ElMessage.error(error.message || '文件上传失败')
    } finally {
      sending.value = false
    }
  }
  input.click()
}

// 语音录制功能
const toggleVoiceRecord = async () => {
  if (isRecording.value) {
    stopVoiceRecord()
  } else {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      mediaRecorder.value = new MediaRecorder(stream)
      audioChunks.value = []

      mediaRecorder.value.ondataavailable = (event) => {
        audioChunks.value.push(event.data)
      }

      mediaRecorder.value.onstop = async () => {
        const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' })
        await sendVoiceMessage(audioBlob)
        stream.getTracks().forEach(track => track.stop())
      }

      mediaRecorder.value.start()
      isRecording.value = true
      recordTime.value = 0
      recordTimer.value = setInterval(() => {
        recordTime.value++
        if (recordTime.value >= 300) { // 最大5分钟
          stopVoiceRecord()
        }
      }, 1000)
    } catch (error) {
      ElMessage.error('无法访问麦克风，请检查权限设置')
      console.error('Error accessing microphone:', error)
    }
  }
}

const stopVoiceRecord = () => {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }
  if (recordTimer.value) {
    clearInterval(recordTimer.value)
    recordTimer.value = null
  }
  isRecording.value = false
  recordTime.value = 0
}

const cancelVoiceRecord = () => {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }
  if (recordTimer.value) {
    clearInterval(recordTimer.value)
    recordTimer.value = null
  }
  isRecording.value = false
  recordTime.value = 0
  audioChunks.value = []
}

const formatRecordTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const sendVoiceMessage = async (audioBlob) => {
  try {
    sending.value = true
    // 实际上传语音文件到服务器
    const uploadResult = await uploadFile(audioBlob, 'audio')
    const voiceUrl = uploadResult.url || uploadResult.data?.url || uploadResult
    
    await sendMessage({
      relationshipId: relationshipId.value,
      receiverId: chatInfo.value.userId,
      messageType: 3,
      content: JSON.stringify({
        voice_url: voiceUrl,
        duration: recordTime.value
      })
    })
    
    ElMessage.success('语音消息发送成功')
    loadMessages()
  } catch (error) {
    ElMessage.error(error.message || '发送语音消息失败')
    console.error(error)
  } finally {
    sending.value = false
  }
}

// 语音播放功能
const toggleVoicePlay = (message) => {
  const audioElement = document.querySelector(`audio[data-message-id="${message.id}"]`)
  if (!audioElement) return

  if (isPlayingVoice.value === message.id) {
    audioElement.pause()
    isPlayingVoice.value = null
  } else {
    // 停止其他正在播放的语音
    if (isPlayingVoice.value) {
      const currentAudio = document.querySelector(`audio[data-message-id="${isPlayingVoice.value}"]`)
      if (currentAudio) currentAudio.pause()
    }
    audioElement.play()
    isPlayingVoice.value = message.id
  }
}

const handleVoiceEnded = (messageId) => {
  if (isPlayingVoice.value === messageId) {
    isPlayingVoice.value = null
  }
}

// 语音通话功能
const handleVoiceCall = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要拨打语音通话给 ${chatInfo.value.name} 吗？`,
      '发起语音通话',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // TODO: 调用语音通话API
    ElMessage.info('正在发起语音通话...')
    
    // 这里应该调用WebRTC或第三方通话服务
    // 例如：initiateVoiceCall(chatInfo.value.userId)
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Voice call error:', error)
    }
  }
}

const loadChatInfo = async () => {
  try {
    // 如果是新建聊天（relationshipId 为 "new" 或 undefined），从 query 参数获取 teacherId
    if (!relationshipId.value || relationshipId.value === 'new') {
      const teacherId = route.query.teacherId
      if (!teacherId) {
        ElMessage.error('缺少教师ID参数')
        router.back()
        return
      }
      // 获取教师信息
      const teacherData = await getTeacherDetail(teacherId)
      chatInfo.value = {
        name: teacherData.nickname || teacherData.realName || '教师',
        avatar: teacherData.avatar || '',
        userId: parseInt(teacherId),
        onlineStatus: teacherData.onlineStatus || 0
      }
      // 新建聊天时，消息列表为空
      messages.value = []
      return
    }
    
    // 正常加载聊天关系信息
    // 确保 relationshipId 是数字类型
    const id = typeof relationshipId.value === 'string' ? parseInt(relationshipId.value) : relationshipId.value
    if (isNaN(id)) {
      console.error('无效的 relationshipId:', relationshipId.value)
      ElMessage.error('无效的聊天关系ID')
      router.back()
      return
    }
    
    const data = await getChatRelationship(id)
    chatInfo.value = {
      name: data.otherUserName || data.name || data.nickname || '用户',
      avatar: data.otherUserAvatar || data.avatar || '',
      userId: data.otherUserId,
      onlineStatus: data.onlineStatus || 0
    }
  } catch (error) {
    console.error('加载聊天信息失败:', error)
    ElMessage.error('加载聊天信息失败')
    // 使用默认值
    chatInfo.value = {
      name: '用户',
      avatar: '',
      userId: null,
      onlineStatus: 0
    }
  }
}

// WebSocket 连接
let ws = null
const connectWebSocket = () => {
  // 如果后端支持WebSocket，在这里建立连接
  // const wsUrl = `ws://localhost:8080/ws?token=${userStore.token}`
  // ws = new WebSocket(wsUrl)
  // 
  // ws.onmessage = (event) => {
  //   const message = JSON.parse(event.data)
  //   if (message.type === 'receiveMsg' && message.data.relationshipId === relationshipId.value) {
  //     // 收到新消息，添加到消息列表
  //     messages.value.push(message.data)
  //     scrollToBottom()
  //     // 标记已读
  //     markMessageRead(relationshipId.value)
  //   }
  // }
  // 
  // ws.onerror = (error) => {
  //   console.error('WebSocket error:', error)
  // }
  // 
  // ws.onclose = () => {
  //   // 重连逻辑
  //   setTimeout(connectWebSocket, 3000)
  // }
}

const disconnectWebSocket = () => {
  if (ws) {
    ws.close()
    ws = null
  }
}

// 标记消息已读
const markAsRead = async () => {
  if (relationshipId.value && relationshipId.value !== 'new') {
    const id = typeof relationshipId.value === 'string' ? parseInt(relationshipId.value) : relationshipId.value
    if (!isNaN(id)) {
      try {
        await markMessageRead(id)
        // 标记已读后，可以触发父组件刷新未读数量（通过事件或 store）
        // 这里可以通过 window 事件通知 ChatList 刷新
        window.dispatchEvent(new CustomEvent('chat-read', { detail: { relationshipId: id } }))
      } catch (error) {
        console.error('标记消息已读失败:', error)
      }
    }
  }
}

// 页面可见性变化处理
const handleVisibilityChange = () => {
  // 当页面重新可见时，标记消息已读
  if (!document.hidden && relationshipId.value && relationshipId.value !== 'new') {
    markAsRead()
  }
}

onMounted(async () => {
  await loadChatInfo()
  await loadMessages()
  // 消息加载完成后标记已读
  await markAsRead()
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 建立 WebSocket 连接（如果后端支持）
  // connectWebSocket()
  
  // 清理录音资源
  return () => {
    disconnectWebSocket()
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    if (recordTimer.value) {
      clearInterval(recordTimer.value)
    }
    if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
      mediaRecorder.value.stop()
    }
  }
})
</script>

<style scoped>
.chat-window {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  background: #fff;
  z-index: 1000;
}

/* 返回按钮样式已移到 header-back-button */

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
  height: 100%;
  min-height: 0;
  position: relative;
  max-height: 100vh;
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
  min-height: 56px;
}

.header-back-button {
  flex-shrink: 0;
  padding: 8px 12px;
  margin-right: 12px;
}

.header-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: absolute;
  left: 0;
  right: 0;
  pointer-events: none;
}

.header-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.2;
}

.online-status {
  font-size: 11px;
  color: #67c23a;
  margin-top: 2px;
}

.header-right-placeholder {
  width: 80px;
  flex-shrink: 0;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 20px;
  background: #f5f7fa;
  min-height: 0;
  display: flex;
  flex-direction: column;
  -webkit-overflow-scrolling: touch; /* iOS 平滑滚动 */
  position: relative;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 4px;
  align-items: flex-start;
  width: 100%;
}

.message-item.message-right {
  flex-direction: row;
  justify-content: flex-end;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 65%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-item.message-right .message-content {
  align-items: flex-end;
}

.message-item.message-right .message-bubble {
  background: #409eff;
  color: #fff;
}

/* 右侧消息中的特殊消息类型样式 */
.message-item.message-right .message-voice,
.message-item.message-right .message-file {
  background: rgba(255, 255, 255, 0.15);
}

.message-item.message-right .file-icon-wrapper {
  background: rgba(255, 255, 255, 0.2);
}

.message-item.message-right .voice-play-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.message-item.message-right .file-download-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  word-wrap: break-word;
  max-width: 100%;
}

/* 特殊消息类型不需要额外的padding */
.message-bubble .message-image-wrapper,
.message-bubble .message-voice,
.message-bubble .message-file {
  margin: -10px -14px;
  padding: 0;
}

.message-bubble .message-image-wrapper {
  margin: -10px -14px;
  border-radius: 12px;
}

.bubble-left {
  background: #fff;
  border: 1px solid #ebeef5;
}

.bubble-right {
  background: #409eff;
  color: #fff;
}

.message-text {
  margin: 0;
  line-height: 1.5;
}

/* 图片消息样式 */
.message-image-wrapper {
  max-width: 250px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.message-image-wrapper:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: scale(1.02);
}

.message-image {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 8px;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  background: #f5f7fa;
  min-height: 100px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.image-error span {
  font-size: 12px;
}

/* 语音消息样式 */
.message-voice {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  min-width: 180px;
  max-width: 280px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  transition: all 0.3s ease;
}

.bubble-right .message-voice {
  background: rgba(255, 255, 255, 0.2);
}

.voice-play-btn {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  transition: all 0.3s ease;
}

.voice-play-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.voice-play-btn.playing {
  background: rgba(255, 255, 255, 0.3);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.voice-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.voice-waveform {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 24px;
  padding: 0 4px;
}

.wave-bar {
  width: 3px;
  background: currentColor;
  border-radius: 2px;
  opacity: 0.4;
  height: 8px;
  transition: all 0.2s ease;
}

.voice-waveform.playing .wave-bar {
  animation: waveAnimation 0.8s ease-in-out infinite;
  opacity: 0.8;
}

.voice-waveform.playing .wave-bar:nth-child(1) { animation-delay: 0s; }
.voice-waveform.playing .wave-bar:nth-child(2) { animation-delay: 0.1s; }
.voice-waveform.playing .wave-bar:nth-child(3) { animation-delay: 0.2s; }
.voice-waveform.playing .wave-bar:nth-child(4) { animation-delay: 0.3s; }
.voice-waveform.playing .wave-bar:nth-child(5) { animation-delay: 0.4s; }

@keyframes waveAnimation {
  0%, 100% {
    height: 8px;
    opacity: 0.4;
  }
  50% {
    height: 20px;
    opacity: 1;
  }
}

.voice-duration {
  font-size: 11px;
  opacity: 0.85;
  font-weight: 500;
}

.voice-audio {
  display: none;
}

/* 文件消息样式 */
.message-file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  min-width: 200px;
  max-width: 320px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.bubble-right .message-file {
  background: rgba(255, 255, 255, 0.15);
}

.message-file:hover {
  background: rgba(255, 255, 255, 0.1);
}

.bubble-right .message-file:hover {
  background: rgba(255, 255, 255, 0.2);
}

.file-icon-wrapper {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  transition: all 0.3s ease;
}

.message-file:hover .file-icon-wrapper {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.file-icon {
  font-size: 24px;
  color: currentColor;
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.file-size {
  font-size: 11px;
  opacity: 0.7;
}

.file-download-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  transition: all 0.3s ease;
}

.file-download-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

/* 居中显示的时间（类似微信） */
.message-time-center {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin: 10px 0 6px 0;
  padding: 0 12px;
  line-height: 1.5;
}

/* 撤回按钮区域 */
.message-recall {
  text-align: center;
  margin-top: 4px;
  padding: 0 4px;
}

.message-recall .el-button {
  font-size: 12px;
  color: #909399;
}

.date-divider {
  text-align: center;
  margin: 16px 0;
  color: #909399;
  font-size: 12px;
  position: relative;
}

.date-divider::before,
.date-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30%;
  height: 1px;
  background: #ebeef5;
}

.date-divider::before {
  left: 0;
}

.date-divider::after {
  right: 0;
}

.input-area {
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
  width: 100%;
}

/* 加号按钮样式 */
.add-button-dropdown {
  flex-shrink: 0;
}

.add-button {
  width: 36px;
  height: 36px;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  transition: all 0.3s ease;
}

.add-button:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}

.add-button:focus {
  border-color: #409eff;
  color: #409eff;
}

/* 下拉菜单样式 */
.add-button-dropdown :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
}

.add-button-dropdown :deep(.el-dropdown-menu__item .el-icon) {
  font-size: 18px;
}

.add-button-dropdown :deep(.el-dropdown-menu__item span) {
  font-size: 14px;
}

.voice-record-btn {
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.voice-record-btn.recording {
  color: #ef4444 !important;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.voice-record-area {
  padding: 20px;
  background: #f9fafb;
  border-top: 1px solid #ebeef5;
}

.record-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.record-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #ef4444;
  animation: pulse 1.5s ease-in-out infinite;
}

.record-time {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  font-variant-numeric: tabular-nums;
}

.record-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.record-actions .el-button {
  border-radius: 12px;
  padding: 10px 24px;
  min-width: 100px;
}

.input-box {
  padding: 10px 16px;
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  resize: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  line-height: 1.5;
  max-height: 120px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  transition: border-color 0.2s;
}

.message-input :deep(.el-textarea__inner):focus {
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff inset;
}

.message-input :deep(.el-textarea__inner)::placeholder {
  color: #a8abb2;
}

/* 移动端优化 */
@media (max-width: 767px) {
  .chat-window {
    height: 100vh;
    height: -webkit-fill-available; /* iOS Safari */
  }

  .chat-container {
    height: 100%;
    max-height: 100vh;
    max-height: -webkit-fill-available; /* iOS Safari */
  }

  .input-area {
    position: relative;
    flex-shrink: 0;
    background: #fff;
    width: 100%;
  }

  .messages-container {
    flex: 1 1 auto;
    min-height: 0;
    overflow-y: auto;
  }

  .input-box {
    padding: 8px 12px;
  }
  
  .message-input :deep(.el-textarea__inner) {
    padding: 6px 14px;
    font-size: 16px; /* 防止iOS自动缩放 */
  }
  
  .add-button {
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 767px) {
  .chat-window {
    height: 100vh;
    height: -webkit-fill-available; /* iOS Safari */
    padding: 0;
  }

  .chat-container {
    border-radius: 16px;
    height: 100%;
    max-height: 100vh;
    max-height: -webkit-fill-available; /* iOS Safari */
  }

  .chat-header {
    padding: 12px 16px;
    flex-wrap: wrap;
  }

  .header-actions {
    margin-top: 8px;
    width: 100%;
    justify-content: flex-end;
  }

  .message-content {
    max-width: 85%;
  }

  .message-image {
    max-width: 150px;
  }

  .message-voice {
    min-width: 150px;
    padding: 6px 10px;
  }

  .voice-waveform {
    height: 16px;
  }

  .wave-bar {
    width: 2px;
    height: 6px;
  }

  .add-button {
    width: 32px;
    height: 32px;
  }

  .voice-record-area {
    padding: 16px;
  }

  .record-time {
    font-size: 16px;
  }

  .record-actions .el-button {
    min-width: 80px;
    padding: 8px 16px;
  }
}

@media (max-width: 480px) {
  .chat-window {
    height: 100vh;
    height: -webkit-fill-available; /* iOS Safari */
  }

  .message-voice {
    min-width: 120px;
  }

  .voice-info {
    gap: 2px;
  }
}
</style>

