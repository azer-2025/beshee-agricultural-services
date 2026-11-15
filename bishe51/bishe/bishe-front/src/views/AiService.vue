<template>
  <div class="ai-service-page">
    <!-- Hero Section with Glassmorphism -->
    <section class="ai-service-hero">
      <div class="hero-content">
        <div class="hero-copy">
          <div class="hero-badge">
            <el-icon><MagicStick /></el-icon>
            <span>AI 智能助手 2.0</span>
          </div>
          <h1>
            您的<span class="gradient-text">助农管家</span>已上线
          </h1>
          <p>
            结合您的订单、购物车和商品偏好，为您提供全天候 7×24 小时即时服务。
            无论是物流查询、退款咨询还是产品推荐，我都能帮到您。
          </p>
          <div class="hero-actions">
            <el-button type="primary" class="btn-premium" @click="usePrompt('帮我看看最近订单进度')">
              <el-icon><Search /></el-icon> 订单建议
            </el-button>
            <el-button class="btn-outline" @click="goHumanService">
              <el-icon><Service /></el-icon> 人工咨询
            </el-button>
          </div>
        </div>

        <div class="hero-stats">
          <div class="stats-header">
            <h3>实时摘要</h3>
            <span class="live-indicator">LIVE</span>
          </div>
          <div class="stats-grid">
            <div class="stat-card pink">
              <div class="stat-icon"><el-icon><ChatDotSquare /></el-icon></div>
              <div class="stat-info">
                <span class="stat-label">待处理咨询</span>
                <strong class="stat-value">{{ pendingConsultCount }}</strong>
              </div>
            </div>
            <div class="stat-card blue">
              <div class="stat-icon"><el-icon><ShoppingCart /></el-icon></div>
              <div class="stat-info">
                <span class="stat-label">购物车商品</span>
                <strong class="stat-value">{{ mallStore.cartCount || 0 }}</strong>
              </div>
            </div>
            <div class="stat-card green">
              <div class="stat-icon"><el-icon><Tickets /></el-icon></div>
              <div class="stat-info">
                <span class="stat-label">待评价订单</span>
                <strong class="stat-value">{{ mallStore.reviewPendingCount || 0 }}</strong>
              </div>
            </div>
            <div class="stat-card orange">
              <div class="stat-icon"><el-icon><ShoppingBag /></el-icon></div>
              <div class="stat-info">
                <span class="stat-label">可推荐商品</span>
                <strong class="stat-value">{{ products.length }}</strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="ai-service-container">
      <!-- Sidebar Column -->
      <aside class="ai-sidebar">
        <div class="glass-card sidebar-card">
          <div class="card-title">
            <el-icon><QuestionFilled /></el-icon>
            <span>猜你想问</span>
          </div>
          <div class="prompt-chips">
            <button
              v-for="prompt in quickPrompts"
              :key="prompt"
              class="modern-chip"
              @click="usePrompt(prompt)"
            >
              {{ prompt }}
            </button>
          </div>
        </div>

        <div class="glass-card sidebar-card" v-if="recentOrders.length">
          <div class="card-title">
            <el-icon><Calendar /></el-icon>
            <span>最近订单</span>
          </div>
          <div class="compact-orders">
            <div
              v-for="item in recentOrders"
              :key="item.orderId"
              class="compact-order-item"
              @click="usePrompt(`帮我看看订单 ${item.orderNo || item.orderId} 的状态`)"
            >
              <div class="order-head">
                <span class="order-no">#{{ String(item.orderNo || item.orderId).slice(-6) }}</span>
                <el-tag size="small" :type="getStatusType(item.orderStatus)">
                  {{ orderStatusText(item.orderStatus) }}
                </el-tag>
              </div>
              <div class="order-foot">
                <span>{{ payStatusText(item.payStatus) }}</span>
                <span class="time">{{ formatTime(item.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="glass-card sidebar-card" v-if="products.length">
          <div class="card-title">
            <el-icon><StarFilled /></el-icon>
            <span>为您精选</span>
          </div>
          <div class="compact-products">
            <div v-for="item in featuredProducts" :key="item.productId" class="compact-product-item">
              <div class="p-info">
                <div class="p-name">{{ item.productName }}</div>
                <div class="p-farm">{{ item.farmName || '助农优选' }}</div>
              </div>
              <div class="p-price">￥{{ priceText(item.price) }}</div>
            </div>
          </div>
        </div>
      </aside>

      <!-- Chat Main Panel -->
      <main class="chat-wrapper">
        <div class="chat-inner glass-card">
          <div class="chat-header">
            <div class="chat-status">
              <span class="pulse-dot"></span>
              <span>AI 服务在线</span>
            </div>
            <div class="chat-actions">
              <el-tooltip content="清空聊天记录" placement="top">
                <el-button circle size="small" @click="clearConversation">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>

          <div ref="messageViewport" class="message-area">
            <transition-group name="message-fade">
              <div
                v-for="item in messages"
                :key="item.id"
                class="message-bubble-row"
                :class="item.role"
              >
                <div class="avatar-wrap">
                  <div class="avatar" v-if="item.role === 'assistant'">
                    <el-icon><ChatLineRound /></el-icon>
                  </div>
                  <div class="avatar user-avatar" v-else>
                    {{ userInitial }}
                  </div>
                </div>
                <div class="bubble-content">
                  <div class="bubble-text">{{ item.text }}</div>
                  <div v-if="item.actions?.length" class="bubble-actions">
                    <el-button
                      v-for="action in item.actions"
                      :key="action.label"
                      size="small"
                      round
                      class="action-pill"
                      @click="handleMessageAction(action)"
                    >
                      {{ action.label }}
                    </el-button>
                  </div>
                  <div class="bubble-meta">{{ item.time }}</div>
                </div>
              </div>
            </transition-group>
<!-- Typing Indicator -->
<div v-if="sending" class="message-bubble-row assistant">
  <div class="avatar-wrap">
    <div class="avatar"><el-icon><ChatLineRound /></el-icon></div>
  </div>
  <div class="bubble-content">
    <div class="typing-indicator">
...
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="composer-area">
            <div class="input-container">
              <el-input
                v-model="draft"
                type="textarea"
                :rows="1"
                autosize
                resize="none"
                placeholder="询问订单进度、退款流程或推荐商品..."
                class="modern-textarea"
                @keydown.enter.exact.prevent="submitMessage"
              />
              <el-button
                type="primary"
                circle
                class="send-btn"
                :loading="sending"
                :disabled="!draft.trim()"
                @click="submitMessage"
              >
                <el-icon><Promotion /></el-icon>
              </el-button>
            </div>
            <div class="composer-footer">
              <span class="tip">Enter 发送，Shift + Enter 换行</span>
              <span class="word-count">{{ draft.length }}/200</span>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Calendar,
  ChatDotSquare,
  ChatLineRound,
  Delete,
  MagicStick,
  Promotion,
  QuestionFilled,
  Search,
  Service,
  ShoppingBag,
  ShoppingCart,
  StarFilled,
  Tickets
} from '@element-plus/icons-vue'
import { aiChat, clearAiHistory, listAiHistory, listConsults, listOrders, listProducts } from '@/api/mall'
import { useMallStore } from '@/store/mall'
import { useUserStore } from '@/store/user'
import { showFailToast } from '@/utils/notify'

const router = useRouter()
const mallStore = useMallStore()
const userStore = useUserStore()

const sending = ref(false)
const draft = ref('')
const messages = ref([])
const orders = ref([])
const consults = ref([])
const products = ref([])
const messageViewport = ref(null)

const PAY_STATUS = {
  '0': '待支付',
  '1': '已支付',
  '2': '已退款'
}

const ORDER_STATUS = {
  '0': '待接单',
  '1': '待发货',
  '2': '运输中',
  '3': '待收货',
  '4': '已完成',
  '5': '已取消',
  '6': '退款处理中',
  '7': '已退款'
}

const quickPrompts = computed(() => [
  '帮我看看最近订单进度',
  '购物车里的商品现在适合下单吗',
  '我想申请退款，要怎么操作',
  '推荐几款热门助农产品',
  '评价入口在哪里',
  '转人工咨询'
])

const recentOrders = computed(() => orders.value.slice(0, 4))
const featuredProducts = computed(() => products.value.slice(0, 4))
const pendingConsultCount = computed(() => consults.value.filter(item => String(item.status) !== '2').length)
const userInitial = computed(() => String(userStore.userInfo?.nickName || '用户').slice(0, 1))

function storageKey() {
  return `front_ai_service_messages_${userStore.userInfo?.userId || 'guest'}`
}

function payStatusText(val) {
  return PAY_STATUS[String(val)] || `状态${val}`
}

function orderStatusText(val) {
  return ORDER_STATUS[String(val)] || `状态${val}`
}

function getStatusType(status) {
  const s = String(status)
  if (['0', '1'].includes(s)) return 'warning'
  if (['2', '3'].includes(s)) return 'primary'
  if (s === '4') return 'success'
  if (['5', '7'].includes(s)) return 'info'
  if (s === '6') return 'danger'
  return ''
}

function priceText(value) {
  return Number(value || 0).toFixed(2)
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(5, 16)
}

function createMessage(role, text, actions = []) {
  return {
    id: `${Date.now()}_${Math.random().toString(16).slice(2)}`,
    role,
    text,
    actions,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

function saveConversation() {
  localStorage.setItem(storageKey(), JSON.stringify(messages.value))
}

async function scrollToBottom() {
  await nextTick()
  const el = messageViewport.value
  if (el) {
    el.scrollTo({
      top: el.scrollHeight,
      behavior: 'smooth'
    })
  }
}

function defaultWelcomeMessage() {
  const name = userStore.userInfo?.nickName || '您好'
  const activeOrder = orders.value.find(item => ['1', '2', '3'].includes(String(item.orderStatus)))
  let snippets = [
    `${name}，欢迎使用助农商城 AI 客服助手！🌟`,
    `我能为您提供订单查询、售后咨询及个性化商品推荐。`
  ]

  if (activeOrder) {
    snippets.push(`\n温馨提示：您的订单 **#${activeOrder.orderNo || activeOrder.orderId}** 目前正处于“${orderStatusText(activeOrder.orderStatus)}”阶段，我会为您密切关注进度。`)
  }
  
  return createMessage('assistant', snippets.join('\n'))
}

function restoreConversation() {
  const raw = localStorage.getItem(storageKey())
  if (!raw) {
    messages.value = [defaultWelcomeMessage()]
    saveConversation()
    return
  }
  try {
    const parsed = JSON.parse(raw)
    messages.value = Array.isArray(parsed) && parsed.length ? parsed : [defaultWelcomeMessage()]
  } catch (e) {
    messages.value = [defaultWelcomeMessage()]
  }
}

async function clearConversation() {
  try {
    await clearAiHistory()
  } catch (e) {
    // ignore
  }
  messages.value = [defaultWelcomeMessage()]
  saveConversation()
  scrollToBottom()
}

function usePrompt(prompt) {
  draft.value = prompt
  submitMessage()
}

function goHumanService() {
  router.push('/')
}

function latestOrderHint() {
  const order = orders.value.find(item => !['4', '5', '7'].includes(String(item.orderStatus))) || orders.value[0]
  if (!order) {
    return '您目前没有进行中的订单。您可以去“商品浏览”看看最近的热门农货。'
  }
  return `您最近的订单 **#${order.orderNo || order.orderId}** 状态为：**${orderStatusText(order.orderStatus)}**。如有疑问，我可以为您跳转至详情页。`
}

function productRecommendationHint() {
  if (!products.value.length) {
    return '当前暂无可推荐商品，建议您稍后再来看看。'
  }
  const topNames = products.value
    .slice(0, 3)
    .map(item => `**${item.productName}**（￥${priceText(item.price)}）`)
    .join('、')
  return `根据您的喜好，我为您推荐：${topNames}。这些都是近期口碑极佳的助农产品。`
}

function refundHint() {
  const refundable = orders.value.find(item => ['1', '2', '3', '6'].includes(String(item.orderStatus)))
  if (!refundable) {
    return '若需申请退款，请先确保订单已支付且处于可售后状态。您可以在“订单跟踪”中查看。'
  }
  return `针对订单 **#${refundable.orderNo || refundable.orderId}**，您可以直接在订单详情页发起退款。如有困难，请联系人工客服。`
}

function cartHint() {
  const count = mallStore.cartCount || 0
  if (!count) {
    return '您的购物车目前空空如也。'
  }
  return `您的购物车中有 **${count}** 件商品。现在下单可能还能赶上最新的物流班次哦。`
}

function buildReply(input) {
  const text = String(input || '').trim().toLowerCase()
  const actions = []
  let reply = ''

  if (/(人工|转人工|客服|咨询)/.test(text)) {
    reply = '已为您准备好人工咨询入口，点击下方按钮即可前往。'
    actions.push({ label: '前往人工咨询', to: '/' })
  } else if (/(退款|退货|售后)/.test(text)) {
    reply = refundHint()
    actions.push({ label: '订单跟踪', to: '/orders' })
  } else if (/(物流|发货|配送|快递|订单|到哪|进度)/.test(text)) {
    reply = latestOrderHint()
    actions.push({ label: '查看订单详情', to: '/orders' })
  } else if (/(购物车|结算|下单|买单)/.test(text)) {
    reply = cartHint()
    actions.push({ label: '去购物车', to: '/cart' })
  } else if (/(评价|评论|晒单)/.test(text)) {
    reply = `您有 **${mallStore.reviewPendingCount}** 笔待评价订单，您的评价对农户们非常重要。`
    actions.push({ label: '评价中心', to: '/reviews' })
  } else if (/(推荐|商品|农产品|水果|蔬菜|买什么|送礼)/.test(text)) {
    reply = productRecommendationHint()
    actions.push({ label: '更多商品', to: '/products' })
  } else {
    reply = '抱歉，我可能没完全听懂。您可以询问关于订单、退款、评价或商品推荐的问题。'
    actions.push({ label: '看看商品', to: '/products' })
  }

  return { text: reply, actions }
}

function handleMessageAction(action) {
  if (action.to) {
    router.push(action.to)
  }
}

function serializeHistory() {
  return messages.value.slice(-9, -1).map(item => ({
    role: item.role === 'assistant' ? 'assistant' : 'user',
    content: item.text
  }))
}

async function submitMessage() {
  const text = draft.value.trim()
  if (!text || sending.value) return

  messages.value.push(createMessage('user', text))
  draft.value = ''
  saveConversation()
  scrollToBottom()

  sending.value = true
  try {
    const fallback = buildReply(text)
    let replyText = fallback.text
    let actions = fallback.actions

    try {
      const response = await aiChat({
        message: text,
        history: serializeHistory()
      })
      if (response?.data?.reply) {
        replyText = response.data.reply
      }
    } catch (e) {
      // Use fallback
    }

    messages.value.push(createMessage('assistant', replyText, actions))
    saveConversation()
    scrollToBottom()
  } finally {
    sending.value = false
  }
}

async function loadContext() {
  const tasks = [
    listOrders({ pageNum: 1, pageSize: 6 }),
    listConsults({ pageNum: 1, pageSize: 6 }),
    listProducts({ pageNum: 1, pageSize: 6 }),
    mallStore.refreshCartStat(),
    mallStore.refreshReviewStat()
  ]
  const [orderRes, consultRes, productRes] = await Promise.allSettled(tasks)
  if (orderRes.status === 'fulfilled') orders.value = orderRes.value?.rows || []
  if (consultRes.status === 'fulfilled') consults.value = consultRes.value?.rows || []
  if (productRes.status === 'fulfilled') products.value = productRes.value?.rows || []
}

onMounted(async () => {
  await loadContext()
  try {
    const response = await listAiHistory({ limit: 20 })
    const rows = response?.data || []
    if (rows.length) {
      messages.value = rows.map(item => createMessage(item.role === 'assistant' ? 'assistant' : 'user', item.content || ''))
    } else {
      restoreConversation()
    }
  } catch (e) {
    restoreConversation()
  }
  scrollToBottom()
})
</script>

<style scoped>
.ai-service-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 20px 60px;
}

/* --- Hero Section --- */
.ai-service-hero {
  margin-bottom: 32px;
  border-radius: 32px;
  background: linear-gradient(135deg, #f0fdf4 0%, #e0f2fe 100%);
  border: 1px solid rgba(255, 255, 255, 0.6);
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
}

.hero-content {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 40px;
  padding: 48px;
  align-items: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(22, 163, 74, 0.1);
  color: #166534;
  border-radius: 99px;
  font-size: 13px;
  font-weight: 800;
  margin-bottom: 20px;
}

.hero-copy h1 {
  font-size: 48px;
  font-weight: 900;
  color: #0f172a;
  line-height: 1.1;
  margin-bottom: 16px;
}

.gradient-text {
  background: linear-gradient(135deg, #16a34a, #0ea5e9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-copy p {
  font-size: 16px;
  color: #475569;
  line-height: 1.6;
  max-width: 520px;
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.btn-premium {
  height: 52px !important;
  padding: 0 32px !important;
  border-radius: 16px !important;
  font-size: 15px !important;
  background: linear-gradient(135deg, #16a34a, #15803d) !important;
  border: none !important;
  box-shadow: 0 10px 25px rgba(22, 163, 74, 0.2) !important;
}

.btn-outline {
  height: 52px !important;
  padding: 0 32px !important;
  border-radius: 16px !important;
  font-size: 15px !important;
  background: #fff !important;
  border: 1px solid #e2e8f0 !important;
  color: #475569 !important;
}

/* --- Stats Grid --- */
.hero-stats {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-header h3 {
  font-size: 16px;
  font-weight: 800;
  color: #1e293b;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  font-weight: 900;
  color: #ef4444;
}

.live-indicator::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #ef4444;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2.5); opacity: 0; }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-card {
  padding: 16px;
  border-radius: 18px;
  display: flex;
  gap: 12px;
  background: #fff;
  transition: all 0.3s;
}

.stat-card:hover { transform: translateY(-3px); box-shadow: 0 10px 20px rgba(0,0,0,0.04); }

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 20px;
}

.stat-card.pink .stat-icon { background: #fdf2f8; color: #db2777; }
.stat-card.blue .stat-icon { background: #eff6ff; color: #2563eb; }
.stat-card.green .stat-icon { background: #f0fdf4; color: #16a34a; }
.stat-card.orange .stat-icon { background: #fff7ed; color: #ea580c; }

.stat-info { display: flex; flex-direction: column; }
.stat-label { font-size: 11px; color: #64748b; font-weight: 700; text-transform: uppercase; }
.stat-value { font-size: 22px; font-weight: 900; color: #0f172a; }

/* --- Layout Container --- */
.ai-service-container {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 32px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.02);
}

/* --- Sidebar --- */
.ai-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card { padding: 24px; }
.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 20px;
}

.card-title .el-icon { font-size: 18px; color: #16a34a; }

.prompt-chips { display: flex; flex-direction: column; gap: 10px; }
.modern-chip {
  width: 100%;
  padding: 12px 16px;
  text-align: left;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s;
}

.modern-chip:hover {
  background: #f8fafc;
  border-color: #16a34a;
  color: #16a34a;
  transform: translateX(4px);
}

.compact-orders, .compact-products { display: flex; flex-direction: column; gap: 12px; }

.compact-order-item, .compact-product-item {
  padding: 12px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.compact-order-item:hover, .compact-product-item:hover {
  border-color: #3b82f6;
  background: #f0f7ff;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.order-no { font-size: 12px; font-weight: 800; color: #1e293b; }
.order-foot {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
}

.p-name { font-size: 13px; font-weight: 700; color: #1e293b; }
.p-farm { font-size: 11px; color: #94a3b8; }
.compact-product-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.p-price { font-size: 14px; font-weight: 900; color: #ca8a04; }

/* --- Chat Wrapper --- */
.chat-wrapper {
  height: 800px;
}

.chat-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.4);
  animation: dot-pulse 2s infinite;
}

@keyframes dot-pulse {
  0% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(34, 197, 94, 0); }
  100% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
}

.message-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-bubble-row {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message-bubble-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: #f1f5f9;
  color: #64748b;
  display: grid;
  place-items: center;
  font-size: 18px;
}

.user-avatar {
  background: #16a34a;
  color: #fff;
  font-size: 14px;
  font-weight: 800;
}

.bubble-content { display: flex; flex-direction: column; gap: 4px; }

.bubble-text {
  padding: 14px 18px;
  border-radius: 20px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.assistant .bubble-text {
  background: #fff;
  color: #334155;
  border: 1px solid #f1f5f9;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.user .bubble-text {
  background: #1e293b;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.bubble-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.action-pill {
  background: #fff !important;
  border: 1px solid #e2e8f0 !important;
  color: #64748b !important;
  font-size: 12px !important;
  padding: 4px 12px !important;
}

.bubble-meta {
  font-size: 10px;
  color: #94a3b8;
  margin: 0 4px;
}

.user .bubble-meta { text-align: right; }

/* --- Typing Indicator --- */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
  background: #fff;
  border-radius: 20px;
  border: 1px solid #f1f5f9;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #cbd5e1;
  border-radius: 50%;
  animation: typing 1s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

/* --- Composer --- */
.composer-area {
  padding: 24px;
  background: #fff;
  border-top: 1px solid #f1f5f9;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f8fafc;
  padding: 8px 8px 8px 16px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.input-container:focus-within {
  background: #fff;
  border-color: #16a34a;
  box-shadow: 0 0 0 4px rgba(22, 163, 74, 0.1);
}

.modern-textarea :deep(.el-textarea__inner) {
  border: none !important;
  background: transparent !important;
  padding: 8px 0 !important;
  font-family: inherit !important;
  font-size: 14px !important;
  color: #1e293b !important;
  box-shadow: none !important;
}

.send-btn {
  width: 44px !important;
  height: 44px !important;
  background: #16a34a !important;
  border: none !important;
  font-size: 20px !important;
  flex-shrink: 0;
}

.send-btn:disabled { opacity: 0.5; background: #cbd5e1 !important; }

.composer-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  padding: 0 4px;
}

.composer-footer span { font-size: 11px; color: #94a3b8; }

/* --- Animations --- */
.message-fade-enter-active, .message-fade-leave-active { transition: all 0.4s ease; }
.message-fade-enter-from { opacity: 0; transform: translateY(20px); }
.message-fade-leave-to { opacity: 0; transform: scale(0.9); }

/* --- Responsive --- */
@media (max-width: 1200px) {
  .hero-content { grid-template-columns: 1fr; padding: 32px; }
  .ai-service-container { grid-template-columns: 1fr; }
  .ai-sidebar { order: 2; display: grid; grid-template-columns: repeat(2, 1fr); }
  .chat-wrapper { order: 1; height: 600px; }
}

@media (max-width: 768px) {
  .hero-copy h1 { font-size: 32px; }
  .ai-sidebar { grid-template-columns: 1fr; }
  .hero-stats { margin-top: 20px; }
}
</style>
