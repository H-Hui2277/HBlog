<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '../services/api'

const route = useRoute()
const article = ref(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const response = await articleApi.getById(route.params.id)
    article.value = response.data
  } catch (err) {
    error.value = '加载文章失败'
    console.error(err)
  } finally {
    loading.value = false
  }
})

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<template>
  <div class="article-view">
    <div v-if="loading" class="loading">
      <span>加载中...</span>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <article v-else class="article">
      <header class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="article-date">{{ formatDate(article.createdAt) }}</span>
        </div>
      </header>

      <div v-if="article.imageUrl" class="article-image">
        <img :src="article.imageUrl" :alt="article.title" />
      </div>

      <div class="article-content" v-html="article.content"></div>
    </article>

    <div class="article-footer">
      <router-link to="/" class="back-link">← 返回首页</router-link>
    </div>
  </div>
</template>

<style scoped>
.article-view {
  max-width: 800px;
  margin: 0 auto;
}

.article {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 2.5rem;
  box-shadow: var(--shadow);
}

.article-header {
  margin-bottom: 2rem;
}

.article-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
  margin-bottom: 1rem;
}

.article-meta {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.article-image {
  margin-bottom: 2rem;
  border-radius: var(--radius);
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: auto;
  display: block;
}

.article-content {
  color: var(--text-primary);
  line-height: 1.8;
  font-size: 1.0625rem;
}

.article-content :deep(p) {
  margin-bottom: 1.5rem;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius);
  margin: 1.5rem 0;
}

.article-footer {
  margin-top: 2rem;
}

.back-link {
  display: inline-flex;
  align-items: center;
  color: var(--text-secondary);
  font-weight: 500;
  transition: var(--transition);
}

.back-link:hover {
  color: var(--primary);
}

.error-message {
  text-align: center;
  padding: 2rem;
  color: #ef4444;
}
</style>
