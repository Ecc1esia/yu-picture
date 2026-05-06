<template>
  <div id="textToImagePage">
    <a-card title="AI 文生图">
      <a-row :gutter="24">
        <!-- 左侧：输入区 -->
        <a-col :span="12">
          <a-form layout="vertical">
            <a-form-item label="提示词" required>
              <a-textarea
                v-model:value="prompt"
                placeholder="描述你想要生成的图片，例如：一只可爱的橘猫在阳光下打盹"
                :rows="6"
                :maxLength="500"
                showCount
              />
            </a-form-item>
            <a-form-item label="风格">
              <a-select v-model:value="style">
                <a-select-option value="<auto>">自动</a-select-option>
                <a-select-option value="<photography>">摄影</a-select-option>
                <a-select-option value="<portrait>">人像</a-select-option>
                <a-select-option value="<3d-cartoon>">3D卡通</a-select-option>
                <a-select-option value="<anime>">动漫</a-select-option>
                <a-select-option value="<oil-painting>">油画</a-select-option>
                <a-select-option value="<watercolor>">水彩</a-select-option>
                <a-select-option value="<sketch>">素描</a-select-option>
                <a-select-option value="<chinese-painting>">国画</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="图片尺寸">
              <a-select v-model:value="size">
                <a-select-option value="1024*1024">1024×1024</a-select-option>
                <a-select-option value="720*1280">720×1280（竖屏）</a-select-option>
                <a-select-option value="1280*720">1280×720（横屏）</a-select-option>
              </a-select>
            </a-form-item>
            <a-button type="primary" :loading="loading" :disabled="!prompt.trim()" @click="handleGenerate">
              生成图片
            </a-button>
          </a-form>
        </a-col>

        <!-- 右侧：结果区 -->
        <a-col :span="12">
          <div class="result-area">
            <div v-if="loading" class="loading-placeholder">
              <a-spin tip="AI 正在生成中..." size="large">
                <div class="spin-content" />
              </a-spin>
            </div>
            <div v-else-if="generatedUrl" class="result-content">
              <a-image :src="generatedUrl" :width="360" />
              <div class="result-actions">
                <a-button type="primary" @click="handleSaveToSpace">保存到空间</a-button>
                <a-button @click="handleDownload" style="margin-left: 8px">下载图片</a-button>
              </div>
            </div>
            <div v-else class="empty-placeholder">
              <p>输入提示词并点击生成</p>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { createTextToImageTask, getTextToImageTask } from '@/api/aiController.ts'

const prompt = ref('')
const style = ref('<auto>')
const size = ref('1024*1024')
const loading = ref(false)
const generatedUrl = ref('')
const currentTaskId = ref('')

const handleGenerate = async () => {
  if (!prompt.value.trim()) {
    message.warning('请输入提示词')
    return
  }
  loading.value = true
  generatedUrl.value = ''
  try {
    const res = await createTextToImageTask({
      prompt: prompt.value.trim(),
      size: size.value,
      style: style.value,
      n: 1,
    })
    if (res.data.code === 0 && res.data.data?.output?.taskId) {
      currentTaskId.value = res.data.data.output.taskId
      await pollTaskResult(currentTaskId.value)
    } else {
      message.error('创建任务失败：' + (res.data.message ?? '未知错误'))
    }
  } catch (e) {
    message.error('请求失败：' + e)
  }
  loading.value = false
}

const pollTaskResult = async (taskId: string): Promise<void> => {
  return new Promise((resolve) => {
    const poll = async () => {
      try {
        const res = await getTextToImageTask(taskId)
        if (res.data.code === 0 && res.data.data) {
          const status = res.data.data.output?.taskStatus
          if (status === 'SUCCEEDED') {
            const results = res.data.data.output?.results
            if (results && results.length > 0 && results[0].url) {
              generatedUrl.value = results[0].url
            }
            resolve()
            return
          } else if (status === 'FAILED') {
            message.error('生成失败：' + (res.data.data.output?.message ?? '未知错误'))
            resolve()
            return
          }
        }
        // 继续轮询
        setTimeout(poll, 3000)
      } catch (e) {
        message.error('查询任务失败：' + e)
        resolve()
      }
    }
    poll()
  })
}

const handleSaveToSpace = () => {
  if (generatedUrl.value) {
    window.open(`/add_picture?pictureUrl=${encodeURIComponent(generatedUrl.value)}`, '_blank')
  }
}

const handleDownload = () => {
  if (generatedUrl.value) {
    const a = document.createElement('a')
    a.href = generatedUrl.value
    a.download = `ai-generated-${Date.now()}.png`
    a.click()
  }
}
</script>

<style scoped>
#textToImagePage {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.result-area {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
}

.loading-placeholder,
.empty-placeholder {
  text-align: center;
  color: #999;
}

.spin-content {
  height: 100px;
}

.result-content {
  text-align: center;
}

.result-actions {
  margin-top: 16px;
}
</style>
