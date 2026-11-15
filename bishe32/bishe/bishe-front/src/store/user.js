import { defineStore } from 'pinia'
import { getInfo, login as loginApi } from '@/api/auth'
import { useMallStore } from '@/store/mall'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('front_token') || '',
    userInfo: {}
  }),
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('front_token', token)
    },
    async login(payload) {
      const res = await loginApi(payload)
      this.setToken(res.token)
      await this.fetchUserInfo()
    },
    async fetchUserInfo() {
      const res = await getInfo()
      this.userInfo = res.user || {}
      return this.userInfo
    },
    logout() {
      const mallStore = useMallStore()
      const userId = this.userInfo?.userId
      if (userId) {
        sessionStorage.removeItem(`front_seen_promotion_${userId}`)
      }
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('front_token')
      mallStore.clearStat()
    }
  }
})
