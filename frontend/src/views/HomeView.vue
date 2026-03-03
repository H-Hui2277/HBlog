<script setup>
import { ref, onMounted } from 'vue'
import { articleApi } from '../services/api'
import ArticleCard from '../components/ArticleCard.vue'

const articles = ref([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const response = await articleApi.getAll()
    articles.value = response.data
  } catch (err) {
    error.value = '加载文章失败，请稍后重试'
    console.error(err)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home">
    <header class="hero">
      <h1 class="hero-title">欢迎来到 HBlog</h1>
      <p class="hero-subtitle">分享想法，记录生活</p>
    </header>

    <div v-if="loading" class="loading">
      <span>加载中...</span>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="articles.length === 0" class="empty-state">
      <p>暂无文章</p>
      <p class="empty-hint">管理员可以登录后创建文章</p>
    </div>

    <div v-else class="articles-grid">
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
      />
    </div>
  </div>
</template>

<style scoped>
.hero {
  text-align: center;
  padding: 3rem 1rem;
  margin-bottom: 2rem;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 2rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 1rem;
  color: var(--text-secondary);
}

.empty-state p:first-child {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.error-message {
  text-align: center;
  padding: 2rem;
  color: #ef4444;
}
</style>
