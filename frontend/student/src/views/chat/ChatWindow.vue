<template>
  <div class="chat-window">
    <el-button
      type="text"
      :icon="ArrowLeft"
      @click="$router.back()"
      class="back-button"
    >
      返回
    </el-button>

    <div class="chat-container">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="header-info">
          <el-avatar :src="chatInfo.avatar" :size="40">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="info-text">
            <h3>{{ chatInfo.name }}</h3>
            <span v-if="chatInfo.onlineStatus === 1" class="online-status">在线</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            :icon="Phone"
            circle
            @click="handleVoiceCall"
            :disabled="chatInfo.onlineStatus !== 1"
            title="语音通话"
          />
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="messagesContainer" class="messages-container">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ 'message-right': message.senderId === currentUserId }"
        >
          <el-avatar
            v-if="message.senderId !== currentUserId"
            :src="chatInfo.avatar"
            :size="32"
            class="message-avatar"
          />
          <div class="message-content">
            <div class="message-bubble" :class="getMessageClass(message)">
              <!-- 文本消息 -->
              <p v-if="message.messageType === 1" class="message-text">
                {{ message.content }}
              </p>
              <!-- 图片消息 -->
              <el-image
                v-else-if="message.messageType === 4"
                :src="getImageUrl(message)"
                :preview-src-list="[getImageUrl(message)]"
                fit="cover"
                class="message-image"
              />
              <!-- 语音消息 -->
              <div v-else-if="message.messageType === 3" class="message-voice">
                <el-button
                  :icon="isPlayingVoice === message.id ? VideoPause : VideoPlay"
                  circle
                  size="small"
                  @click="toggleVoicePlay(message)"
                  class="voice-play-btn"
                />
                <div class="voice-info">
                  <div class="voice-waveform" :class="{ playing: isPlayingVoice === message.id }">
                    <span v-for="i in 5" :key="i" class="wave-bar" :style="{ animationDelay: i * 0.1 + 's' }"></span>
                  </div>
                  <span class="voice-duration">{{ message.duration || 0 }}秒</span>
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
                <el-icon><Document /></el-icon>
                <span>{{ message.fileName }}</span>
                <el-link :href="getFileUrl(message)" target="_blank" type="primary">
                  下载
                </el-link>
              </div>
            </div>
            <div class="message-time">
              {{ formatMessageTime(message.createdAt) }}
              <el-button
                v-if="message.senderId === currentUserId && canRecall(message)"
                type="text"
                size="small"
                @click="handleRecall(message.id)"
              >
                撤回
              </el-button>
            </div>
          </div>
          <el-avatar
            v-if="message.senderId === currentUserId"
            :src="currentUserAvatar"
            :size="32"
            class="message-avatar"
          />
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-toolbar">
          <el-button
            type="text"
            :icon="Picture"
            @click="handleImageUpload"
            title="发送图片"
          />
          <el-button
            type="text"
            :icon="Document"
            @click="handleFileUpload"
            title="发送文件"
          />
          <el-button
            type="text"
            :icon="Microphone"
            @click="toggleVoiceRecord"
            :class="{ recording: isRecording }"
            title="语音消息"
            class="voice-record-btn"
          />
        </div>
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
            :rows="3"
            placeholder="输入消息..."
            @keyup.ctrl.enter="handleSend"
            @keyup.enter.exact="handleSend"
          />
          <div class="input-actions">
            <el-button 
              type="primary" 
              @click="handleSend" 
              :loading="sending"
              :disabled="!inputMessage.trim()"
              :icon="sending ? '' : 'Promotion'"
            >
              {{ sending ? '发送中...' : '发送' }}
            </el-button>
          </div>
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
import {
  ArrowLeft,
  User,
  Microphone,
  Document,
  Picture,
  Phone,
  VideoPlay,
  VideoPause,
  Promotion
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const relationshipId = ref(route.params.relationshipId)
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
  try {
    const data = await getChatMessages(relationshipId.value, {
      page: 1,
      pageSize: 50
    })
    messages.value = (data.list || []).reverse() // 反转以便从旧到新显示
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
  try {
    const content = JSON.parse(message.content)
    return content.image_url || content.file_url
  } catch {
    return message.fileUrl || ''
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

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const canRecall = (message) => {
  if (!message.createdAt) return false
  const now = new Date()
  const msgTime = new Date(message.createdAt)
  const diff = (now - msgTime) / 1000 / 60 // 分钟
  return diff < 2 && !message.isRecalled
}

const handleSend = async () => {
  if (!inputMessage.value.trim()) return

  sending.value = true
  try {
    await sendMessage({
      relationshipId: relationshipId.value,
      receiverId: chatInfo.value.userId,
      messageType: 1,
      content: inputMessage.value
    })
    inputMessage.value = ''
    loadMessages()
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
        await sendMessage({
          relationshipId: relationshipId.value,
          receiverId: chatInfo.value.userId,
          messageType: 4,
          content: JSON.stringify({
            image_url: imageUrl,
            thumbnail_url: imageUrl,
            width: img.width,
            height: img.height
          })
        })
        ElMessage.success('图片发送成功')
        loadMessages()
        sending.value = false
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
    const data = await getChatRelationship(relationshipId.value)
    chatInfo.value = {
      name: data.name || data.nickname || '用户',
      avatar: data.avatar || '',
      userId: data.userId || data.userId1 || data.userId2,
      onlineStatus: data.onlineStatus || 0
    }
  } catch (error) {
    console.error('加载聊天信息失败:', error)
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

onMounted(async () => {
  await loadChatInfo()
  loadMessages()
  // 标记消息已读
  markMessageRead(relationshipId.value)
  // 建立 WebSocket 连接（如果后端支持）
  // connectWebSocket()
  
  // 清理录音资源
  return () => {
    disconnectWebSocket()
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
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.back-button {
  margin-bottom: 12px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.info-text h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.online-status {
  font-size: 12px;
  color: #67c23a;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-item.message-right {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 8px;
  word-wrap: break-word;
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

.message-image {
  max-width: 200px;
  border-radius: 4px;
}

.message-voice {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  min-width: 200px;
}

.voice-play-btn {
  flex-shrink: 0;
}

.voice-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.voice-waveform {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 20px;
}

.wave-bar {
  width: 3px;
  background: currentColor;
  border-radius: 2px;
  opacity: 0.3;
  height: 8px;
  transition: height 0.1s ease;
}

.voice-waveform.playing .wave-bar {
  animation: waveAnimation 0.8s ease-in-out infinite;
  opacity: 0.6;
}

@keyframes waveAnimation {
  0%, 100% {
    height: 8px;
    opacity: 0.3;
  }
  50% {
    height: 16px;
    opacity: 1;
  }
}

.voice-duration {
  font-size: 12px;
  opacity: 0.8;
}

.voice-audio {
  display: none;
}

.message-file {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.message-item.message-right .message-time {
  text-align: left;
}

.input-area {
  border-top: 1px solid #ebeef5;
  background: #fff;
}

.input-toolbar {
  padding: 8px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
  padding: 12px 16px;
}

.input-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.input-actions .el-button {
  min-width: 80px;
}

@media (max-width: 767px) {
  .chat-window {
    height: calc(100vh - 180px);
    padding: 0 8px;
  }

  .chat-container {
    border-radius: 16px;
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

  .input-toolbar {
    padding: 6px 12px;
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
    height: calc(100vh - 200px);
  }

  .message-voice {
    min-width: 120px;
  }

  .voice-info {
    gap: 2px;
  }
}
</style>

