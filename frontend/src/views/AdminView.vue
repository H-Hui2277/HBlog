<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '../services/api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const articles = ref([])
const loading = ref(true)
const error = ref('')
const deleting = ref(null)

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await loadArticles()
})

async function loadArticles() {
  try {
    const response = await articleApi.getAll()
    articles.value = response.data
  } catch (err) {
    error.value = '加载文章失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

function goToEdit(id) {
  router.push(`/admin/edit/${id}`)
}

function goToCreate() {
  router.push('/admin/edit')
}

async function handleDelete(id) {
  if (!confirm('确定要删除这篇文章吗？')) return

  deleting.value = id
  try {
    await articleApi.delete(id)
    articles.value = articles.value.filter(a => a.id !== id)
  } catch (err) {
    alert('删除失败')
    console.error(err)
  } finally {
    deleting.value = null
  }
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<template>
  <div class="admin-view">
    <header class="admin-header">
      <h1 class="admin-title">文章管理</h1>
      <button @click="goToCreate" class="btn btn-primary">
        + 新建文章
      </button>
    </header>

    <div v-if="loading" class="loading">
      <span>加载中...</span>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="articles.length === 0" class="empty-state">
      <p>暂无文章</p>
      <button @click="goToCreate" class="btn btn-primary">创建第一篇文章</button>
    </div>

    <div v-else class="articles-list">
      <table class="articles-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="article in articles" :key="article.id">
            <td class="article-title-cell">
              <router-link :to="`/article/${article.id}`" target="_blank">
                {{ article.title }}
              </router-link>
            </td>
            <td>{{ formatDate(article.createdAt) }}</td>
            <td>{{ formatDate(article.updatedAt) }}</td>
            <td class="actions-cell">
              <button @click="goToEdit(article.id)" class="btn btn-secondary btn-sm">
                编辑
              </button>
              <button
                @click="handleDelete(article.id)"
                class="btn btn-danger btn-sm"
                :disabled="deleting === article.id"
              >
                {{ deleting === article.id ? '删除中...' : '删除' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.admin-view {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow);
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.admin-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

.articles-table {
  width: 100%;
  border-collapse: collapse;
}

.articles-table th,
.articles-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border);
}

.articles-table th {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.articles-table td {
  color: var(--text-primary);
}

.article-title-cell a {
  color: var(--text-primary);
  font-weight: 500;
}

.article-title-cell a:hover {
  color: var(--primary);
}

.actions-cell {
  display: flex;
  gap: 0.5rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.error-message {
  color: #ef4444;
  text-align: center;
  padding: 2rem;
}
</style>
