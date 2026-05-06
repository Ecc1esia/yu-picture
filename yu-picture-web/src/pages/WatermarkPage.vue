<template>
  <div id="watermarkPage">
    <a-card title="隐形水印工具">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 嵌入水印 -->
        <a-tab-pane key="embed" tab="嵌入水印">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-upload
                :customRequest="handleEmbedUpload"
                :showUploadList="false"
                accept="image/*"
              >
                <div v-if="!embedSourceUrl" class="upload-placeholder">
                  <UploadOutlined class="upload-icon" />
                  <p>点击上传图片</p>
                </div>
                <img v-else :src="embedSourceUrl" class="preview-img" />
              </a-upload>
            </a-col>
            <a-col :span="12">
              <div class="result-panel">
                <h4>水印结果</h4>
                <img v-if="watermarkedUrl" :src="watermarkedUrl" class="preview-img" />
                <div v-else class="empty-hint">上传图片后输入水印文本</div>
              </div>
            </a-col>
          </a-row>
          <div class="action-bar">
            <a-input
              v-model:value="watermarkText"
              placeholder="输入水印内容（如用户ID、邮箱）"
              style="width: 300px"
            />
            <a-button
              type="primary"
              :disabled="!embedSourceUrl || !watermarkText.trim()"
              :loading="embedding"
              @click="handleEmbed"
              style="margin-left: 12px"
            >
              嵌入水印
            </a-button>
            <a-button
              v-if="watermarkedUrl"
              type="primary"
              @click="handleDownload"
              style="margin-left: 8px"
            >
              下载 PNG
            </a-button>
          </div>
        </a-tab-pane>

        <!-- 提取水印 -->
        <a-tab-pane key="extract" tab="提取水印">
          <div style="text-align: center">
            <a-upload
              :customRequest="handleExtractUpload"
              :showUploadList="false"
              accept="image/*"
            >
              <div v-if="!extractSourceUrl" class="upload-placeholder">
                <UploadOutlined class="upload-icon" />
                <p>上传疑似带水印的 PNG 图片</p>
              </div>
              <img v-else :src="extractSourceUrl" class="extract-preview" />
            </a-upload>
            <div v-if="extractSourceUrl" style="margin-top: 16px">
              <a-button type="primary" :loading="extracting" @click="handleExtract">
                提取水印
              </a-button>
            </div>
            <a-alert
              v-if="extractedText !== null"
              :type="extractedText ? 'success' : 'error'"
              :message="extractedText || '未检测到有效水印'"
              style="margin-top: 16px; max-width: 400px; margin-left: auto; margin-right: auto"
            />
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { embedWatermark, extractWatermark } from '@/utils/watermark.ts'

const activeTab = ref('embed')

// ---- 嵌入水印 ----
const embedSourceUrl = ref('')
const watermarkedUrl = ref('')
const watermarkText = ref('')
const embedding = ref(false)
const embedFile = ref<File>()

const handleEmbedUpload = (options: any) => {
  const { file } = options
  if (file instanceof File) {
    embedFile.value = file
    embedSourceUrl.value = URL.createObjectURL(file)
    watermarkedUrl.value = ''
  }
}

const handleEmbed = async () => {
  if (!embedSourceUrl.value || !watermarkText.value.trim()) return
  embedding.value = true
  try {
    const blob = await embedWatermark(embedSourceUrl.value, watermarkText.value.trim())
    watermarkedUrl.value = URL.createObjectURL(blob)
    message.success('水印嵌入成功')
  } catch (e) {
    message.error('水印嵌入失败：' + e)
  }
  embedding.value = false
}

const handleDownload = () => {
  if (watermarkedUrl.value) {
    const a = document.createElement('a')
    a.href = watermarkedUrl.value
    a.download = `watermarked-${Date.now()}.png`
    a.click()
  }
}

// ---- 提取水印 ----
const extractSourceUrl = ref('')
const extractedText = ref<string | null>(null)
const extracting = ref(false)
const extractFile = ref<File>()

const handleExtractUpload = (options: any) => {
  const { file } = options
  if (file instanceof File) {
    extractFile.value = file
    extractSourceUrl.value = URL.createObjectURL(file)
    extractedText.value = null
  }
}

const handleExtract = async () => {
  if (!extractSourceUrl.value) return
  extracting.value = true
  extractedText.value = null
  try {
    const text = await extractWatermark(extractSourceUrl.value)
    extractedText.value = '水印内容：' + text
    message.success('水印提取成功')
  } catch (e) {
    extractedText.value = ''
    message.error('水印提取失败：' + e)
  }
  extracting.value = false
}
</script>

<style scoped>
#watermarkPage {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.upload-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  color: #999;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.preview-img {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.result-panel {
  text-align: center;
}

.empty-hint {
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

.extract-preview {
  max-width: 400px;
  max-height: 300px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
</style>
