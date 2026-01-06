<template>
  <el-button
    type="text"
    :icon="ArrowLeft"
    @click="handleBack"
    class="back-button"
  >
    返回
  </el-button>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const handleBack = () => {
  router.back()
}
</script>

<style scoped>
.back-button {
  margin-bottom: var(--spacing-lg, 20px);
  padding: var(--spacing-sm, 8px) var(--spacing-md, 16px);
  font-size: var(--font-size-body-small, 14px);
  color: #6b7280;
  border-radius: 8px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover {
  color: #3b82f6;
  background: #f3f4f6;
  transform: translateX(-4px);
}

@media (max-width: 767px) {
  .back-button {
    margin-bottom: var(--spacing-md, 16px);
    font-size: var(--font-size-body-small, 13px);
    padding: var(--spacing-sm, 8px) var(--spacing-md, 12px);
  }
}
</style>

