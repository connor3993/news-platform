<template>
  <div class="checkout-page pad">
    <div class="back-bar" @click="goBack"><span>‹ 返回文章</span></div>

    <div v-if="loading" class="loading">加载中…</div>

    <div v-else-if="!orderId" class="empty">
      <p>订单不存在</p>
      <button class="btn" @click="$router.push('/home')">返回首页</button>
    </div>

    <div v-else class="checkout-card">
      <h2 class="ck-title">确认支付</h2>
      <div class="ck-row"><span>订单号</span><span class="mono">{{ orderId }}</span></div>
      <div class="ck-row"><span>状态</span><span :class="`status-${status}`">{{ statusText }}</span></div>
      <div class="ck-row ck-amount">
        <span>应付金额</span>
        <span class="amount">¥ {{ formatAmount(amount) }}</span>
      </div>

      <div v-if="status === 'PAY_SUCCESS'" class="ck-success">
        ✅ 支付成功！正在跳回文章…
      </div>
      <div v-else-if="status === 'CLOSE'" class="ck-closed">
        ⏰ 订单已超时关单，请返回文章重新下单。
      </div>
      <div v-else class="ck-actions">
        <button class="btn btn-primary btn-block" :disabled="acting" @click="mockPay">
          {{ acting ? '处理中…' : '我已支付（mock）' }}
        </button>
        <p class="ck-tip">当前为 mock 模式：点击按钮即模拟支付成功。生产环境会跳到支付宝收银台。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePayStore } from '@/stores/pay'
import { toast } from '@/utils/message'
import { formatAmount } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const payStore = usePayStore()

const orderId = computed(() => route.query.orderId || payStore.currentOrderId)
const amount = ref(payStore.currentAmount)
const status = ref(payStore.currentStatus)
const loading = ref(true)
const acting = ref(false)
let pollTimer = null

const statusText = computed(() => ({
  CREATE: '创建完成',
  PAY_WAIT: '等待支付',
  PAY_SUCCESS: '支付成功',
  CLOSE: '已关单'
}[status.value] || '未知'))

function goBack() {
  router.back()
}

async function refresh() {
  if (!orderId.value) {
    loading.value = false
    return
  }
  try {
    const data = await payStore.refreshOrder(orderId.value)
    status.value = data?.status
    amount.value = data?.totalAmount || amount.value
  } catch {
    // 订单不存在等情况
  } finally {
    loading.value = false
  }
}

async function mockPay() {
  acting.value = true
  try {
    await payStore.mockSuccess(orderId.value)
    toast('支付成功', 'info')
    status.value = 'PAY_SUCCESS'
    stopPoll()
    setTimeout(() => {
      // 跳回文章详情，强制刷新
      const articleId = route.query.articleId
      if (articleId) {
        router.replace({ path: `/article/${articleId}`, query: { paid: 1 } })
      } else {
        router.replace('/home')
      }
    }, 800)
  } catch (e) {
    toast('支付失败：' + (e.message || '未知错误'), 'error')
  } finally {
    acting.value = false
  }
}

function startPoll() {
  // 每 2 秒拉一次订单状态，便于检测超时关单
  pollTimer = setInterval(refresh, 2000)
}
function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(async () => {
  await refresh()
  if (status.value !== 'PAY_SUCCESS' && status.value !== 'CLOSE') {
    startPoll()
  }
})
onBeforeUnmount(stopPoll)
</script>

<style scoped>
.checkout-page { max-width: 480px; margin: 0 auto; }
.back-bar { position: sticky; top: 52px; z-index: 30; background: #fff; border-bottom: 1px solid #000; padding: 12px 14px; font-size: 14px; cursor: pointer; margin-bottom: 16px; }
.back-bar:hover { background: #f4f4f4; }
.checkout-card { border: 2px solid #000; padding: 18px; background: #fff; }
.ck-title { font-size: 18px; font-weight: 700; margin-bottom: 16px; }
.ck-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #ddd; font-size: 13px; }
.ck-row:last-of-type { border-bottom: 0; }
.ck-amount { padding-top: 14px; font-size: 14px; font-weight: 600; }
.amount { color: #f56c6c; font-size: 22px; font-weight: 700; }
.mono { font-family: monospace; font-size: 12px; }
.status-0 { color: #999; }
.status-1 { color: #faad14; }
.status-2 { color: #52c41a; }
.status-3 { color: #999; text-decoration: line-through; }
.ck-actions { margin-top: 18px; }
.btn-block { width: 100%; padding: 12px; font-size: 15px; font-weight: 600; }
.ck-tip { font-size: 12px; color: #999; margin-top: 10px; line-height: 1.5; }
.ck-success { padding: 14px; background: #f6ffed; border: 1px solid #b7eb8f; color: #389e0d; margin-top: 14px; font-size: 14px; }
.ck-closed { padding: 14px; background: #fafafa; border: 1px solid #ddd; color: #999; margin-top: 14px; font-size: 14px; }
.empty { text-align: center; padding: 60px 0; }
</style>
