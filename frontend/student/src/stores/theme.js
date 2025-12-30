import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// 主题配置
const themes = {
  light: {
    name: '浅色主题',
    colors: {
      // 主色调
      primary: '#667eea',
      primaryHover: '#5568d3',
      primaryLight: '#e0e7ff',
      
      // 背景色
      bgPrimary: '#ffffff',
      bgSecondary: '#f9fafb',
      bgTertiary: '#f3f4f6',
      
      // 文本色
      textPrimary: '#1a1a1a',
      textSecondary: '#4b5563',
      textTertiary: '#6b7280',
      textDisabled: '#9ca3af',
      
      // 边框色
      borderPrimary: '#e5e7eb',
      borderSecondary: '#d1d5db',
      
      // 状态色
      success: '#10b981',
      warning: '#f59e0b',
      error: '#ef4444',
      info: '#3b82f6',
      
      // 阴影
      shadowSm: '0 1px 2px rgba(0, 0, 0, 0.05)',
      shadowMd: '0 4px 6px rgba(0, 0, 0, 0.1)',
      shadowLg: '0 10px 15px rgba(0, 0, 0, 0.1)',
      
      // 圆角
      radiusSm: '12px',
      radiusMd: '16px',
      radiusLg: '20px',
      radiusXl: '24px',
    }
  },
  dark: {
    name: '深色主题',
    colors: {
      // 主色调
      primary: '#818cf8',
      primaryHover: '#6366f1',
      primaryLight: '#312e81',
      
      // 背景色
      bgPrimary: '#1f2937',
      bgSecondary: '#111827',
      bgTertiary: '#0f172a',
      
      // 文本色
      textPrimary: '#f9fafb',
      textSecondary: '#d1d5db',
      textTertiary: '#9ca3af',
      textDisabled: '#6b7280',
      
      // 边框色
      borderPrimary: '#374151',
      borderSecondary: '#4b5563',
      
      // 状态色
      success: '#34d399',
      warning: '#fbbf24',
      error: '#f87171',
      info: '#60a5fa',
      
      // 阴影
      shadowSm: '0 1px 2px rgba(0, 0, 0, 0.3)',
      shadowMd: '0 4px 6px rgba(0, 0, 0, 0.4)',
      shadowLg: '0 10px 15px rgba(0, 0, 0, 0.5)',
      
      // 圆角
      radiusSm: '12px',
      radiusMd: '16px',
      radiusLg: '20px',
      radiusXl: '24px',
    }
  }
}

export const useThemeStore = defineStore('theme', () => {
  // 当前主题
  const currentTheme = ref('light')
  
  // 获取当前主题配置
  const getTheme = () => {
    return themes[currentTheme.value] || themes.light
  }
  
  // 获取CSS变量字符串
  const getCSSVariables = () => {
    const theme = getTheme()
    let css = ':root {\n'
    Object.entries(theme.colors).forEach(([key, value]) => {
      css += `  --color-${key}: ${value};\n`
    })
    css += '}'
    return css
  }
  
  // 应用主题
  const applyTheme = (themeName = currentTheme.value) => {
    currentTheme.value = themeName
    const theme = themes[themeName] || themes.light
    
    // 设置CSS变量
    const root = document.documentElement
    Object.entries(theme.colors).forEach(([key, value]) => {
      root.style.setProperty(`--color-${key}`, value)
    })
    
    // 保存到本地存储
    localStorage.setItem('theme', themeName)
    
    // 更新body类名
    document.body.className = document.body.className.replace(/theme-\w+/g, '')
    document.body.classList.add(`theme-${themeName}`)
  }
  
  // 切换主题
  const toggleTheme = () => {
    const newTheme = currentTheme.value === 'light' ? 'dark' : 'light'
    applyTheme(newTheme)
  }
  
  // 初始化主题
  const initTheme = () => {
    const savedTheme = localStorage.getItem('theme') || 'light'
    applyTheme(savedTheme)
  }
  
  // 监听主题变化，自动应用
  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  }, { immediate: false })
  
  return {
    currentTheme,
    themes,
    getTheme,
    getCSSVariables,
    applyTheme,
    toggleTheme,
    initTheme
  }
})

