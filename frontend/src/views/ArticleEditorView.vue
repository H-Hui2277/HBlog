<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { articleApi, uploadApi } from '../services/api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isEditing = ref(false)
const articleId = ref(null)
const loading = ref(false)
const saving = ref(false)
const error = ref('')

const title = ref('')
const content = ref('')
const quillInstance = ref(null)
const image = ref(null)
const imagePreview = ref('')
const removeImage = ref(false)

const MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB

const quillOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      [{ header: [1, 2, 3, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ color: [] }, { background: [] }],
      ['link', 'image'],
      ['clean']
    ]
  }
}

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  articleId.value = route.params.id
  isEditing.value = !!articleId.value

  if (isEditing.value) {
    await loadArticle()
  }
})

onBeforeUnmount(() => {
  // Cleanup
})

async function loadArticle() {
  loading.value = true
  try {
    const response = await articleApi.getById(articleId.value)
    const article = response.data
    title.value = article.title
    content.value = article.content || ''
    if (article.imageUrl) {
      imagePreview.value = article.imageUrl
    }
  } catch (err) {
    error.value = '加载文章失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

function handleImageChange(e) {
  const file = e.target.files[0]
  if (!file) return

  // Validate size
  if (file.size > MAX_IMAGE_SIZE) {
    alert('图片大小不能超过5MB')
    e.target.value = ''
    return
  }

  // Validate type
  if (!file.type.startsWith('image/')) {
    alert('只能上传图片文件')
    e.target.value = ''
    return
  }

  image.value = file
  removeImage.value = false

  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

function removeCurrentImage() {
  image.value = null
  imagePreview.value = ''
  removeImage.value = true
}

function handleQuillReady(quill) {
  quillInstance.value = quill

  // Get the toolbar and add custom image handler
  const toolbar = quill.getModule('toolbar')
  if (toolbar) {
    toolbar.addHandler('image', () => {
      const input = document.createElement('input')
      input.setAttribute('type', 'file')
      input.setAttribute('accept', 'image/*')
      input.click()

      input.onchange = async () => {
        const file = input.files[0]
        if (!file) return

        // Validate size
        if (file.size > MAX_IMAGE_SIZE) {
          alert('图片大小不能超过5MB')
          return
        }

        try {
          const formData = new FormData()
          formData.append('file', file)
          const response = await uploadApi.uploadImage(formData)
          const url = response.data.url

          const range = quill.getSelection(true)
          // Insert image URL
          quill.insertEmbed(range.index, 'image', url)
          quill.setSelection(range.index + 1)
        } catch (err) {
          console.error('Image upload failed:', err)
          alert('图片上传失败')
        }
      }
    })
  }

  // Add image resize functionality using native Quill API
  quill.root.addEventListener('click', function(e) {
    if (e.target.tagName === 'IMG') {
      // Create resize handle if not exists
      const img = e.target
      if (!img.dataset.resizable) {
        img.dataset.resizable = 'true'
        makeImageResizable(quill, img)
      }
    }
  })
}

function makeImageResizable(quill, img) {
  // Add click handler to show resize options
  img.style.cursor = 'nwse-resize'

  let startX, startY, startWidth, startHeight

  img.addEventListener('mousedown', function(e) {
    e.preventDefault()
    startX = e.clientX
    startY = e.clientY
    startWidth = img.width
    startHeight = img.height

    const onMouseMove = (e) => {
      const newWidth = startWidth + (e.clientX - startX)
      const newHeight = startHeight + (e.clientY - startY)

      // Apply new size
      if (newWidth > 50) {
        img.style.width = newWidth + 'px'
      }
      if (newHeight > 50) {
        img.style.height = newHeight + 'px'
      }
    }

    const onMouseUp = () => {
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)

      // Update the image with new dimensions in the editor
      const alt = img.alt || ''
      const src = img.src
      const width = img.style.width
      const height = img.style.height

      // Find and update the Delta
      const delta = quill.getContents()
      delta.ops.forEach((op, index) => {
        if (op.insert && op.insert.image && op.insert.image.url === src) {
          const newAttributes = {
            ...op.insert.image,
            style: `width: ${width}; height: ${height};`
          }
          quill.updateContents({
            ops: [{ retain: index }, { insert: { image: newAttributes } }]
          })
        }
      })
    }

    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
  })
}

async function handleSubmit() {
  if (!title.value.trim()) {
    error.value = '请输入标题'
    return
  }

  error.value = ''
  saving.value = true

  try {
    const formData = new FormData()
    formData.append('title', title.value)
    formData.append('content', content.value)

    if (image.value) {
      formData.append('image', image.value)
    }

    if (removeImage.value) {
      formData.append('removeImage', 'true')
    }

    if (isEditing.value) {
      await articleApi.update(articleId.value, formData)
    } else {
      await articleApi.create(formData)
    }

    router.push('/admin')
  } catch (err) {
    error.value = err.response?.data?.message || '保存失败，请稍后重试'
    console.error(err)
  } finally {
    saving.value = false
  }
}

function handleCancel() {
  router.push('/admin')
}
</script>

<template>
  <div class="editor-view">
    <div class="editor-card">
      <header class="editor-header">
        <h1 class="editor-title">{{ isEditing ? '编辑文章' : '新建文章' }}</h1>
      </header>

      <div v-if="loading" class="loading">
        <span>加载中...</span>
      </div>

      <form v-else @submit.prevent="handleSubmit" class="editor-form">
        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div class="form-group">
          <label for="title">标题</label>
          <input
            id="title"
            v-model="title"
            type="text"
            placeholder="请输入文章标题"
            required
          />
        </div>

        <div class="form-group">
          <label>内容</label>
          <div class="quill-wrapper">
            <QuillEditor
              v-model:content="content"
              contentType="html"
              :options="quillOptions"
              @ready="handleQuillReady"
            />
          </div>
        </div>

        <div class="form-group">
          <label>封面图片 (最大5MB)</label>
          <div class="image-upload">
            <div v-if="imagePreview && !removeImage" class="image-preview">
              <img :src="imagePreview" alt="预览" />
              <button type="button" @click="removeCurrentImage" class="remove-image">
                ×
              </button>
            </div>
            <div v-else class="image-input">
              <input
                type="file"
                accept="image/*"
                @change="handleImageChange"
                id="image"
                class="file-input"
              />
              <label for="image" class="file-label">
                <span class="file-icon">📷</span>
                <span>点击上传封面图片</span>
              </label>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="handleCancel" class="btn btn-secondary">
            取消
          </button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.editor-view {
  max-width: 900px;
  margin: 0 auto;
}

.editor-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow);
}

.editor-header {
  margin-bottom: 2rem;
}

.editor-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

.editor-form {
  display: flex;
  flex-direction: column;
}

.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: #fef2f2;
  border-radius: var(--radius);
}

.quill-wrapper {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
}

.quill-wrapper :deep(.ql-toolbar) {
  border: none;
  border-bottom: 1px solid var(--border);
  background: var(--bg-secondary);
}

.quill-wrapper :deep(.ql-container) {
  border: none;
  min-height: 300px;
  font-size: 1rem;
}

.quill-wrapper :deep(.ql-editor) {
  min-height: 300px;
}

.quill-wrapper :deep(.ql-editor img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius);
}

.image-upload {
  margin-top: 0.5rem;
}

.image-preview {
  position: relative;
  display: inline-block;
  border-radius: var(--radius);
  overflow: hidden;
}

.image-preview img {
  max-width: 300px;
  max-height: 200px;
  display: block;
}

.remove-image {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition);
}

.remove-image:hover {
  background: rgba(239, 68, 68, 0.9);
}

.file-input {
  display: none;
}

.file-label {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  border: 2px dashed var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  color: var(--text-secondary);
  transition: var(--transition);
}

.file-label:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.file-icon {
  font-size: 1.25rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.form-actions .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
