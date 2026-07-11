import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  createPayOrderApi,
  mockPaySuccessApi,
  getPayOrderApi,
  isPurchasedApi
} from '@/api/pay'

export const usePayStore = defineStore('pay', () => {
  const currentOrderId = ref('')
  const currentPayUrl = ref('')
  const currentAmount = ref(null)
  const currentStatus = ref(null)
  const loading = ref(false)

  async function createOrder(articleId) {
    loading.value = true
    try {
      const res = await createPayOrderApi(articleId)
      const data = res.data || {}
      currentOrderId.value = data.orderId
      currentPayUrl.value = data.payUrl
      currentAmount.value = data.totalAmount
      currentStatus.value = data.status
      return data
    } finally {
      loading.value = false
    }
  }

  async function mockSuccess(orderId) {
    await mockPaySuccessApi(orderId)
    currentStatus.value = 'PAY_SUCCESS'
  }

  async function refreshOrder(orderId) {
    const res = await getPayOrderApi(orderId)
    currentStatus.value = res.data?.status
    return res.data
  }

  async function isPurchased(articleId) {
    try {
      const res = await isPurchasedApi(articleId)
      return !!res.data
    } catch {
      return false
    }
  }

  function reset() {
    currentOrderId.value = ''
    currentPayUrl.value = ''
    currentAmount.value = null
    currentStatus.value = null
  }

  return {
    currentOrderId,
    currentPayUrl,
    currentAmount,
    currentStatus,
    loading,
    createOrder,
    mockSuccess,
    refreshOrder,
    isPurchased,
    reset
  }
})
