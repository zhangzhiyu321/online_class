<template>
  <el-button
    :icon="currentTheme === 'dark' ? Sunny : Moon"
    circle
    @click="toggleTheme"
    class="theme-toggle"
    :title="currentTheme === 'dark' ? '切换到浅色模式' : '切换到深色模式'"
    :class="{ 'is-dark': currentTheme === 'dark' }"
  />
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { Sunny, Moon } from '@element-plus/icons-vue'

const themeStore = useThemeStore()
const currentTheme = computed(() => themeStore.currentTheme)

const toggleTheme = () => {
  themeStore.toggleTheme()
}

onMounted(() => {
  themeStore.initTheme()
})
</script>

<style scoped>
.theme-toggle {
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  color: #6b7280 !important;
}

.theme-toggle:hover {
  background: #f3f4f6 !important;
  border-color: #d1d5db !important;
  color: #1a1a1a !important;
  transform: scale(1.05);
}

.theme-toggle.is-dark {
  background: #374151 !important;
  border-color: #4b5563 !important;
  color: #d1d5db !important;
}

.theme-toggle.is-dark:hover {
  background: #4b5563 !important;
  border-color: #6b7280 !important;
  color: #f9fafb !important;
}
</style>

