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
    <!-- 移除过渡动画，页面直接显示 -->
    <component :is="Component" :key="route.path" />
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
/* 
 * 注意：全局样式重置和基础样式已在 assets/main.css 中定义
 * 已移除路由过渡动画，页面直接显示
 */
</style>
