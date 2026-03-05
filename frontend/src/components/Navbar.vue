<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)

function handleLogout() {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-container">
      <router-link to="/" class="logo">
        <span class="logo-icon">✍</span>
        <span class="logo-text">HBlog</span>
      </router-link>

      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <template v-if="isAuthenticated">
          <router-link to="/admin" class="nav-link">文章</router-link>
          <router-link to="/admin/categories" class="nav-link">分类</router-link>
          <button @click="handleLogout" class="nav-link btn-logout">退出</button>
        </template>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.logo-icon {
  font-size: 1.75rem;
}

.logo:hover {
  color: var(--primary);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-link {
  color: var(--text-secondary);
  font-weight: 500;
  transition: var(--transition);
  background: none;
  font-size: 1rem;
}

.nav-link:hover {
  color: var(--primary);
}

.btn-logout {
  color: var(--text-secondary);
}

.btn-logout:hover {
  color: #ef4444;
}
</style>
