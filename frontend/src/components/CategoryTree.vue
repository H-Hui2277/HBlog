<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  },
  level: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['edit', 'delete'])

const expandedNodes = ref(new Set())

function toggleExpand(id) {
  if (expandedNodes.value.has(id)) {
    expandedNodes.value.delete(id)
  } else {
    expandedNodes.value.add(id)
  }
}

function hasChildren(category) {
  return category.children && category.children.length > 0
}

function isExpanded(id) {
  return expandedNodes.value.has(id)
}

function handleEdit(category) {
  emit('edit', category)
}

function handleDelete(category) {
  emit('delete', category)
}
</script>

<template>
  <ul class="category-tree" :class="{ 'nested': level > 0 }">
    <li v-for="category in categories" :key="category.id" class="category-item">
      <div class="category-row">
        <button
          v-if="hasChildren(category)"
          class="expand-btn"
          @click="toggleExpand(category.id)"
        >
          <span :class="{ 'rotated': isExpanded(category.id) }">▶</span>
        </button>
        <span v-else class="expand-placeholder"></span>

        <span class="category-name">{{ category.name }}</span>
        <span class="category-slug">({{ category.slug }})</span>

        <div class="category-actions">
          <button @click="handleEdit(category)" class="action-btn edit-btn">
            编辑
          </button>
          <button @click="handleDelete(category)" class="action-btn delete-btn">
            删除
          </button>
        </div>
      </div>

      <CategoryTree
        v-if="hasChildren(category) && isExpanded(category.id)"
        :categories="category.children"
        :level="level + 1"
        @edit="handleEdit"
        @delete="handleDelete"
      />
    </li>
  </ul>
</template>

<style scoped>
.category-tree {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-tree.nested {
  padding-left: 1.5rem;
  border-left: 1px solid var(--border);
  margin-left: 0.5rem;
}

.category-item {
  margin-bottom: 0.25rem;
}

.category-row {
  display: flex;
  align-items: center;
  padding: 0.5rem;
  border-radius: var(--radius);
  transition: background 0.2s;
}

.category-row:hover {
  background: var(--bg-secondary);
}

.expand-btn {
  background: none;
  border: none;
  padding: 0.25rem;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 0.75rem;
  transition: transform 0.2s;
}

.expand-btn span {
  display: inline-block;
  transition: transform 0.2s;
}

.expand-btn span.rotated {
  transform: rotate(90deg);
}

.expand-placeholder {
  width: 1.5rem;
}

.category-name {
  font-weight: 500;
  color: var(--text-primary);
  margin-right: 0.5rem;
}

.category-slug {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.category-actions {
  margin-left: auto;
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border);
}

.edit-btn:hover {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}

.delete-btn {
  background: transparent;
  color: #ef4444;
  border: 1px solid #ef4444;
}

.delete-btn:hover {
  background: #ef4444;
  color: white;
}
</style>
