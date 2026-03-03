<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const formattedDate = computed(() => {
  const date = new Date(props.article.createdAt)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

const summary = computed(() => {
  const content = props.article.content || ''
  // Strip HTML tags
  const stripped = content.replace(/<[^>]*>/g, '')
  return stripped.length > 150 ? stripped.substring(0, 150) + '...' : stripped
})

function goToArticle() {
  router.push(`/article/${props.article.id}`)
}
</script>

<template>
  <article class="article-card" @click="goToArticle">
    <div v-if="article.imageUrl" class="card-image">
      <img :src="article.imageUrl" :alt="article.title" />
    </div>
    <div class="card-content">
      <h2 class="card-title">{{ article.title }}</h2>
      <p class="card-summary">{{ summary }}</p>
      <div class="card-meta">
        <span class="card-date">{{ formattedDate }}</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.article-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  overflow: hidden;
  cursor: pointer;
  transition: var(--transition);
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: var(--transition);
}

.article-card:hover .card-image img {
  transform: scale(1.05);
}

.card-content {
  padding: 1.5rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.75rem;
  line-height: 1.4;
}

.card-summary {
  color: var(--text-secondary);
  font-size: 0.9375rem;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.card-date {
  color: var(--text-muted);
  font-size: 0.875rem;
}
</style>
