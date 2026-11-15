<template>
  <div class="orders-page">
    <section class="toolbar card-modern">
      <h2>订单中心</h2>
      <div class="tabs">
        <el-radio-group v-model="activeTab" @change="loadOrders">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="pay0">待支付</el-radio-button>
          <el-radio-button label="ship">待收货</el-radio-button>
          <el-radio-button label="done">已完成</el-radio-button>
        </el-radio-group>
      </div>
    </section>

    <section v-if="orders.length" class="order-list">
      <article v-for="item in orders" :key="item.orderId" class="order-card card-modern">
        <div class="head">
          <div>
            <div class="no">订单号：{{ item.orderNo }}</div>
            <div class="time">{{ formatTime(item.createTime) }}</div>
          </div>
          <el-tag :type="statusType(item.orderStatus)">{{ orderStatusText(item.orderStatus) }}</el-tag>
        </div>

        <div class="body">
          <div class="info"><span>农户：</span>{{ item.farmerName || '-' }}</div>
          <div class="info"><span>商品金额：</span>￥{{ Number(item.totalAmount || 0).toFixed(2) }}</div>
          <div class="info"><span>运费：</span>￥{{ Number(item.freightAmount || 0).toFixed(2) }}（{{ item.freightTemplateName || '默认免运费' }}）</div>
          <div class="info"><span>应付金额：</span>￥{{ Number(item.payAmount || 0).toFixed(2) }}</div>
          <div class="info"><span>支付：</span>{{ payStatusText(item.payStatus) }}</div>
        </div>

        <div v-if="getLatestDispute(item.orderId)" class="dispute-brief">
          <div class="dispute-brief-head">
            <span class="label">纠纷进度：</span>
            <el-tag size="small" :type="disputeStatusType(getLatestDispute(item.orderId).status)">
              {{ disputeStatusText(getLatestDispute(item.orderId).status) }}
            </el-tag>
            <span class="time">{{ formatTime(getLatestDispute(item.orderId).createTime) }}</span>
          </div>
          <div class="dispute-brief-line">
            <span class="label">主题：</span>
            <span>{{ getLatestDispute(item.orderId).title || '订单纠纷' }}</span>
          </div>
          <div class="dispute-brief-line">
            <span class="label">处理结果：</span>
            <span>{{ plainText(getLatestDispute(item.orderId).handleResult) || '管理员暂未回复' }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button @click="openDetail(item)">详情</el-button>
          <el-button v-if="String(item.payStatus) !== '0'" @click="openVoucher(item)">凭证</el-button>
          <el-button v-if="String(item.payStatus) === '0'" type="primary" @click="doPay(item)">立即支付</el-button>
          <el-button v-if="['2','3'].includes(String(item.orderStatus))" type="success" @click="doConfirm(item)">确认收货</el-button>
          <el-button v-if="canRefund(item)" type="danger" plain @click="openRefund(item)">申请退款</el-button>
          <el-button v-if="String(item.orderStatus) === '4'" @click="goReview(item)">评价</el-button>
          <el-button v-if="canDispute(item)" type="warning" plain @click="openDispute(item)">纠纷处理</el-button>
          <el-button v-if="getOrderDisputes(item.orderId).length" type="primary" plain @click="openDisputeHistory(item)">纠纷记录</el-button>
        </div>
      </article>
    </section>

    <el-empty v-else description="暂无订单数据" />

    <el-dialog v-model="detailOpen" title="订单详情" width="980px">
      <template v-if="currentOrder">
        <div class="detail-grid">
          <div><b>订单号：</b>{{ currentOrder.orderNo }}</div>
          <div><b>下单时间：</b>{{ formatTime(currentOrder.createTime) }}</div>
          <div><b>收货人：</b>{{ currentOrder.receiverName }} {{ currentOrder.receiverPhone }}</div>
          <div><b>地址：</b>{{ currentOrder.receiverAddress }}</div>
          <div><b>商品金额：</b>￥{{ Number(currentOrder.totalAmount || 0).toFixed(2) }}</div>
          <div><b>运费模板：</b>{{ currentOrder.freightTemplateName || '默认免运费' }}</div>
          <div><b>运费：</b>￥{{ Number(currentOrder.freightAmount || 0).toFixed(2) }}</div>
          <div><b>支付金额：</b>￥{{ Number(currentOrder.payAmount || 0).toFixed(2) }}</div>
        </div>

        <el-table :data="detailItems" border style="margin-top: 12px">
          <el-table-column prop="productName" label="商品" min-width="220" />
          <el-table-column label="单价" width="120">
            <template #default="scope">￥{{ Number(scope.row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="scope">￥{{ (Number(scope.row.price || 0) * Number(scope.row.quantity || 0)).toFixed(2) }}</template>
          </el-table-column>
        </el-table>

        <el-timeline style="margin-top: 16px">
        <el-timeline-item
            v-for="(trace, idx) in detailTraceAsc"
            :key="idx"
            :timestamp="formatTime(trace.traceTime)"
          >
            <b>{{ traceNodeText(trace.statusNode) }}</b>
            <div>{{ traceDescText(trace) }}</div>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-dialog>

    <el-dialog v-model="voucherOpen" title="支付凭证" width="560px">
      <div v-if="currentVoucher && currentVoucher.payNo" class="voucher-box">
        <div><b>凭证号：</b>{{ currentVoucher.payNo }}</div>
        <div><b>支付方式：</b>{{ channelText(currentVoucher.channel, currentVoucher.channelName) }}</div>
        <div><b>支付金额：</b>￥{{ Number(currentVoucher.payAmount || 0).toFixed(2) }}</div>
        <div><b>支付时间：</b>{{ formatTime(currentVoucher.payTime) }}</div>
      </div>
      <el-empty v-else description="暂无凭证" />
    </el-dialog>

    <el-dialog v-model="refundOpen" title="申请退款" width="620px">
      <template v-if="refundOrder">
        <div class="refund-summary">
          <div><b>订单号：</b>{{ refundOrder.orderNo }}</div>
          <div><b>当前状态：</b>{{ orderStatusText(refundOrder.orderStatus) }}</div>
          <div><b>支付金额：</b>￥{{ Number(refundOrder.payAmount || 0).toFixed(2) }}</div>
        </div>
      </template>
      <el-form label-width="90px">
        <el-form-item label="退款原因">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请填写退款原因，例如商品破损、缺货、物流异常等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundOpen = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="disputeOpen" title="发起纠纷" width="620px">
      <el-form label-width="80px">
        <el-form-item label="主题">
          <el-input v-model="disputeForm.title" placeholder="例如：质量问题、破损、物流问题" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="disputeForm.content" type="textarea" :rows="5" placeholder="请详细描述问题、证据和诉求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disputeOpen = false">取消</el-button>
        <el-button type="primary" @click="confirmDispute">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="disputeHistoryOpen" title="纠纷记录" width="760px">
      <template v-if="disputeHistoryOrder">
        <div class="history-order-no">订单号：{{ disputeHistoryOrder.orderNo }}</div>
        <el-timeline style="margin-top: 12px">
          <el-timeline-item
            v-for="record in disputeHistoryList"
            :key="record.disputeId"
            :timestamp="formatTime(record.createTime)"
            placement="top"
          >
            <div class="history-row">
              <b>{{ record.title || '订单纠纷' }}</b>
              <el-tag size="small" :type="disputeStatusType(record.status)">
                {{ disputeStatusText(record.status) }}
              </el-tag>
            </div>
            <div class="history-line">问题：{{ plainText(record.content) || '-' }}</div>
            <div class="history-line">回复：{{ plainText(record.handleResult) || '管理员暂未回复' }}</div>
            <div class="history-time">处理时间：{{ formatTime(record.handleTime) }}</div>
          </el-timeline-item>
        </el-timeline>
      </template>
      <el-empty v-else description="暂无纠纷记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  applyRefund,
  confirmOrder,
  createDispute,
  getOrderVoucher,
  listConsults,
  listOrderItems,
  listOrders,
  listOrderTrace,
  payOrder
} from '@/api/mall'
import { useMallStore } from '@/store/mall'
import { showConfirmDialog, showFailToast, showSuccessToast } from '@/utils/notify'

