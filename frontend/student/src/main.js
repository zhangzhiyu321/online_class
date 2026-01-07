import './assets/main.css'
import './styles/list-pages.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { ElMessage, ElNotification } from 'element-plus'

import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { 
  locale: zhCn
})

// 配置 ElMessage 和 ElNotification 的默认持续时间
// 保存原始方法
const originalMessageSuccess = ElMessage.success
const originalMessageError = ElMessage.error
const originalMessageWarning = ElMessage.warning
const originalMessageInfo = ElMessage.info

// 包装方法以添加默认持续时间
ElMessage.success = (options) => {
  if (typeof options === 'string') {
    return originalMessageSuccess({ message: options, duration: 5000 })
  }
  return originalMessageSuccess({ duration: 5000, ...options })
}

ElMessage.error = (options) => {
  if (typeof options === 'string') {
    return originalMessageError({ message: options, duration: 5000 })
  }
  return originalMessageError({ duration: 5000, ...options })
}

ElMessage.warning = (options) => {
  if (typeof options === 'string') {
    return originalMessageWarning({ message: options, duration: 5000 })
  }
  return originalMessageWarning({ duration: 5000, ...options })
}

ElMessage.info = (options) => {
  if (typeof options === 'string') {
    return originalMessageInfo({ message: options, duration: 5000 })
  }
  return originalMessageInfo({ duration: 5000, ...options })
}

// 配置 ElNotification 的默认持续时间
const originalNotification = ElNotification
ElNotification.success = (options) => {
  if (typeof options === 'string') {
    return originalNotification({ title: options, duration: 5000, type: 'success' })
  }
  return originalNotification({ duration: 5000, type: 'success', ...options })
}

ElNotification.error = (options) => {
  if (typeof options === 'string') {
    return originalNotification({ title: options, duration: 5000, type: 'error' })
  }
  return originalNotification({ duration: 5000, type: 'error', ...options })
}

ElNotification.warning = (options) => {
  if (typeof options === 'string') {
    return originalNotification({ title: options, duration: 5000, type: 'warning' })
  }
  return originalNotification({ duration: 5000, type: 'warning', ...options })
}

ElNotification.info = (options) => {
  if (typeof options === 'string') {
    return originalNotification({ title: options, duration: 5000, type: 'info' })
  }
  return originalNotification({ duration: 5000, type: 'info', ...options })
}

// 初始化主题
const themeStore = useThemeStore()
themeStore.initTheme()

app.mount('#app')

// 设置消息触摸滑动关闭功能（手机端）
function setupMessageSwipeToClose() {
  // 只在移动设备上启用
  const isMobile = window.innerWidth <= 767
  
  if (!isMobile) return
  
  let touchStartY = 0
  let touchCurrentY = 0
  let currentElement = null
  let isDragging = false
  let originalTransform = ''
  let originalOpacity = ''
  
  // 处理触摸事件的通用函数
  function handleTouchStart(e) {
    const el = e.target.closest('.el-message, .el-notification')
    if (!el) return
    
    currentElement = el
    touchStartY = e.touches[0].clientY
    touchCurrentY = touchStartY
    isDragging = false
    originalTransform = el.style.transform || ''
    originalOpacity = el.style.opacity || ''
  }
  
  function handleTouchMove(e) {
    if (!currentElement) return
    
    touchCurrentY = e.touches[0].clientY
    const deltaY = touchCurrentY - touchStartY
    
    // 只处理向上滑动（负值）
    if (deltaY < 0) {
      isDragging = true
      const translateY = Math.max(deltaY, -200) // 限制最大滑动距离
      const opacity = Math.max(0, 1 + deltaY / 200) // 根据滑动距离调整透明度
      
      // 应用变换
      const baseTransform = originalTransform.includes('translateX(-50%)') 
        ? 'translateX(-50%)' 
        : originalTransform.includes('translateX') 
          ? originalTransform.match(/translateX\([^)]+\)/)?.[0] || ''
          : ''
      
      currentElement.style.transform = `${baseTransform} translateY(${translateY}px)`
      currentElement.style.opacity = opacity
    }
  }
  
  function handleTouchEnd() {
    if (!currentElement || !isDragging) {
      currentElement = null
      return
    }
    
    const deltaY = touchCurrentY - touchStartY
    const threshold = -50 // 滑动超过 50px 才关闭
    
    if (deltaY < threshold) {
      // 关闭消息/通知
      currentElement.classList.add('is-closing')
      
      // 等待动画完成后移除元素
      setTimeout(() => {
        if (currentElement && currentElement.parentNode) {
          currentElement.parentNode.removeChild(currentElement)
        }
      }, 300)
    } else {
      // 恢复原位置
      currentElement.style.transform = originalTransform
      currentElement.style.opacity = originalOpacity || '1'
    }
    
    currentElement = null
    isDragging = false
  }
  
  function handleTouchCancel() {
    if (currentElement && isDragging) {
      currentElement.style.transform = originalTransform
      currentElement.style.opacity = originalOpacity || '1'
    }
    currentElement = null
    isDragging = false
  }
  
  // 绑定事件
  document.addEventListener('touchstart', handleTouchStart, { passive: true })
  document.addEventListener('touchmove', handleTouchMove, { passive: true })
  document.addEventListener('touchend', handleTouchEnd, { passive: true })
  document.addEventListener('touchcancel', handleTouchCancel, { passive: true })
}

// 等待 DOM 加载完成后设置触摸滑动功能
if (typeof window !== 'undefined') {
  setTimeout(() => {
    setupMessageSwipeToClose()
  }, 100)
}
