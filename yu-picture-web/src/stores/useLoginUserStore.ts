import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController.ts'

/**
 * 存储登录用户信息状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  // 定义状态
  const loginUser = ref<API.LoginUserVO>({ userName: '未登录' })

  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
    // todo
    // 测试用户登录
    // setTimeout(() => {
    //   loginUser.value = { userName: '测试用户', id: '1' }
    // }, 3000)
  }

  // 定义登录用户
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
