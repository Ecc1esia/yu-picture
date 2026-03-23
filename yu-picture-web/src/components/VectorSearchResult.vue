<template>
  <div class="vector-search-result">
    <!-- 空状态 -->
    <a-empty v-if="!loading && results.length === 0" description="暂无相似图片" />

    <!-- 加载状态 -->
    <a-spin v-else-if="loading" tip="搜索中..." />

    <!-- 搜索结果列表 -->
    <a-list v-else :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }" :data-source="results" class="result-grid">
      <template #renderItem="{ item }">
        <a-list-item class="result-item">
          <a-card hoverable class="result-card" @click="onPictureClick(item)">
            <template #cover>
              <div class="image-wrapper">
                <img :alt="item.name" :src="item.thumbnailUrl ?? item.url" loading="lazy" class="result-image" />
                <div class="score-overlay">
                  <a-tag color="green">相似度: {{ (item.score * 100).toFixed(1) }}%</a-tag>
                </div>
              </div>
            </template>
            <a-card-meta :title="item.name">
              <template #description>
                <div class="result-info">
                  <span class="similarity-text">相似度: {{ (item.score * 100).toFixed(1) }}%</span>
                </div>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
export interface VectorSearchResultVO {
  pictureId: number
  name: string
  thumbnailUrl: string
  url: string
  score: number
}

interface Props {
  results: VectorSearchResultVO[]
  loading?: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'pictureClick', item: VectorSearchResultVO): void
}>()

const onPictureClick = (item: VectorSearchResultVO) => {
  emit('pictureClick', item)
}
</script>

<style scoped>
.vector-search-result {
  width: 100%;
}

.result-grid {
  width: 100%;
}

.result-item {
  margin-bottom: 16px;
}

.result-card {
  transition: transform 0.2s;
}

.result-card:hover {
  transform: translateY(-4px);
}

.image-wrapper {
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

.score-overlay {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

.similarity-text {
  color: #52c41a;
  font-weight: 500;
}

.result-info {
  margin-top: 8px;
}
</style>
