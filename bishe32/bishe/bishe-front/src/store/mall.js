import { defineStore } from 'pinia'
import { listCart, listPendingReviews } from '@/api/mall'

export const useMallStore = defineStore('mall', {
  state: () => ({
    cartCount: 0,
    invalidCount: 0,
    reviewPendingCount: 0
  }),
  actions: {
    async refreshCartStat() {
      const res = await listCart()
      const items = res.data || []
      this.cartCount = items.filter(item => item.invalidFlag !== 1).length
      this.invalidCount = items.filter(item => item.invalidFlag === 1).length
      return items
    },
    async refreshReviewStat() {
      const res = await listPendingReviews()
      const items = res.data || []
      this.reviewPendingCount = items.length
      return items
    },
    clearStat() {
      this.cartCount = 0
      this.invalidCount = 0
      this.reviewPendingCount = 0
    }
  }
})