const router = useRouter()
const mallStore = useMallStore()

const orders = ref([])
const activeTab = ref('all')
const detailOpen = ref(false)
const voucherOpen = ref(false)
const currentOrder = ref(null)
const currentVoucher = ref(null)
const detailItems = ref([])
const detailTrace = ref([])
const consults = ref([])
const refundOpen = ref(false)
const refundOrder = ref(null)
const refundForm = reactive({ orderId: null, reason: '' })
const disputeOpen = ref(false)
const disputeForm = reactive({ orderId: null, title: '', content: '' })
const disputeHistoryOpen = ref(false)
const disputeHistoryOrder = ref(null)
const disputeHistoryList = ref([])

const PAY_STATUS = { '0': '待支付', '1': '已支付', '2': '已退款' }
const ORDER_STATUS = { '0': '待接单', '1': '待发货', '2': '运输中', '3': '待收货', '4': '已完成', '5': '已取消', '6': '退款处理中', '7': '已退款' }
const TRACE_NODE = { ORDER_CREATED: '订单创建', PAID: '支付成功', ACCEPTED: '农户接单', SHIPPED: '已发货', IN_TRANSIT: '运输中', OUT_FOR_DELIVERY: '派送中', RECEIVED: '已签收', REFUND_APPLY: '退款申请', REFUND_APPROVED: '退款通过', REFUND_REJECTED: '退款驳回' }
const TRACE_DESC = {
  ORDER_CREATED: '订单已创建，等待支付',
  PAID: '订单已完成支付，等待农户处理',
  ACCEPTED: '农户已接单，正在备货',
  SHIPPED: '订单已发货',
  IN_TRANSIT: '订单运输中',
  OUT_FOR_DELIVERY: '包裹已到站点，正在派送',
  RECEIVED: '买家已确认收货',
  CANCELLED: '订单已取消',
  REFUND_APPLY: '已发起退款申请',
  REFUND_APPROVED: '退款申请已通过',
  REFUND_REJECTED: '退款申请已驳回'
}

