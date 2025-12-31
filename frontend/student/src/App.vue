<!--
  根应用组件 (App.vue)
  功能：
  1. 渲染路由视图
  2. 提供路由切换的过渡动画
  3. 全局样式定义
-->
<template>
  <!-- 
    路由视图容器
    使用 transition 实现路由切换的过渡动画
    mode="out-in": 先退出旧组件，再进入新组件
    appear: 初始渲染时也应用过渡动画
  -->
  <router-view v-slot="{ Component, route }">
    <transition
      :name="route.meta.transition || 'fade'"
      mode="out-in"
      appear
    >
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
/**
 * 根应用组件 - Script Setup
 * 
 * 说明：
 * 这是 Vue 应用的根组件，主要作用是：
 * 1. 提供路由视图的容器
 * 2. 配置路由切换的过渡动画
 * 3. 定义全局样式
 */
</script>

<style>
/* ========== 全局样式重置 ========== */

/* 全局样式重置（移除所有元素的默认margin和padding，统一使用border-box盒模型） */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 应用根容器样式 */
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol',
    'Noto Color Emoji';
  -webkit-font-smoothing: antialiased; /* 字体平滑（macOS/iOS） */
  -moz-osx-font-smoothing: grayscale; /* 字体平滑（Firefox macOS） */
  color: #2c3e50;
  min-height: 100vh;
}

/* Body 样式重置 */
body {
  margin: 0;
  padding: 0;
}

/* ========== 全局工具类样式 ========== */

/* 容器工具类（最大宽度限制，居中显示） */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题工具类 */
.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

/* 卡片工具类 */
.card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

/* ========== 响应式工具类 ========== */

/* 移动端样式（屏幕宽度 <= 767px） */
@media (max-width: 767px) {
  .page-title {
    font-size: 20px;
  }

  .card {
    padding: 16px;
  }
}

/* ========== 路由过渡动画 ========== */

/**
 * 淡入淡出过渡动画（fade）
 * 用于路由切换时的默认过渡效果
 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: opacity; /* 性能优化 */
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/**
 * 滑动过渡动画（slide）
 * 用于需要滑动效果的路由切换
 */
.slide-enter-active,
.slide-leave-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: opacity, transform; /* 性能优化 */
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(20px) translateZ(0); /* GPU加速 */
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-20px) translateZ(0); /* GPU加速 */
}
</style>
