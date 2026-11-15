<template>
  <div class="app-container">
    <el-card shadow="never" class="mb16">
      <div class="summary-row">
        <el-statistic title="待接单（已支付）" :value="pendingAcceptCount" />
        <el-statistic title="待处理退款" :value="pendingRefundCount" />
        <div class="refresh-wrap">
          <div class="refresh-time">上次刷新：{{ lastRefreshTime || '-' }}</div>
          <el-badge :value="noticeUnreadCount" :hidden="noticeUnreadCount === 0">
            <el-button v-hasPermi="['agri:farmer:notice:list']" @click="openNoticeDrawer">消息中心</el-button>
          </el-badge>
          <el-switch
            v-model="autoRefresh"
            active-text="自动提醒"
            inactive-text="手动"
            inline-prompt
            @change="onAutoRefreshChange"
          />
          <el-button @click="refreshData(false)">立即刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="15">
        <el-card shadow="never">
          <template #header>订单协同处理</template>
          <el-form :inline="true" :model="query" class="mb12">
            <el-form-item label="订单状态">
              <el-select v-model="query.orderStatus" clearable style="width: 140px">
                <el-option label="待接单" value="0" />
                <el-option label="待发货" value="1" />
                <el-option label="运输中" value="2" />
                <el-option label="待收货" value="3" />
                <el-option label="已完成" value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="支付状态">
              <el-select v-model="query.payStatus" clearable style="width: 140px">
                <el-option label="待支付" value="0" />
                <el-option label="已支付" value="1" />
                <el-option label="已退款" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="refreshData(false)">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="orderList" border v-loading="loadingOrder">
            <el-table-column prop="orderNo" label="订单号" width="200" />
            <el-table-column prop="buyerName" label="买家" width="110" />
            <el-table-column label="金额" width="100">
              <template #default="scope">￥{{ Number(scope.row.payAmount || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="支付状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.payStatus === '1' ? 'success' : (scope.row.payStatus === '0' ? 'warning' : 'info')">
                  {{ payStatusText(scope.row.payStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="订单状态" width="100">
              <template #default="scope">
                <el-tag :type="orderStatusTagType(scope.row.orderStatus)">
                  {{ orderStatusText(scope.row.orderStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下单时间" width="160">
              <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="220">
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  v-hasPermi="['agri:farmer:order:accept']"
                  v-if="scope.row.orderStatus === '0' && scope.row.payStatus === '1'"
                  @click="accept(scope.row)"
                >接单</el-button>
                <el-button
                  link
                  type="success"
                  v-hasPermi="['agri:farmer:order:ship']"
                  v-if="scope.row.orderStatus === '1'"
                  @click="ship(scope.row)"
                >发货</el-button>
                <el-tag v-if="scope.row.orderStatus === '0' && scope.row.payStatus !== '1'" type="warning">待买家支付</el-tag>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="query.pageNum"
            v-model:limit="query.pageSize"
            @pagination="refreshData(false)"
          />
        </el-card>
      </el-col>

      <el-col :span="9">
        <el-card shadow="never">
          <template #header>退款申请</template>
          <el-table :data="refundList" border v-loading="loadingRefund">
            <el-table-column prop="orderId" label="订单ID" width="90" />
            <el-table-column label="金额" width="100">
              <template #default="scope">￥{{ Number(scope.row.amount || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="退款原因" min-width="120" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="scope">
                <el-tag :type="scope.row.status === '0' ? 'warning' : (scope.row.status === '1' ? 'success' : 'danger')">
                  {{ refundStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                  link
                  type="success"
                  v-hasPermi="['agri:farmer:refund:handle']"
                  v-if="scope.row.status === '0'"
                  @click="handle(scope.row, '1')"
                >同意</el-button>
                <el-button
                  link
                  type="danger"
                  v-hasPermi="['agri:farmer:refund:handle']"
                  v-if="scope.row.status === '0'"
                  @click="handle(scope.row, '2')"
                >拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-drawer v-model="noticeOpen" title="农户消息中心" size="420px">
      <div class="notice-toolbar">
        <span>未读 {{ noticeUnreadCount }} 条</span>
        <el-button
          link
          type="primary"
          v-hasPermi="['agri:farmer:notice:read']"
          :disabled="noticeUnreadCount === 0"
          @click="markAllNoticesRead"
        >全部已读</el-button>
      </div>
      <el-form :inline="true" :model="noticeQuery" class="notice-filter">
        <el-form-item label="状态">
          <el-select v-model="noticeQuery.readFlag" clearable style="width: 98px">
            <el-option label="未读" value="0" />
            <el-option label="已读" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="noticeQuery.noticeType" clearable style="width: 116px">
            <el-option label="新订单" value="NEW_ORDER" />
            <el-option label="退款申请" value="REFUND_APPLY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="noticeQuery.keyword" clearable placeholder="关键词" style="width: 140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onNoticeSearch">查询</el-button>
          <el-button @click="onNoticeReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-scrollbar height="calc(100vh - 220px)" v-loading="noticeLoading">
        <div v-if="noticeList.length > 0" class="notice-list">
          <div
            v-for="item in noticeList"
            :key="item.noticeId"
            class="notice-item"
            :class="{ unread: item.readFlag === '0' }"
          >
            <div class="notice-item__head">
              <el-tag size="small" :type="noticeTypeTag(item.noticeType)">{{ noticeTypeText(item.noticeType) }}</el-tag>
              <span class="notice-time">{{ formatDateTime(item.createTime) }}</span>
            </div>
            <div class="notice-title">{{ item.title }}</div>
            <div class="notice-content">{{ item.content }}</div>
            <div class="notice-actions">
              <el-button
                v-if="item.readFlag === '0'"
                link
                type="primary"
                v-hasPermi="['agri:farmer:notice:read']"
                @click="markNoticeRead(item)"
              >标记已读</el-button>
              <span v-else class="notice-read-time">已读 {{ formatDateTime(item.readTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无消息" />
      </el-scrollbar>
      <div class="notice-pagination">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="noticeTotal"
          :page-size="noticeQuery.pageSize"
          :current-page="noticeQuery.pageNum"
          @current-change="onNoticePageChange"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import cache from '@/plugins/cache'
import { parseTime } from '@/utils/ruoyi'
import {
  acceptFarmerOrder,
  countFarmerUnreadNotices,
  handleFarmerRefund,
  listFarmerNotices,
  listFarmerOrders,
  listFarmerRefunds,
  readAllFarmerNotice,
  readFarmerNotice,
  shipFarmerOrder
 } from '@/api/agri/mall'

const FARMER_NOTICE_QUERY_CACHE_KEY = 'agri_farmer_notice_query'
const defaultNoticeQuery = { pageNum: 1, pageSize: 8, readFlag: '', noticeType: '', keyword: '' }
const savedNoticeQuery = cache.local.getJSON(FARMER_NOTICE_QUERY_CACHE_KEY) || {}
const query = reactive({ pageNum: 1, pageSize: 10, orderStatus: '', payStatus: '' })
const orderList = ref([])
const refundList = ref([])
const total = ref(0)
const loadingOrder = ref(false)
const loadingRefund = ref(false)
const lastRefreshTime = ref('')
const autoRefresh = ref(true)
const pendingAcceptCount = ref(0)
const pendingRefundCount = ref(0)
const noticeOpen = ref(false)
const noticeLoading = ref(false)
const noticeList = ref([])
const noticeUnreadCount = ref(0)
const noticeTotal = ref(0)
const noticeQuery = reactive({
  pageNum: Number(savedNoticeQuery.pageNum) > 0 ? Number(savedNoticeQuery.pageNum) : defaultNoticeQuery.pageNum,
  pageSize: Number(savedNoticeQuery.pageSize) > 0 ? Number(savedNoticeQuery.pageSize) : defaultNoticeQuery.pageSize,
  readFlag: savedNoticeQuery.readFlag || defaultNoticeQuery.readFlag,
  noticeType: savedNoticeQuery.noticeType || defaultNoticeQuery.noticeType,
  keyword: savedNoticeQuery.keyword || defaultNoticeQuery.keyword
})
const reminderPendingOrderIds = ref([])
let timer = null
let initialized = false
let knownOrderIds = new Set()
let knownRefundIds = new Set()

watch(noticeQuery, (val) => {
  cache.local.setJSON(FARMER_NOTICE_QUERY_CACHE_KEY, {
    pageNum: Number(val.pageNum) > 0 ? Number(val.pageNum) : defaultNoticeQuery.pageNum,
    pageSize: Number(val.pageSize) > 0 ? Number(val.pageSize) : defaultNoticeQuery.pageSize,
    readFlag: val.readFlag || '',
    noticeType: val.noticeType || '',
    keyword: val.keyword || ''
  })
}, { deep: true })

const pendingAcceptIds = computed(() => reminderPendingOrderIds.value)

const pendingRefundIds = computed(() => {
  return refundList.value
    .filter(item => item.status === '0')
    .map(item => Number(item.refundId))
})

function nowText() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

function formatDateTime(value) {
  return parseTime(value) || '-'
}

function payStatusText(val) {
  const map = { '0': '待支付', '1': '已支付', '2': '已退款' }
  return map[String(val)] || `状态${val}`
}

function orderStatusText(val) {
  const map = { '0': '待接单', '1': '待发货', '2': '运输中', '3': '待收货', '4': '已完成', '5': '已取消', '6': '退款中', '7': '已退款' }
  return map[String(val)] || `状态${val}`
}

function orderStatusTagType(val) {
  const code = String(val)
  if (code === '0' || code === '1') return 'warning'
  if (code === '2' || code === '3') return 'primary'
  if (code === '4') return 'success'
  if (code === '6') return 'warning'
  return 'info'
}

function refundStatusText(val) {
  const map = { '0': '待处理', '1': '已同意', '2': '已拒绝' }
  return map[String(val)] || `状态${val}`
}

function noticeTypeText(val) {
  const map = { NEW_ORDER: '新订单', REFUND_APPLY: '退款申请' }
  return map[String(val)] || '系统消息'
}

function noticeTypeTag(val) {
  const map = { NEW_ORDER: 'success', REFUND_APPLY: 'warning' }
  return map[String(val)] || 'info'
}

async function loadOrders() {
  loadingOrder.value = true
  try {
    const res = await listFarmerOrders(query)
    orderList.value = res.rows || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '订单加载失败')
  } finally {
    loadingOrder.value = false
  }
}

async function loadRefunds() {
  loadingRefund.value = true
  try {
    const res = await listFarmerRefunds()
    refundList.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '退款列表加载失败')
  } finally {
    loadingRefund.value = false
  }
}

async function loadReminderOrders() {
  try {
    const res = await listFarmerOrders({ pageNum: 1, pageSize: 200, orderStatus: '0', payStatus: '1' })
    reminderPendingOrderIds.value = (res.rows || []).map(item => Number(item.orderId))
  } catch (e) {
    reminderPendingOrderIds.value = []
  }
}

async function refreshNoticeCount() {
  try {
    const res = await countFarmerUnreadNotices()
    noticeUnreadCount.value = Number(res.data || 0)
  } catch (e) {
    noticeUnreadCount.value = 0
  }
}

async function loadNoticeList() {
  noticeLoading.value = true
  try {
    const res = await listFarmerNotices(noticeQuery)
    noticeList.value = res.rows || []
    noticeTotal.value = Number(res.total || 0)
  } catch (e) {
    ElMessage.error(e.message || '消息列表加载失败')
  } finally {
    noticeLoading.value = false
  }
}

function notifyNewData() {
  const currentOrderSet = new Set(pendingAcceptIds.value)
  const currentRefundSet = new Set(pendingRefundIds.value)
  pendingAcceptCount.value = currentOrderSet.size
  pendingRefundCount.value = currentRefundSet.size
  if (!initialized) {
    knownOrderIds = currentOrderSet
    knownRefundIds = currentRefundSet
    initialized = true
    return
  }

  const newOrders = [...currentOrderSet].filter(id => !knownOrderIds.has(id))
  const newRefunds = [...currentRefundSet].filter(id => !knownRefundIds.has(id))
  if (newOrders.length > 0) {
    ElNotification({
      title: '新订单提醒',
      type: 'success',
      message: `有 ${newOrders.length} 笔新的待接单订单，请及时处理`
    })
  }
  if (newRefunds.length > 0) {
    ElNotification({
      title: '退款提醒',
      type: 'warning',
      message: `有 ${newRefunds.length} 条新的退款申请，请尽快处理`
    })
  }
  knownOrderIds = currentOrderSet
  knownRefundIds = currentRefundSet
}

async function refreshData(allowNotify = false) {
  await Promise.all([loadOrders(), loadRefunds(), loadReminderOrders(), refreshNoticeCount()])
  if (allowNotify) {
    notifyNewData()
  } else {
    pendingAcceptCount.value = pendingAcceptIds.value.length
    pendingRefundCount.value = pendingRefundIds.value.length
    knownOrderIds = new Set(pendingAcceptIds.value)
    knownRefundIds = new Set(pendingRefundIds.value)
    initialized = true
  }
  lastRefreshTime.value = nowText()
}

async function openNoticeDrawer() {
  noticeOpen.value = true
  await loadNoticeList()
}

async function onNoticeSearch() {
  noticeQuery.pageNum = 1
  await loadNoticeList()
}

async function onNoticeReset() {
  noticeQuery.readFlag = defaultNoticeQuery.readFlag
  noticeQuery.noticeType = defaultNoticeQuery.noticeType
  noticeQuery.keyword = defaultNoticeQuery.keyword
  noticeQuery.pageNum = defaultNoticeQuery.pageNum
  noticeQuery.pageSize = defaultNoticeQuery.pageSize
  await loadNoticeList()
}

async function onNoticePageChange(pageNum) {
  noticeQuery.pageNum = pageNum
  await loadNoticeList()
}

async function markNoticeRead(item) {
  if (!item || item.readFlag !== '0') {
    return
  }
  try {
    await readFarmerNotice(item.noticeId)
    await Promise.all([loadNoticeList(), refreshNoticeCount()])
  } catch (e) {
    ElMessage.error(e.message || '消息标记失败')
  }
}

async function markAllNoticesRead() {
  try {
    const res = await readAllFarmerNotice()
    const rows = Number(res.data || 0)
    ElMessage.success(rows > 0 ? `已标记 ${rows} 条消息` : '暂无未读消息')
    noticeQuery.pageNum = 1
    await Promise.all([loadNoticeList(), refreshNoticeCount()])
  } catch (e) {
    ElMessage.error(e.message || '全部已读失败')
  }
}

function startTimer() {
  stopTimer()
  if (!autoRefresh.value) return
  timer = setInterval(() => {
    refreshData(true)
  }, 15000)
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function onAutoRefreshChange() {
  if (autoRefresh.value) {
    startTimer()
  } else {
    stopTimer()
  }
}

function accept(row) {
  acceptFarmerOrder(row.orderId, { remark: '已接单，准备打包' }).then(() => {
    ElMessage.success('接单成功')
    refreshData(false)
  }).catch(err => {
    ElMessage.error(err.message || '接单失败')
  })
}

function ship(row) {
  ElMessageBox.prompt('请输入发货备注', '发货').then(({ value }) => {
    shipFarmerOrder({ orderId: row.orderId, remark: value }).then(() => {
      ElMessage.success('发货成功')
      refreshData(false)
    }).catch(err => {
      ElMessage.error(err.message || '发货失败')
    })
  }).catch(() => {})
}

function handle(row, status) {
  const title = status === '1' ? '同意退款' : '拒绝退款'
  ElMessageBox.prompt('请输入处理备注', title).then(({ value }) => {
    handleFarmerRefund({ refundId: row.refundId, status, remark: value }).then(() => {
      ElMessage.success('处理成功')
      refreshData(false)
    }).catch(err => {
      ElMessage.error(err.message || '退款处理失败')
    })
  }).catch(() => {})
}

onMounted(() => {
  refreshData(false)
  startTimer()
})

onBeforeUnmount(() => {
  stopTimer()
})
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.mb12 { margin-bottom: 12px; }
.summary-row { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; align-items: center; }
.refresh-wrap { display: flex; align-items: center; justify-content: flex-end; gap: 12px; }
.refresh-time { color: #909399; font-size: 13px; }
.notice-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; color: #606266; }
.notice-filter { margin-bottom: 8px; }
.notice-list { display: grid; gap: 10px; }
.notice-item { border: 1px solid #ebeef5; border-radius: 8px; padding: 10px; background: #fff; }
.notice-item.unread { border-color: #b3d8ff; background: #f5faff; }
.notice-item__head { display: flex; justify-content: space-between; align-items: center; }
.notice-time { color: #909399; font-size: 12px; }
.notice-title { margin-top: 8px; font-weight: 600; color: #303133; }
.notice-content { margin-top: 6px; color: #606266; line-height: 1.5; }
.notice-actions { margin-top: 8px; display: flex; justify-content: flex-end; }
.notice-read-time { color: #909399; font-size: 12px; }
.notice-pagination { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>
