import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('adminToken') || '')

  const isAuthenticated = computed(() => !!token.value)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('adminToken', newToken)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem('adminToken')
  }

  return {
    token,
    isAuthenticated,
    setToken,
    logout
  }
})