const detailTraceAsc = computed(() => [...detailTrace.value].sort((a, b) => timeNum(a.traceTime) - timeNum(b.traceTime)))
const disputeGroupMap = computed(() => {
  const map = {}
  ;(consults.value || []).forEach(row => {
    const key = String(row?.orderId || '')
    if (!key) return
    if (!map[key]) map[key] = []
    map[key].push(row)
  })
  Object.keys(map).forEach(key => {
    map[key] = [...map[key]].sort((a, b) => {
      const diff = timeNum(b.updateTime || b.createTime) - timeNum(a.updateTime || a.createTime)
      if (diff !== 0) return diff
      return Number(b.disputeId || 0) - Number(a.disputeId || 0)
    })
  })
  return map
})

function statusType(status) {
  const s = String(status)
  if (s === '4') return 'success'
  if (s === '0' || s === '1') return 'warning'
  if (s === '2' || s === '3') return 'primary'
  return 'info'
}

function payStatusText(val) { return PAY_STATUS[String(val)] || `状态${val}` }
function orderStatusText(val) { return ORDER_STATUS[String(val)] || `状态${val}` }
function traceNodeText(node) { return TRACE_NODE[String(node)] || String(node || '-') }
function disputeStatusText(status) {
  const code = String(status)
  if (code === '0') return '待处理'
  if (code === '1') return '处理中'
  if (code === '2') return '已处理'
  return '未知状态'
}
function disputeStatusType(status) {
  const code = String(status)
  if (code === '0') return 'danger'
  if (code === '1') return 'warning'
  if (code === '2') return 'success'
  return 'info'
}
function traceDescText(trace) {
  const node = String(trace?.statusNode || '')
  return TRACE_DESC[node] || '-'
}
function plainText(text) {
  if (!text) return ''
  return String(text)
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}
function formatTime(time) { return time ? String(time).replace('T', ' ').slice(0, 19) : '-' }
function timeNum(time) { const n = new Date(time).getTime(); return Number.isNaN(n) ? 0 : n }
function channelText(channel, channelName) { return channelName || (String(channel) === '2' ? '支付宝沙箱' : '模拟支付') }
function getOrderDisputes(orderId) { return disputeGroupMap.value[String(orderId)] || [] }
function getLatestDispute(orderId) { return getOrderDisputes(orderId)[0] || null }
function canRefund(order) {
  const status = String(order.orderStatus)
  const payStatus = String(order.payStatus)
  return payStatus === '1' && ['1', '2', '3'].includes(status)
}
function canDispute(order) {
  const status = String(order.orderStatus)
  const payStatus = String(order.payStatus)
  return payStatus === '1' && status !== '5' && status !== '7'
}

async function loadOrders() {
  const params = { pageNum: 1, pageSize: 30 }
  if (activeTab.value === 'pay0') params.payStatus = '0'
  if (activeTab.value === 'ship') params.orderStatus = '2'
  if (activeTab.value === 'done') params.orderStatus = '4'
  const [orderResult, consultResult] = await Promise.allSettled([
    listOrders(params),
    listConsults({ pageNum: 1, pageSize: 200 })
  ])
  if (orderResult.status !== 'fulfilled') {
    showFailToast(orderResult.reason?.message || '加载订单失败')
    return
  }
  orders.value = orderResult.value.rows || []
  if (consultResult.status === 'fulfilled') {
    consults.value = consultResult.value.rows || []
  } else {
    consults.value = []
  }
}

