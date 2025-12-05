<template>
  <!--todo-->
  <div id="vipExchangePage">
    <h2 style="margin-bottom: 16px">会员码兑换</h2>

    <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
      <a-form-item name="vipCode" label="兑换码">
        <a-input v-model:value="formData.vipCode" placeholder="请输入会员码" allow-clear />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          兑换
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
// import { message } from 'ant-design-vue'
// import { reactive, ref } from 'vue'
// import { exchangeVipUsingPost } from '@/api/userController.ts'
// import { useRouter } from 'vue-router'

// const formData = reactive<API.VipExchangeRequest>({
//   vipCode: '',
// })

// const loading = ref(false)
// const router = useRouter()

// const handleSubmit = async () => {
//   // 校验兑换码是否为空
//   if (!formData.vipCode) {
//     message.error('请输入会员码')
//     return
//   }

//   loading.value = true

//   try {
//     const res = await exchangeVipUsingPost({ vipCode: formData.vipCode })

//     // 成功
//     if (res.downTask.code === 0 && res.data.data) {
//       message.success('兑换成功')
//       router.push({
//         path: '/',
//       })
//     } else {
//       message.error('兑换失败, ' + res.data.message)
//     }
//   } catch (error) {
//     message.error('兑换失败,请稍后重试')
//   } finally {
//     loading.value = false
//   }
// }
</script>

<style scoped>
#vipExchangePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
