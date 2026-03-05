<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '../stores/category'
import { useAuthStore } from '../stores/auth'
import CategoryTree from '../components/CategoryTree.vue'

const router = useRouter()
const authStore = useAuthStore()
const categoryStore = useCategoryStore()

const showModal = ref(false)
const isEditing = ref(false)
const editingCategory = ref(null)

const formData = ref({
  name: '',
  slug: '',
  parentId: null,
  sortOrder: 0
})

const loading = ref(false)
const error = ref('')
const deleting = ref(null)

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  await categoryStore.loadCategoryTree()
})

function openCreateModal() {
  isEditing.value = false
  editingCategory.value = null
  formData.value = {
    name: '',
    slug: '',
    parentId: null,
    sortOrder: 0
  }
  showModal.value = true
}

function openEditModal(category) {
  isEditing.value = true
  editingCategory.value = category
  formData.value = {
    name: category.name,
    slug: category.slug,
    parentId: category.parentId,
    sortOrder: category.sortOrder || 0
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  error.value = ''
}

async function handleSubmit() {
  if (!formData.value.name.trim()) {
    error.value = '请输入分类名称'
    return
  }

  loading.value = true
  error.value = ''

  try {
    if (isEditing.value) {
      await categoryStore.updateCategory(editingCategory.value.id, formData.value)
    } else {
      await categoryStore.createCategory(formData.value)
    }
    closeModal()
  } catch (err) {
    error.value = err.response?.data?.message || '操作失败'
  } finally {
    loading.value = false
  }
}

async function handleDelete(category) {
  if (!confirm(`确定要删除分类"${category.name}"吗？`)) {
    return
  }

  deleting.value = category.id
  try {
    await categoryStore.deleteCategory(category.id)
  } catch (err) {
    alert(err.response?.data?.message || '删除失败')
  } finally {
    deleting.value = null
  }
}

function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<template>
  <div class="category-view">
    <header class="page-header">
      <h1 class="page-title">分类管理</h1>
      <button @click="openCreateModal" class="btn btn-primary">
        + 新建分类
      </button>
    </header>

    <div v-if="categoryStore.loading" class="loading">
      加载中...
    </div>

    <div v-else-if="categoryStore.categoryTree.length === 0" class="empty-state">
      <p>暂无分类</p>
      <button @click="openCreateModal" class="btn btn-primary">创建第一个分类</button>
    </div>

    <div v-else class="category-list">
      <CategoryTree
        :categories="categoryStore.categoryTree"
        @edit="openEditModal"
        @delete="handleDelete"
      />
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <header class="modal-header">
          <h2>{{ isEditing ? '编辑分类' : '新建分类' }}</h2>
          <button @click="closeModal" class="close-btn">×</button>
        </header>

        <form @submit.prevent="handleSubmit" class="modal-form">
          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <div class="form-group">
            <label for="name">名称</label>
            <input
              id="name"
              v-model="formData.name"
              type="text"
              placeholder="请输入分类名称"
              required
            />
          </div>

          <div class="form-group">
            <label for="slug">Slug</label>
            <input
              id="slug"
              v-model="formData.slug"
              type="text"
              placeholder="URL别名（可选，自动生成）"
            />
          </div>

          <div class="form-group">
            <label for="parent">父分类</label>
            <select id="parent" v-model="formData.parentId">
              <option :value="null">无（顶级分类）</option>
              <option
                v-for="cat in categoryStore.categories"
                :key="cat.id"
                :value="cat.id"
                :disabled="isEditing && cat.id === editingCategory?.id"
              >
                {{ cat.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="sortOrder">排序</label>
            <input
              id="sortOrder"
              v-model.number="formData.sortOrder"
              type="number"
              min="0"
            />
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn btn-secondary">
              取消
            </button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category-view {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

.category-list {
  margin-top: 1rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 500px;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border);
}

.modal-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: var(--text-primary);
}

.modal-form {
  padding: 1.5rem;
}

.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: #fef2f2;
  border-radius: var(--radius);
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--text-primary);
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  font-size: 1rem;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--primary);
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}
</style>
