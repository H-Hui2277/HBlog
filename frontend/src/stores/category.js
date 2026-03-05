import { defineStore } from 'pinia'
import { ref } from 'vue'
import { categoryApi } from '../services/api'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([])
  const categoryTree = ref([])
  const loading = ref(false)
  const error = ref('')

  async function loadCategories() {
    loading.value = true
    error.value = ''
    try {
      const response = await categoryApi.getAll()
      categories.value = response.data
    } catch (err) {
      error.value = '加载分类失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  async function loadCategoryTree() {
    loading.value = true
    error.value = ''
    try {
      const response = await categoryApi.getTree()
      categoryTree.value = response.data
    } catch (err) {
      error.value = '加载分类树失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  async function createCategory(data) {
    try {
      const response = await categoryApi.create(data)
      await loadCategories()
      await loadCategoryTree()
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '创建分类失败'
      throw err
    }
  }

  async function updateCategory(id, data) {
    try {
      const response = await categoryApi.update(id, data)
      await loadCategories()
      await loadCategoryTree()
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '更新分类失败'
      throw err
    }
  }

  async function deleteCategory(id) {
    try {
      await categoryApi.delete(id)
      await loadCategories()
      await loadCategoryTree()
    } catch (err) {
      error.value = err.response?.data?.message || '删除分类失败'
      throw err
    }
  }

  function flattenTree(tree, result = []) {
    for (const item of tree) {
      result.push({ id: item.id, name: item.name, parentId: item.parentId })
      if (item.children && item.children.length > 0) {
        flattenTree(item.children, result)
      }
    }
    return result
  }

  function getFlatCategories() {
    return flattenTree(categoryTree.value)
  }

  return {
    categories,
    categoryTree,
    loading,
    error,
    loadCategories,
    loadCategoryTree,
    createCategory,
    updateCategory,
    deleteCategory,
    getFlatCategories
  }
})
