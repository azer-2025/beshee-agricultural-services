<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="订单状态">
          <el-select v-model="query.orderStatus" clearable style="width: 150px">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :value="item.value" :label="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="query.payStatus" clearable style="width: 150px">
            <el-option label="待支付" value="0" />
            <el-option label="已支付" value="1" />
            <el-option label="已退款" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="orderList" border v-loading="loading">
        <el-table-column label="订单号" prop="orderNo" width="220" />
        <el-table-column label="农户" prop="farmerName" width="120" />
        <el-table-column label="应付" width="120">
          <template #default="scope">￥{{ Number(scope.row.payAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="支付状态" width="110">
          <template #default="scope">
            <el-tag :type="payStatusTagType(scope.row.payStatus)">{{ payStatusText(scope.row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="orderStatusTagType(scope.row.orderStatus)">{{ orderStatusText(scope.row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="360">
          <template #default="scope">
            <el-button link @click="showItems(scope.row)">明细</el-button>
            <el-button link @click="showTrace(scope.row)">物流</el-button>
            <el-button link type="success" v-if="scope.row.payStatus === '1' || scope.row.payStatus === '2'" @click="showVoucher(scope.row)">凭证</el-button>
            <el-button link type="success" v-if="scope.row.payStatus === '0'" @click="pay(scope.row)">模拟支付</el-button>
            <el-button link type="primary" v-if="scope.row.orderStatus === '2' || scope.row.orderStatus === '3'" @click="confirm(scope.row)">确认收货</el-button>
            <el-button link type="primary" v-if="scope.row.orderStatus === '4' && hasPendingReview(scope.row.orderId)" @click="goReview(scope.row)">去评价</el-button>
            <el-button link type="warning" v-if="scope.row.orderStatus === '1' || scope.row.orderStatus === '2' || scope.row.orderStatus === '3'" @click="refund(scope.row)">申请退款</el-button>
            <el-button link type="danger" @click="openDispute(scope.row)">发起纠纷</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        @pagination="loadOrders"
      />
    </el-card>

    <el-dialog v-model="itemsOpen" title="订单明细" width="760px">
      <el-table :data="orderItems" border>
        <el-table-column label="商品" prop="productName" />
        <el-table-column label="单价" prop="price" width="100" />
        <el-table-column label="数量" prop="quantity" width="80" />
        <el-table-column label="金额" prop="amount" width="100" />
        <el-table-column label="评价" prop="reviewStatus" width="90" />
      </el-table>
      <el-empty v-if="orderItems.length === 0" description="暂无明细数据" />
    </el-dialog>

    <el-dialog v-model="traceOpen" title="物流轨迹" width="620px">
      <el-timeline v-if="traceList.length">
        <el-timeline-item
          v-for="item in traceList"
          :key="item.traceId || `${item.statusNode}-${item.traceTime}`"
          :timestamp="formatDateTime(item.traceTime)"
        >
          {{ traceNodeText(item.statusNode) }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无物流轨迹" />
    </el-dialog>

    <el-dialog v-model="voucherOpen" title="支付凭证" width="560px">
      <el-descriptions :column="1" border v-if="voucherData && voucherData.payNo">
        <el-descriptions-item label="订单号">{{ voucherOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="凭证号">{{ voucherData.payNo }}</el-descriptions-item>
        <el-descriptions-item label="支付通道">{{ channelText(voucherData.channel, voucherData.channelName) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatDateTime(voucherData.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">￥{{ Number(voucherData.payAmount || 0).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无支付凭证" />
    </el-dialog>

    <el-dialog v-model="disputeOpen" title="发起纠纷" width="520px">
      <el-form :model="disputeForm" label-width="70px">
        <el-form-item label="标题"><el-input v-model="disputeForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="disputeForm.content" type="textarea" rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disputeOpen = false">取消</el-button>
        <el-button type="primary" @click="submitDispute">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import {
  applyBuyerRefund,
  confirmBuyerOrder,
  createBuyerDispute,
  getBuyerOrderVoucher,
  listBuyerOrderItems,
  listBuyerOrderTrace,
  listBuyerOrders,
  listBuyerPendingReviews,
  payBuyerOrder
} from '@/api/agri/mall'

const router = useRouter()
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, orderStatus: '', payStatus: '' })
const orderList = ref([])
const total = ref(0)
const orderItems = ref([])
const traceList = ref([])
const itemsOpen = ref(false)
const traceOpen = ref(false)
const voucherOpen = ref(false)
const voucherData = ref(null)
const voucherOrderNo = ref('')
const disputeOpen = ref(false)
const currentOrderId = ref(null)
const disputeForm = reactive({ title: '', content: '' })
const pendingOrderSet = ref(new Set())

const orderStatusOptions = [
  { label: '待接单', value: '0' },
  { label: '待发货', value: '1' },
  { label: '运输中', value: '2' },
  { label: '待收货', value: '3' },
  { label: '已完成', value: '4' },
  { label: '退款中', value: '6' },
  { label: '已退款', value: '7' }
]

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

const TRACE_NODE = {
  ORDER_CREATED: '订单创建',
  PAID: '支付成功',
  ACCEPTED: '农户接单',
  SHIPPED: '已发货',
  IN_TRANSIT: '运输中',
  OUT_FOR_DELIVERY: '派送中',
  RECEIVED: '已签收',
  REFUND_APPLY: '退款申请',
  REFUND_APPROVED: '退款通过',
  REFUND_REJECTED: '退款驳回'
}

function payStatusText(val) {
  return PAY_STATUS[String(val)] || `状态${val}`
}

function orderStatusText(val) {
  return ORDER_STATUS[String(val)] || `状态${val}`
}

function payStatusTagType(val) {
  const code = String(val)
  if (code === '0') return 'warning'
  if (code === '1') return 'success'
  if (code === '2') return 'info'
  return 'info'
}

function orderStatusTagType(val) {
  const code = String(val)
  if (code === '0' || code === '1') return 'warning'
  if (code === '2' || code === '3') return 'primary'
  if (code === '4') return 'success'
  if (code === '6') return 'warning'
  if (code === '7') return 'info'
  return 'info'
}

function traceNodeText(node) {
  return TRACE_NODE[String(node)] || String(node || '-')
}

function channelText(channel, channelName) {
  if (channelName) return channelName
  return String(channel) === '2' ? '支付宝沙箱' : '模拟支付SDK'
}

function formatDateTime(value) {
  return parseTime(value) || '-'
}

function loadPendingMap() {
  listBuyerPendingReviews().then(res => {
    const set = new Set((res.data || []).map(item => Number(item.orderId)))
    pendingOrderSet.value = set
  }).catch(() => {
    pendingOrderSet.value = new Set()
  })
}

function loadOrders() {
  loading.value = true
  listBuyerOrders(query).then(res => {
    orderList.value = res.rows || []
    total.value = res.total || 0
    loadPendingMap()
  }).catch(err => {
    ElMessage.error(err.message || '订单加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function showItems(row) {
  listBuyerOrderItems(row.orderId).then(res => {
    orderItems.value = res.data || []
    itemsOpen.value = true
  }).catch(err => {
    ElMessage.error(err.message || '订单明细加载失败')
  })
}

function showTrace(row) {
  listBuyerOrderTrace(row.orderId).then(res => {
    traceList.value = res.data || []
    traceOpen.value = true
  }).catch(err => {
    ElMessage.error(err.message || '物流轨迹加载失败')
  })
}

function showVoucher(row) {
  getBuyerOrderVoucher(row.orderId).then(res => {
    voucherData.value = res.data || null
    voucherOrderNo.value = row.orderNo
    voucherOpen.value = true
  }).catch(err => {
    ElMessage.error(err.message || '支付凭证加载失败')
  })
}

function pay(row) {
  payBuyerOrder({ orderId: row.orderId, channel: '1' }).then(res => {
    const payNo = res?.data?.payNo ? `，凭证号 ${res.data.payNo}` : ''
    ElMessage.success(`支付成功${payNo}`)
    loadOrders()
  }).catch(err => {
    ElMessage.error(err.message || '支付失败')
  })
}

function confirm(row) {
  confirmBuyerOrder(row.orderId).then(() => {
    ElMessage.success('已确认收货，可前往评价')
    loadOrders()
  }).catch(err => {
    ElMessage.error(err.message || '确认收货失败')
  })
}

function goReview(row) {
  router.push(`/buyer/review?orderId=${row.orderId}`)
}

function hasPendingReview(orderId) {
  return pendingOrderSet.value.has(Number(orderId))
}

function refund(row) {
  ElMessageBox.prompt('请输入退款原因', '退款申请').then(({ value }) => {
    applyBuyerRefund({ orderId: row.orderId, reason: value }).then(() => {
      ElMessage.success('退款申请已提交')
      loadOrders()
    }).catch(err => {
      ElMessage.error(err.message || '退款申请失败')
    })
  }).catch(() => {})
}

function openDispute(row) {
  currentOrderId.value = row.orderId
  disputeForm.title = ''
  disputeForm.content = ''
  disputeOpen.value = true
}

function submitDispute() {
  if (!disputeForm.title || !disputeForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  createBuyerDispute({ orderId: currentOrderId.value, ...disputeForm }).then(() => {
    ElMessage.success('纠纷已提交')
    disputeOpen.value = false
  }).catch(err => {
    ElMessage.error(err.message || '纠纷提交失败')
  })
}

onMounted(loadOrders)
</script>
