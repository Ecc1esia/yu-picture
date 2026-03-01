<template>
  <div class="picture-list">
    <!-- 空状态 -->
    <a-empty v-if="!loading && dataList.length === 0" description="暂无图片" />

    <!-- 骨架屏加载状态 -->
    <a-skeleton v-else-if="loading" active :rows="{ count: 4 }" :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }">
      <template #default>
        <a-skeleton-image />
      </template>
    </a-skeleton>

    <!-- 图片列表 -->
    <a-list
      v-else
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      class="picture-grid"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item class="picture-item">
          <a-card
            hoverable
            class="picture-card"
            @click="doClickPicture(picture)"
          >
            <template #cover>
              <div class="image-wrapper">
                <img
                  :alt="picture.name"
                  :src="picture.thumbnailUrl ?? picture.url"
                  loading="lazy"
                  class="picture-image"
                />
                <div class="image-overlay">
                  <EyeOutlined class="preview-icon" />
                </div>
              </div>
            </template>
            <a-card-meta :title="picture.name" class="card-meta">
              <template #description>
                <a-flex wrap="wrap" :gap="4">
                  <a-tag color="blue" class="category-tag">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags?.slice(0, 3)" :key="tag" class="tag-item">
                    {{ tag }}
                  </a-tag>
                  <a-tag v-if="picture.tags?.length > 3" class="tag-item">
                    +{{ picture.tags.length - 3 }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>

            <template v-if="showOp" #actions>
              <a-tooltip title="分享">
                <ShareAltOutlined class="action-icon" @click="(e) => doShare(picture, e)" />
              </a-tooltip>
              <a-tooltip title="以图搜图">
                <SearchOutlined class="action-icon" @click="(e) => doSearch(picture, e)" />
              </a-tooltip>
              <a-tooltip v-if="canEdit" title="编辑">
                <EditOutlined class="action-icon" @click="(e) => doEdit(picture, e)" />
              </a-tooltip>
              <a-tooltip v-if="canDelete" title="删除">
                <DeleteOutlined class="action-icon delete-icon" @click="(e) => doDelete(picture, e)" />
              </a-tooltip>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal title="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  ShareAltOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { ref } from 'vue'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  canEdit?: boolean
  canDelete?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const router = useRouter()
// 跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

// 搜索
const doSearch = (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
const doEdit = (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  // 跳转时一定要携带 spaceId
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除数据
const doDelete = async (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败')
  }
}

// ----- 分享操作 ----
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>('')
// 分享
const doShare = (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped></style>
