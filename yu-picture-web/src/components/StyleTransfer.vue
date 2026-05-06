<template>
  <a-modal
    v-model:open="visible"
    title="AI 风格迁移"
    width="720px"
    :footer="null"
    @cancel="handleClose"
  >
    <a-row :gutter="24">
      <a-col :span="12">
        <div class="image-panel">
          <h4>原始图片</h4>
          <img :src="pictureUrl" class="preview-img" />
        </div>
      </a-col>
      <a-col :span="12">
        <div class="image-panel">
          <h4>生成结果</h4>
          <div v-if="generating" class="loading-area">
            <a-spin tip="风格迁移中..." />
          </div>
          <img v-else-if="resultUrl" :src="resultUrl" class="preview-img" />
          <div v-else class="empty-area">选择风格后点击生成</div>
        </div>
      </a-col>
    </a-row>
    <div class="action-bar">
      <a-select v-model:value="selectedStyle" style="width: 200px" placeholder="选择目标风格">
        <a-select-option value="<photography>">摄影</a-select-option>
        <a-select-option value="<3d-cartoon>">3D卡通</a-select-option>
        <a-select-option value="<anime>">动漫</a-select-option>
        <a-select-option value="<oil-painting>">油画</a-select-option>
        <a-select-option value="<watercolor>">水彩</a-select-option>
        <a-select-option value="<sketch>">素描</a-select-option>
        <a-select-option value="<chinese-painting>">国画</a-select-option>
      </a-select>
      <a-button type="primary" :loading="generating" :disabled="!selectedStyle" @click="handleTransfer" style="margin-left: 12px">
        生成
      </a-button>
      <a-button v-if="resultUrl" type="primary" @click="handleApply" style="margin-left: 8px">
        应用结果
      </a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { createStyleTransferTask, getStyleTransferTask } from '@/api/aiController.ts'

const visible = ref(false)
const pictureUrl = ref('')
const pictureId = ref<number>(0)
const selectedStyle = ref('')
const generating = ref(false)
const resultUrl = ref('')

const open = (id: number, url: string) => {
  pictureId.value = id
  pictureUrl.value = url
  selectedStyle.value = ''
  resultUrl.value = ''
  visible.value = true
}

defineExpose({ open })

const handleTransfer = async () => {
  if (!selectedStyle.value) {
    message.warning('请选择目标风格')
    return
  }
  generating.value = true
  resultUrl.value = ''
  try {
    const res = await createStyleTransferTask({
      pictureId: pictureId.value,
      style: selectedStyle.value,
    })
    if (res.data.code === 0 && res.data.data?.output?.taskId) {
      await pollResult(res.data.data.output.taskId)
    } else {
      message.error('创建任务失败：' + (res.data.message ?? '未知错误'))
    }
  } catch (e) {
    message.error('请求失败：' + e)
  }
  generating.value = false
}

const pollResult = async (taskId: string): Promise<void> => {
  return new Promise((resolve) => {
    const poll = async () => {
      try {
        const res = await getStyleTransferTask(taskId)
        if (res.data.code === 0 && res.data.data) {
          const status = res.data.data.output?.taskStatus
          if (status === 'SUCCEEDED') {
            const results = res.data.data.output?.results
            if (results && results.length > 0 && results[0].url) {
              resultUrl.value = results[0].url
            }
            resolve()
            return
          } else if (status === 'FAILED') {
            message.error('风格迁移失败：' + (res.data.data.output?.message ?? '未知错误'))
            resolve()
            return
          }
        }
        setTimeout(poll, 3000)
      } catch (e) {
        message.error('查询任务失败：' + e)
        resolve()
      }
    }
    poll()
  })
}

const handleApply = () => {
  if (resultUrl.value) {
    window.open(`/add_picture?pictureUrl=${encodeURIComponent(resultUrl.value)}`, '_blank')
  }
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.image-panel {
  text-align: center;
}

.preview-img {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.loading-area {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-area {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
}

.action-bar {
  margin-top: 16px;
  text-align: center;
}
</style>
