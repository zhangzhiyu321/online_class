/**
 * 响应式工具函数
 * 提供设备类型检测和断点常量
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'

// 响应式断点常量
export const BREAKPOINTS = {
  MOBILE: 767,      // 手机端最大宽度
  TABLET: 1024,    // 平板端最大宽度
  // DESKTOP: 1025+  // 电脑端最小宽度
}

// 设备类型枚举
export const DEVICE_TYPES = {
  MOBILE: 'mobile',
  TABLET: 'tablet',
  DESKTOP: 'desktop'
}

/**
 * 响应式组合式函数
 * @returns {Object} 响应式对象，包含设备类型、断点等信息
 */
export function useResponsive() {
  const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1025)

  // 更新窗口宽度
  const updateWidth = () => {
    windowWidth.value = window.innerWidth
  }

  // 设备类型计算属性
  const deviceType = computed(() => {
    if (windowWidth.value <= BREAKPOINTS.MOBILE) {
      return DEVICE_TYPES.MOBILE
    } else if (windowWidth.value <= BREAKPOINTS.TABLET) {
      return DEVICE_TYPES.TABLET
    } else {
      return DEVICE_TYPES.DESKTOP
    }
  })

  // 是否为手机端
  const isMobile = computed(() => deviceType.value === DEVICE_TYPES.MOBILE)

  // 是否为平板端
  const isTablet = computed(() => deviceType.value === DEVICE_TYPES.TABLET)

  // 是否为电脑端
  const isDesktop = computed(() => deviceType.value === DEVICE_TYPES.DESKTOP)

  // 是否为移动设备（手机或平板）
  const isMobileDevice = computed(() => isMobile.value || isTablet.value)

  // 监听窗口大小变化
  onMounted(() => {
    if (typeof window !== 'undefined') {
      window.addEventListener('resize', updateWidth)
      updateWidth()
    }
  })

  onUnmounted(() => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('resize', updateWidth)
    }
  })

  return {
    windowWidth,
    deviceType,
    isMobile,
    isTablet,
    isDesktop,
    isMobileDevice,
    BREAKPOINTS,
    DEVICE_TYPES
  }
}

