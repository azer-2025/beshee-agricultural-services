<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="订单状态">
          <el-select v-model="query.orderStatus" clearable style="width: 140px">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="query.payStatus" clearable style="width: 140px">
            <el-option label="待支付" value="0" />
            <el-option label="已支付" value="1" />
            <el-option label="已退款" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="订单号/买家/农户" style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="openCreate">新建订单</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="200" />
        <el-table-column prop="buyerName" label="买家" width="110" />
        <el-table-column prop="farmerName" label="农户" width="110" />
        <el-table-column label="金额" width="120">
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
        <el-table-column label="下单时间" width="170">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="380">
          <template #default="scope">
            <el-button link @click="showItems(scope.row)">详情</el-button>
            <el-button link @click="showTrace(scope.row)">轨迹</el-button>
            <el-button
              link
              type="success"
              v-if="scope.row.payStatus === '1' || scope.row.payStatus === '2'"
              @click="showVoucher(scope.row)"
            >
              凭证
            </el-button>
            <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" v-if="canAccept(scope.row)" @click="quickUpdate(scope.row, '1', '接单')">接单</el-button>
            <el-button link type="warning" v-if="canShip(scope.row)" @click="quickUpdate(scope.row, '2', '发货')">发货</el-button>
            <el-button link type="success" v-if="canFinish(scope.row)" @click="quickUpdate(scope.row, '4', '完成')">完成</el-button>
            <el-button link type="info" v-if="canCancel(scope.row)" @click="quickUpdate(scope.row, '5', '取消')">取消</el-button>
            <el-button link type="danger" v-if="canDelete(scope.row)" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        @pagination="loadList"
      />
    </el-card>

    <el-dialog v-model="itemsOpen" title="订单详情" width="760px">
      <el-table :data="orderItems" border>
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="amount" label="金额" width="100" />
      </el-table>
      <el-empty v-if="orderItems.length === 0" description="暂无订单明细" />
    </el-dialog>

    <el-dialog v-model="traceOpen" title="订单轨迹" width="620px">
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
        <el-descriptions-item label="凭证号">{{ voucherData.payNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ channelText(voucherData.channel, voucherData.channelName) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatDateTime(voucherData.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">￥{{ Number(voucherData.payAmount || 0).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无支付凭证" />
    </el-dialog>

    <el-dialog v-model="createOpen" title="新建订单" width="560px">
      <el-form :model="createForm" label-width="95px">
        <el-form-item label="买家ID">
          <el-input-number v-model="createForm.buyerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="农户ID">
          <el-input-number v-model="createForm.farmerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实付金额">
          <el-input-number v-model="createForm.payAmount" :min="0.01" :precision="2" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="运费">
          <el-input-number v-model="createForm.freightAmount" :min="0" :precision="2" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="createForm.payStatus" style="width: 100%">
            <el-option label="待支付" value="0" />
            <el-option label="已支付" value="1" />
            <el-option label="已退款" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="createForm.orderStatus" style="width: 100%">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="createForm.receiverName" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.receiverPhone" maxlength="20" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="createForm.receiverAddress" maxlength="255" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.farmerRemark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createOpen = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editOpen" title="编辑订单" width="520px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="订单号">
          <el-input :model-value="editForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="editForm.payStatus" style="width: 100%">
            <el-option label="待支付" value="0" />
            <el-option label="已支付" value="1" />
            <el-option label="已退款" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="editForm.orderStatus" style="width: 100%">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.farmerRemark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import {
  createAdminOrder,
  getAdminOrderVoucher,
  listAdminOrderItems,
  listAdminOrders,
  listAdminOrderTrace,
  removeAdminOrder,
  updateAdminOrder
} from '@/api/agri/mall'

const loading = ref(false)
const creating = ref(false)
const saving = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, orderStatus: '', payStatus: '', keyword: '' })
const list = ref([])
const total = ref(0)

const itemsOpen = ref(false)
const traceOpen = ref(false)
const voucherOpen = ref(false)
const createOpen = ref(false)
const editOpen = ref(false)

const orderItems = ref([])
const traceList = ref([])
const voucherData = ref(null)
const voucherOrderNo = ref('')

const createForm = reactive({
  buyerId: undefined,
  farmerId: undefined,
  payAmount: 0.01,
  freightAmount: 0,
  payStatus: '0',
  orderStatus: '0',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  farmerRemark: ''
})

const editForm = reactive({
  orderId: null,
  orderNo: '',
  payStatus: '0',
  orderStatus: '0',
  farmerRemark: ''
})

const orderStatusOptions = [
  { label: '待接单', value: '0' },
  { label: '待发货', value: '1' },
  { label: '运输中', value: '2' },
  { label: '待收货', value: '3' },
  { label: '已完成', value: '4' },
  { label: '已取消', value: '5' },
  { label: '退款中', value: '6' },
  { label: '已退款', value: '7' }
]

const PAY_STATUS = { '0': '待支付', '1': '已支付', '2': '已退款' }
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
  CANCELLED: '订单取消',
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
  if (code === '0' || code === '1' || code === '6') return 'warning'
  if (code === '2' || code === '3') return 'primary'
  if (code === '4') return 'success'
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

function canAccept(row) {
  return String(row.payStatus) === '1' && String(row.orderStatus) === '0'
}

function canShip(row) {
  return String(row.orderStatus) === '1'
}

function canFinish(row) {
  const status = String(row.orderStatus)
  return status === '2' || status === '3'
}

function canCancel(row) {
  const status = String(row.orderStatus)
  return status !== '4' && status !== '5' && status !== '7'
}

function canDelete(row) {
  const status = String(row.orderStatus)
  return status === '4' || status === '5' || status === '7'
}

function loadList() {
  loading.value = true
  listAdminOrders(query).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).catch(err => {
    ElMessage.error(err.message || '订单列表加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function resetCreateForm() {
  createForm.buyerId = undefined
  createForm.farmerId = undefined
  createForm.payAmount = 0.01
  createForm.freightAmount = 0
  createForm.payStatus = '0'
  createForm.orderStatus = '0'
  createForm.receiverName = ''
  createForm.receiverPhone = ''
  createForm.receiverAddress = ''
  createForm.farmerRemark = ''
}

function openCreate() {
  resetCreateForm()
  createOpen.value = true
}

function submitCreate() {
  if (!createForm.buyerId || !createForm.farmerId) {
    ElMessage.warning('请填写买家ID和农户ID')
    return
  }
  if (!createForm.payAmount || Number(createForm.payAmount) <= 0) {
    ElMessage.warning('实付金额必须大于0')
    return
  }
  creating.value = true
  createAdminOrder({
    buyerId: createForm.buyerId,
    farmerId: createForm.farmerId,
    payAmount: createForm.payAmount,
    freightAmount: createForm.freightAmount,
    payStatus: createForm.payStatus,
    orderStatus: createForm.orderStatus,
    receiverName: createForm.receiverName,
    receiverPhone: createForm.receiverPhone,
    receiverAddress: createForm.receiverAddress,
    farmerRemark: createForm.farmerRemark
  }).then(res => {
    const orderNo = res?.data?.orderNo || ''
    ElMessage.success(orderNo ? `订单创建成功：${orderNo}` : '订单创建成功')
    createOpen.value = false
    loadList()
  }).catch(err => {
    ElMessage.error(err.message || '订单创建失败')
  }).finally(() => {
    creating.value = false
  })
}

function showItems(row) {
  listAdminOrderItems(row.orderId).then(res => {
    orderItems.value = res.data || []
    itemsOpen.value = true
  }).catch(err => {
    ElMessage.error(err.message || '订单详情加载失败')
  })
}

function showTrace(row) {
  listAdminOrderTrace(row.orderId).then(res => {
    traceList.value = res.data || []
    traceOpen.value = true
  }).catch(err => {
    ElMessage.error(err.message || '订单轨迹加载失败')
  })
}

function showVoucher(row) {
  getAdminOrderVoucher(row.orderId).then(res => {
    voucherData.value = res.data || null
    voucherOrderNo.value = row.orderNo
    voucherOpen.value = true
  }).catch(err => {
    ElMessage.error(err.message || '支付凭证加载失败')
  })
}

function openEdit(row) {
  editForm.orderId = row.orderId
  editForm.orderNo = row.orderNo
  editForm.payStatus = String(row.payStatus || '0')
  editForm.orderStatus = String(row.orderStatus || '0')
  editForm.farmerRemark = row.farmerRemark || ''
  editOpen.value = true
}

function submitEdit() {
  if (!editForm.orderId) return
  saving.value = true
  updateAdminOrder({
    orderId: editForm.orderId,
    payStatus: editForm.payStatus,
    orderStatus: editForm.orderStatus,
    farmerRemark: editForm.farmerRemark
  }).then(() => {
    ElMessage.success('订单更新成功')
    editOpen.value = false
    loadList()
  }).catch(err => {
    ElMessage.error(err.message || '订单更新失败')
  }).finally(() => {
    saving.value = false
  })
}

function quickUpdate(row, targetStatus, actionText) {
  ElMessageBox.confirm(`确认${actionText}该订单吗？`, '提示', { type: 'warning' }).then(() => {
    return updateAdminOrder({
      orderId: row.orderId,
      orderStatus: targetStatus
    })
  }).then(() => {
    ElMessage.success(`订单已${actionText}`)
    loadList()
  }).catch(() => {})
}

function remove(row) {
  ElMessageBox.confirm('删除后不可恢复，确认删除该订单吗？', '警告', { type: 'warning' }).then(() => {
    return removeAdminOrder(row.orderId)
  }).then(() => {
    ElMessage.success('订单删除成功')
    loadList()
  }).catch(() => {})
}

onMounted(loadList)
</script>
