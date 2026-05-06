<template>
  <a-modal
    v-model:open="visible"
    :title="isByPictureId ? '以图搜图' : '向量检索'"
    width="800px"
    :footer="null"
    @cancel="handleClose"
  >
    <div class="vector-search-content">
      <!-- 加载状态 -->
      <a-spin v-if="loading" tip="正在搜索相似图片，请稍候..." class="search-loading" />

      <template v-else>
        <!-- 查询图片预览区 -->
        <div v-if="queryPictureUrl" class="query-section">
          <div class="query-preview">
            <img :src="queryPictureUrl" alt="查询图片" class="query-image" />
            <div class="query-overlay">
              <span class="query-label">查询图片</span>
            </div>
          </div>
        </div>

        <!-- 上传区域 -->
        <div v-if="!isByPictureId" class="upload-section">
          <a-upload
            v-if="!uploadedFileUrl"
            Draggable
            :customRequest="handleUpload"
            :showUploadList="false"
            accept="image/*"
            class="upload-area"
          >
            <div class="upload-content">
              <CloudUploadOutlined class="upload-icon" />
              <p class="upload-text">点击或拖拽图片此处上传</p>
              <p class="upload-hint">支持 JPG、PNG、WebP 等格式</p>
            </div>
          </a-upload>

          <div v-if="uploadedFileUrl" class="preview-section">
            <div class="preview-wrapper">
              <img :src="uploadedFileUrl" alt="上传图片" class="preview-image" />
              <div class="preview-overlay">
                <a-button type="text" class="reupload-btn" @click="resetUpload">
                  <CloudUploadOutlined /> 重新上传
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div v-if="showResults" class="results-section">
          <a-divider>
            <span class="results-title">相似图片</span>
          </a-divider>

          <a-empty v-if="searchResults.length === 0" description="未找到相似图片" />

          <div v-else class="results-grid">
            <a-card
              v-for="item in searchResults"
              :key="item.pictureId"
              hoverable
              class="result-card"
              @click="onPictureClick(item)"
            >
              <template #cover>
                <div class="result-image-wrapper">
                  <img :alt="item.name" :src="item.thumbnailUrl ?? item.url" class="result-image" />
                  <div class="result-score">
                    <a-tag color="green" class="score-tag">
                      {{ (item.score ? item.score * 100 : 0).toFixed(1) }}%
                    </a-tag>
                  </div>
                </div>
              </template>
              <a-card-meta :title="item.name">
                <template #description>
                  <span class="similarity-text">相似度 {{ (item.score ? item.score * 100 : 0).toFixed(1) }}%</span>
                </template>
              </a-card-meta>
            </a-card>
          </div>
        </div>
      </template>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { CloudUploadOutlined } from '@ant-design/icons-vue'
import { searchVectorByUpload, searchVectorByPicture, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'

interface Props {
  spaceId: number
  pictureId?: number
}

const props = defineProps<Props>()

const visible = defineModel<boolean>('visible', { default: false })

const uploadedFileUrl = ref<string>('')
const searchResults = ref<API.VectorSearchResultVO[]>([])
const loading = ref(false)
const showResults = ref(false)
const queryPictureUrl = ref<string>('')

const isByPictureId = computed(() => !!props.pictureId)

watch(visible, async (newVal) => {
  if (newVal && props.pictureId) {
    await fetchPictureUrl(props.pictureId)
    doSearchByPictureId(props.pictureId)
  }
})

const fetchPictureUrl = async (pictureId: number) => {
  try {
    const res = await getPictureVoByIdUsingGet({ id: pictureId })
    if (res.data.code === 0 && res.data.data) {
      queryPictureUrl.value = res.data.data.thumbnailUrl ?? res.data.data.url ?? ''
    }
  } catch (error) {
    console.error('获取图片URL失败', error)
  }
}

const handleUpload = (options: any) => {
  const { file, onSuccess, onError } = options
  if (!(file instanceof File)) {
    onError(new Error('不是有效的文件'))
    return
  }
  // 释放之前的 blob URL
  if (uploadedFileUrl.value) {
    URL.revokeObjectURL(uploadedFileUrl.value)
  }
  uploadedFileUrl.value = URL.createObjectURL(file)
  doSearchByUpload(file)
  onSuccess(null, null)
}

const resetUpload = () => {
  if (uploadedFileUrl.value) {
    URL.revokeObjectURL(uploadedFileUrl.value)
  }
  uploadedFileUrl.value = ''
  searchResults.value = []
  showResults.value = false
}

const doSearchByUpload = async (file: File) => {
  loading.value = true
  showResults.value = true
  try {
    const res = await searchVectorByUpload(file, props.spaceId)
    if (res.data.code === 0 && res.data.data) {
      searchResults.value = res.data.data
    } else {
      message.error('搜索失败：' + res.data.message)
    }
  } catch (error) {
    message.error('搜索失败：' + error)
  }
  loading.value = false
}

const doSearchByPictureId = async (pictureId: number) => {
  loading.value = true
  showResults.value = true
  try {
    const res = await searchVectorByPicture(pictureId, props.spaceId)
    if (res.data.code === 0 && res.data.data) {
      searchResults.value = res.data.data
    } else {
      message.error('搜索失败：' + res.data.message)
    }
  } catch (error) {
    message.error('搜索失败：' + error)
  }
  loading.value = false
}

const onPictureClick = (item: API.VectorSearchResultVO) => {
  window.open(`/picture/${item.pictureId}`)
}

const handleClose = () => {
  resetUpload()
  queryPictureUrl.value = ''
  visible.value = false
}

defineExpose({
  open: (_pictureId?: number) => {
    visible.value = true
  },
})
</script>

<style scoped>
.vector-search-content {
  min-height: 300px;
  position: relative;
}

.search-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* 查询图片区域 */
.query-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.query-preview {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.query-preview:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.query-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.query-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  padding: 8px;
  text-align: center;
}

.query-label {
  color: white;
  font-size: 12px;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 24px;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.ant-upload-drag) {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  background: #fafafa;
  transition: all 0.3s;
}

.upload-area :deep(.ant-upload-drag:hover) {
  border-color: #1890ff;
  background: #f0f7ff;
}

.upload-content {
  padding: 40px 20px;
  text-align: center;
}

.upload-icon {
  font-size: 48px;
  color: #999;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.upload-hint {
  color: #999;
  font-size: 12px;
}

/* 预览区域 */
.preview-section {
  display: flex;
  justify-content: center;
}

.preview-wrapper {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.preview-wrapper:hover .preview-overlay {
  opacity: 1;
}

.reupload-btn {
  color: white;
}

/* 结果区域 */
.results-section {
  margin-top: 16px;
}

.results-title {
  color: #666;
  font-weight: 500;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.result-card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.result-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.result-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f5f5f5;
}

.result-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-score {
  position: absolute;
  top: 8px;
  right: 8px;
}

.score-tag {
  font-weight: 500;
}

.similarity-text {
  color: #52c41a;
  font-size: 12px;
}
</style>