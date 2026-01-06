<template>
  <div class="page-header">
    <h1 class="page-title">{{ title }}</h1>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.page-header {
  margin-bottom: var(--spacing-lg, 20px);
}

.page-title {
  font-size: var(--font-size-h1, 28px);
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  letter-spacing: -0.5px;
}

@media (max-width: 767px) {
  .page-title {
    font-size: var(--font-size-h2, 24px);
  }
}
</style>