async function openDetail(order) {
  currentOrder.value = order
  detailOpen.value = true
  try {
    const [items, trace, voucher] = await Promise.all([
      listOrderItems(order.orderId),
      listOrderTrace(order.orderId),
      String(order.payStatus) !== '0' ? getOrderVoucher(order.orderId) : Promise.resolve({ data: null })
    ])
    detailItems.value = items.data || []
    detailTrace.value = trace.data || []
    currentVoucher.value = voucher.data
  } catch (e) {
    showFailToast(e.message || '详情加载失败')
  }
}

async function openVoucher(order) {
  currentOrder.value = order
  try {
    const res = await getOrderVoucher(order.orderId)
    currentVoucher.value = res.data
    voucherOpen.value = true
  } catch (e) {
    showFailToast(e.message || '凭证获取失败')
  }
}

async function doPay(order) {
  let payWindow = null
  try {
    await showConfirmDialog({ title: '确认支付', message: '将使用支付宝沙箱完成该订单支付' })
    payWindow = window.open('', '_blank')
    const res = await payOrder({ orderId: order.orderId, channel: '2' })
    const payData = res?.data || {}
    if (String(payData.payMode) === 'REDIRECT' && payData.payUrl) {
      if (payWindow) {
        payWindow.location.href = payData.payUrl
      } else {
        window.location.href = payData.payUrl
      }
      showSuccessToast('已拉起支付宝沙箱，请完成支付后返回刷新')
      setTimeout(() => {
        loadOrders()
      }, 1500)
      return
    }
    if (payWindow) {
      payWindow.close()
      payWindow = null
    }
    showSuccessToast('支付成功')
    loadOrders()
  } catch (e) {
    if (payWindow) {
      payWindow.close()
    }
    if (e?.message) showFailToast(e.message)
  }
}

async function doConfirm(order) {
  try {
    await showConfirmDialog({ title: '确认收货', message: '请确认您已收到商品并检查无误' })
    await confirmOrder(order.orderId)
    await mallStore.refreshReviewStat()
    showSuccessToast('收货成功')
    loadOrders()
  } catch (e) {
    if (e?.message) showFailToast(e.message)
  }
}

function goReview(order) {
  router.push(`/reviews?orderId=${order.orderId}`)
}

function openRefund(order) {
  refundOrder.value = order
  refundForm.orderId = order.orderId
  refundForm.reason = ''
  refundOpen.value = true
}

function openDispute(order) {
  disputeForm.orderId = order.orderId
  disputeForm.title = ''
  disputeForm.content = ''
  disputeOpen.value = true
}

function openDisputeHistory(order) {
  disputeHistoryOrder.value = order
  disputeHistoryList.value = getOrderDisputes(order.orderId)
  disputeHistoryOpen.value = true
}

async function submitRefund() {
  if (!refundForm.reason.trim()) {
    showFailToast('请填写退款原因')
    return
  }
  try {
    await showConfirmDialog({ title: '确认申请退款', message: '提交后将进入农户审核流程，请确认退款原因填写无误' })
    await applyRefund({
      orderId: refundForm.orderId,
      reason: refundForm.reason.trim()
    })
    showSuccessToast('退款申请已提交，请等待处理')
    refundOpen.value = false
    await loadOrders()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      showFailToast(e.message || '退款申请提交失败')
    }
  }
}

async function confirmDispute() {
  if (!disputeForm.content) {
    showFailToast('请填写纠纷说明')
    return
  }
  try {
    await createDispute(disputeForm)
    showSuccessToast('纠纷已提交，等待平台处理')
    disputeOpen.value = false
    await loadOrders()
  } catch (e) {
    showFailToast(e.message || '提交失败')
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar h2 {
  margin: 0;
  font-size: 30px;
  font-weight: 900;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  padding: 18px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.no {
  font-weight: 800;
}

.time {
  color: #64748b;
  font-size: 13px;
  margin-top: 4px;
}

.body {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 14px;
}

.info {
  color: #334155;
}

.info span {
  color: #64748b;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.dispute-brief {
  border: 1px solid #f1f5f9;
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 12px;
}

.dispute-brief-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.dispute-brief-line {
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}

.dispute-brief .label {
  color: #64748b;
}

.dispute-brief .time {
  color: #94a3b8;
  font-size: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.voucher-box {
  display: grid;
  gap: 10px;
}

.refund-summary {
  display: grid;
  gap: 8px;
  margin-bottom: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafc;
  color: #334155;
}

.history-order-no {
  font-weight: 700;
  color: #1f2937;
}

.history-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.history-line {
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}

.history-time {
  color: #94a3b8;
  font-size: 12px;
  margin-top: 4px;
}
</style>
