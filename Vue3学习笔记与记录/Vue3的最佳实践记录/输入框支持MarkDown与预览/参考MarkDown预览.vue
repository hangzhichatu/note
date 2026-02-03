<template>
  <div class="post-content" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const props = defineProps<{ content: string }>()

// 安全地将 MD 转为 HTML
const renderedContent = computed(() => {
  const dirtyHtml = marked(props.content || '')
  return DOMPurify.sanitize(dirtyHtml)
})
</script>

<style scoped>
/* 可选：引入 highlight.js 样式 */
@import 'highlight.js/styles/github.css';

.post-content {
  line-height: 1.8;
  font-size: 1.05rem;
  color: #333;
}

.post-content :deep(h1),
.post-content :deep(h2),
.post-content :deep(h3) {
  margin-top: 1.5em;
  margin-bottom: 0.8em;
}

.post-content :deep(pre) {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
  overflow-x: auto;
}

.post-content :deep(code) {
  font-family: ui-monospace, SFMono-Regular, monospace;
}
</style>