<template>
  <!-- TODO -->
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">批量创建</h2>
    <!-- 图片信息表单 -->
    <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
      <a-form-item name="searchText" label="关键词">
        <a-input v-model:value="formData.searchText" placeholder="请输入关键词" />
      </a-form-item>
      <a-form-item name="count" label="抓取数量">
        <a-input-number v-model:value="formData.count"></a-input-number
      ></a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})

const loading = ref(false)
const router = useRouter()

const handleSubmit = async () => {
  loading.value = true
}
</script>

<style scoped></style>
