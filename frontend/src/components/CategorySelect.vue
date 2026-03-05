<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCategoryStore } from '../stores/category'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const categoryStore = useCategoryStore()
const loading = ref(true)

onMounted(async () => {
  await categoryStore.loadCategoryTree()
  loading.value = false
})

const flatCategories = computed(() => {
  return categoryStore.getFlatCategories()
})

function isSelected(id) {
  return props.modelValue.includes(id)
}

function toggleCategory(id) {
  const newValue = [...props.modelValue]
  const index = newValue.indexOf(id)
  if (index === -1) {
    newValue.push(id)
  } else {
    newValue.splice(index, 1)
  }
  emit('update:modelValue', newValue)
}

function getCategoryName(id) {
  const category = flatCategories.value.find(c => c.id === id)
  return category ? category.name : ''
}
</script>

<template>
  <div class="category-select">
    <div v-if="loading" class="loading">
      加载中...
    </div>
    <div v-else-if="flatCategories.length === 0" class="empty">
      暂无分类
    </div>
    <div v-else class="category-options">
      <label
        v-for="category in flatCategories"
        :key="category.id"
        class="category-option"
        :class="{ selected: isSelected(category.id) }"
      >
        <input
          type="checkbox"
          :checked="isSelected(category.id)"
          @change="toggleCategory(category.id)"
          class="checkbox"
        />
        <span class="category-name">{{ category.name }}</span>
      </label>
    </div>

    <div v-if="modelValue.length > 0" class="selected-tags">
      <span class="tags-label">已选:</span>
      <span
        v-for="id in modelValue"
        :key="id"
        class="tag"
      >
        {{ getCategoryName(id) }}
        <button @click="toggleCategory(id)" class="tag-remove">×</button>
      </span>
    </div>
  </div>
</template>

<style scoped>
.category-select {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 0.75rem;
  max-height: 200px;
  overflow-y: auto;
}

.loading,
.empty {
  color: var(--text-secondary);
  text-align: center;
  padding: 1rem;
}

.category-options {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.category-option {
  display: flex;
  align-items: center;
  padding: 0.5rem;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.2s;
}

.category-option:hover {
  background: var(--bg-secondary);
}

.category-option.selected {
  background: var(--primary);
  color: white;
}

.category-option.selected .category-name {
  color: white;
}

.checkbox {
  margin-right: 0.5rem;
  cursor: pointer;
}

.category-name {
  color: var(--text-primary);
}

.selected-tags {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--border);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
}

.tags-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.5rem;
  background: var(--primary);
  color: white;
  border-radius: var(--radius);
  font-size: 0.75rem;
}

.tag-remove {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  font-size: 1rem;
  line-height: 1;
}

.tag-remove:hover {
  opacity: 0.8;
}
</style>
