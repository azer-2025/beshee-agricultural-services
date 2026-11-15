<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-tabs v-model="activePanel">
        <el-tab-pane label="咨询消息" name="consult" />
        <el-tab-pane label="订单消息" name="notice" />
        <el-tab-pane label="评价管理" name="review" />
      </el-tabs>

      <template v-if="activePanel === 'consult'">
        <el-form :inline="true" :model="consultQuery" class="mb12">
          <el-form-item label="状态">
            <el-select v-model="consultQuery.status" clearable style="width: 120px">
              <el-option label="待处理" value="0" />
              <el-option label="处理中" value="1" />
              <el-option label="已回复" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="consultQuery.keyword" clearable placeholder="订单号/标题/内容/用户" style="width: 260px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onConsultSearch">查询</el-button>
            <el-button @click="onConsultReset">重置</el-button>
            <el-button type="success" @click="openConsultCreate">新增咨询</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="consultList" border v-loading="loading">
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="consultStatusTag(scope.row.status)">{{ consultStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="订单号" width="190" />
          <el-table-column prop="buyerName" label="买家" width="120" />
          <el-table-column prop="farmerName" label="农户" width="120" />
          <el-table-column prop="title" label="咨询主题" min-width="160" show-overflow-tooltip />
          <el-table-column label="咨询内容" min-width="220" show-overflow-tooltip>
            <template #default="scope">{{ plainText(scope.row.content) || '-' }}</template>
          </el-table-column>
          <el-table-column label="管理员回复" min-width="220" show-overflow-tooltip>
            <template #default="scope">{{ plainText(scope.row.handleResult) || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="回复时间" width="170">
            <template #default="scope">{{ formatDateTime(scope.row.handleTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button link type="primary" @click="openConsultEdit(scope.row)">编辑/回复</el-button>
              <el-button link type="danger" @click="removeConsult(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="consultTotal > 0"
          :total="consultTotal"
          v-model:page="consultQuery.pageNum"
          v-model:limit="consultQuery.pageSize"
          @pagination="loadConsultList"
        />
      </template>

      <template v-else-if="activePanel === 'notice'">
        <div class="notice-summary">
          <el-tag type="danger">未读消息：{{ noticeUnreadCount }}</el-tag>
        </div>

        <el-form :inline="true" :model="noticeQuery" class="mb12">
          <el-form-item label="状态">
            <el-select v-model="noticeQuery.readFlag" clearable style="width: 120px">
              <el-option label="未读" value="0" />
              <el-option label="已读" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="noticeQuery.noticeType" clearable style="width: 140px">
              <el-option label="订单" value="ORDER" />
              <el-option label="纠纷" value="DISPUTE" />
              <el-option label="退款" value="REFUND" />
              <el-option label="农户申请" value="FARMER_APPLY" />
              <el-option label="商品审核" value="PRODUCT_AUDIT" />
              <el-option label="系统" value="SYSTEM" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="noticeQuery.keyword" clearable placeholder="标题/内容关键字" style="width: 260px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onNoticeSearch">查询</el-button>
            <el-button @click="onNoticeReset">重置</el-button>
            <el-button @click="loadNoticeList">刷新</el-button>
            <el-button type="success" @click="markAllNoticeRead">全部已读</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="noticeList" border v-loading="loading">
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="String(scope.row.readFlag) === '1' ? 'success' : 'danger'">
                {{ String(scope.row.readFlag) === '1' ? '已读' : '未读' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="scope">
              <el-tag type="info">{{ noticeTypeText(scope.row.noticeType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="内容" min-width="280" show-overflow-tooltip>
            <template #default="scope">{{ plainText(scope.row.content) || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="已读时间" width="170">
            <template #default="scope">{{ formatDateTime(scope.row.readTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button link type="primary" @click="openNoticeDetail(scope.row)">查看</el-button>
              <el-button
                link
                type="success"
                v-if="String(scope.row.readFlag) !== '1'"
                @click="markNoticeRead(scope.row)"
              >
                标记已读
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="noticeTotal > 0"
          :total="noticeTotal"
          v-model:page="noticeQuery.pageNum"
          v-model:limit="noticeQuery.pageSize"
          @pagination="loadNoticeList"
        />
      </template>

      <template v-else>
        <el-form :inline="true" :model="reviewQuery" class="mb12">
          <el-form-item>
            <el-radio-group v-model="reviewTab" @change="onReviewTabChange">
              <el-radio-button label="pending">待评价</el-radio-button>
              <el-radio-button label="reviewed">已评价</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="reviewQuery.keyword" clearable placeholder="订单号/商品/买家/农户" style="width: 260px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onReviewSearch">查询</el-button>
            <el-button @click="onReviewReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="reviewList" border v-loading="loading">
          <template v-if="reviewTab === 'pending'">
            <el-table-column prop="orderNo" label="订单号" min-width="190" />
            <el-table-column prop="buyerName" label="买家" width="120" />
            <el-table-column prop="farmerName" label="农户" width="120" />
            <el-table-column prop="productName" label="商品" min-width="160" />
            <el-table-column label="金额" width="100">
              <template #default="scope">￥{{ Number(scope.row.amount || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="完成时间" width="170">
              <template #default="scope">{{ formatDateTime(scope.row.finishTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default>
                <el-tag type="warning">待评价</el-tag>
              </template>
            </el-table-column>
          </template>
          <template v-else>
            <el-table-column prop="orderNo" label="订单号" min-width="190" />
            <el-table-column prop="buyerName" label="买家" width="120" />
            <el-table-column prop="farmerName" label="农户" width="120" />
            <el-table-column prop="productName" label="商品" min-width="160" />
            <el-table-column label="质量" width="80" align="center">
              <template #default="scope">{{ scope.row.qualityStar || 0 }}</template>
            </el-table-column>
            <el-table-column label="包装" width="80" align="center">
              <template #default="scope">{{ scope.row.packageStar || 0 }}</template>
            </el-table-column>
            <el-table-column label="服务" width="80" align="center">
              <template #default="scope">{{ scope.row.serviceStar || 0 }}</template>
            </el-table-column>
            <el-table-column label="评价内容" min-width="220" show-overflow-tooltip>
              <template #default="scope">{{ plainText(scope.row.content) || '-' }}</template>
            </el-table-column>
            <el-table-column label="评价时间" width="170">
              <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default>
                <el-tag type="success">已评价</el-tag>
              </template>
            </el-table-column>
          </template>
        </el-table>

        <pagination
          v-show="reviewTotal > 0"
          :total="reviewTotal"
          v-model:page="reviewQuery.pageNum"
          v-model:limit="reviewQuery.pageSize"
          @pagination="loadReviewList"
        />
      </template>
    </el-card>

    <el-dialog v-model="consultOpen" :title="consultForm.disputeId ? '编辑咨询' : '新增咨询'" width="640px">
      <el-form :model="consultForm" label-width="90px">
        <el-form-item label="订单ID">
          <el-input-number v-model="consultForm.orderId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="咨询主题">
          <el-input v-model="consultForm.title" maxlength="120" />
        </el-form-item>
        <el-form-item label="咨询内容">
          <el-input v-model="consultForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="consultForm.status" style="width: 100%">
            <el-option label="待处理" value="0" />
            <el-option label="处理中" value="1" />
            <el-option label="已回复" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理员回复">
          <el-input v-model="consultForm.handleResult" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consultOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitConsult">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeDetailOpen" title="消息详情" width="620px">
      <el-descriptions :column="1" border v-if="currentNotice">
        <el-descriptions-item label="类型">{{ noticeTypeText(currentNotice.noticeType) }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentNotice.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ plainText(currentNotice.content) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentNotice.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="已读时间">{{ formatDateTime(currentNotice.readTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import {
  countAdminUnreadNotices,
  listAdminConsults,
  listAdminNotices,
  listAdminPendingReviews,
  listAdminReviews,
  readAdminNotice,
  readAllAdminNotice,
  removeAdminConsult,
  saveAdminConsult
} from '@/api/agri/mall'

const activePanel = ref('consult')
const reviewTab = ref('pending')
const loading = ref(false)
const saving = ref(false)

const consultList = ref([])
const consultTotal = ref(0)
const consultQuery = reactive({ pageNum: 1, pageSize: 10, status: '', keyword: '' })
const consultOpen = ref(false)
const consultForm = reactive({
  disputeId: null,
  orderId: undefined,
  title: '',
  content: '',
  status: '0',
  handleResult: ''
})

const noticeList = ref([])
const noticeTotal = ref(0)
const noticeUnreadCount = ref(0)
const noticeQuery = reactive({ pageNum: 1, pageSize: 10, readFlag: '', noticeType: '', keyword: '' })
const noticeDetailOpen = ref(false)
const currentNotice = ref(null)

const reviewList = ref([])
const reviewTotal = ref(0)
const reviewQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const CONSULT_STATUS = {
  '0': '待处理',
  '1': '处理中',
  '2': '已回复'
}

const NOTICE_TYPE = {
  ORDER: '订单',
  DISPUTE: '纠纷',
  REFUND: '退款',
  FARMER_APPLY: '农户申请',
  PRODUCT_AUDIT: '商品审核',
  SYSTEM: '系统'
}

function formatDateTime(value) {
  return parseTime(value) || '-'
}

function plainText(text) {
  if (!text) return ''
  return String(text)
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function noticeTypeText(type) {
  return NOTICE_TYPE[String(type)] || String(type || '-')
}

function consultStatusText(status) {
  return CONSULT_STATUS[String(status)] || `状态${status}`
}

function consultStatusTag(status) {
  const code = String(status)
  if (code === '0') return 'warning'
  if (code === '1') return 'primary'
  if (code === '2') return 'success'
  return 'info'
}

function resetConsultForm() {
  consultForm.disputeId = null
  consultForm.orderId = undefined
  consultForm.title = ''
  consultForm.content = ''
  consultForm.status = '0'
  consultForm.handleResult = ''
}

function loadConsultList() {
  loading.value = true
  listAdminConsults(consultQuery).then((res) => {
    consultList.value = res.rows || []
    consultTotal.value = Number(res.total || 0)
  }).catch((err) => {
    ElMessage.error(err.message || '咨询列表加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function onConsultSearch() {
  consultQuery.pageNum = 1
  loadConsultList()
}

function onConsultReset() {
  consultQuery.status = ''
  consultQuery.keyword = ''
  consultQuery.pageNum = 1
  consultQuery.pageSize = 10
  loadConsultList()
}

function openConsultCreate() {
  resetConsultForm()
  consultOpen.value = true
}

function openConsultEdit(row) {
  consultForm.disputeId = row.disputeId
  consultForm.orderId = Number(row.orderId || 0) || undefined
  consultForm.title = row.title || ''
  consultForm.content = plainText(row.content)
  consultForm.status = String(row.status || '0')
  consultForm.handleResult = plainText(row.handleResult)
  consultOpen.value = true
}

function submitConsult() {
  if (!consultForm.orderId) {
    ElMessage.warning('请填写订单ID')
    return
  }
  if (!consultForm.title || !consultForm.content) {
    ElMessage.warning('请填写咨询主题和内容')
    return
  }
  saving.value = true
  saveAdminConsult({
    disputeId: consultForm.disputeId,
    orderId: consultForm.orderId,
    title: consultForm.title,
    content: consultForm.content,
    status: consultForm.status,
    handleResult: consultForm.handleResult
  }).then(() => {
    ElMessage.success(consultForm.disputeId ? '咨询更新成功' : '咨询新增成功')
    consultOpen.value = false
    loadConsultList()
  }).catch((err) => {
    ElMessage.error(err.message || '保存失败')
  }).finally(() => {
    saving.value = false
  })
}

function removeConsult(row) {
  ElMessageBox.confirm('确认删除该咨询记录吗？', '提示', { type: 'warning' }).then(() => {
    return removeAdminConsult(row.disputeId)
  }).then(() => {
    ElMessage.success('删除成功')
    loadConsultList()
  }).catch(() => {})
}

function loadNoticeList() {
  loading.value = true
  listAdminNotices(noticeQuery).then((res) => {
    noticeList.value = res.rows || []
    noticeTotal.value = Number(res.total || 0)
  }).catch((err) => {
    ElMessage.error(err.message || '订单消息加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function loadNoticeUnreadCount() {
  countAdminUnreadNotices().then((res) => {
    noticeUnreadCount.value = Number(res.data || 0)
  }).catch(() => {
    noticeUnreadCount.value = 0
  })
}

function onNoticeSearch() {
  noticeQuery.pageNum = 1
  loadNoticeList()
}

function onNoticeReset() {
  noticeQuery.readFlag = ''
  noticeQuery.noticeType = ''
  noticeQuery.keyword = ''
  noticeQuery.pageNum = 1
  noticeQuery.pageSize = 10
  loadNoticeList()
}

function openNoticeDetail(row) {
  currentNotice.value = row
  noticeDetailOpen.value = true
}

function markNoticeRead(row) {
  readAdminNotice(row.noticeId).then(() => {
    ElMessage.success('已标记为已读')
    loadNoticeList()
    loadNoticeUnreadCount()
  }).catch((err) => {
    ElMessage.error(err.message || '操作失败')
  })
}

function markAllNoticeRead() {
  readAllAdminNotice().then(() => {
    ElMessage.success('已全部标记为已读')
    loadNoticeList()
    loadNoticeUnreadCount()
  }).catch((err) => {
    ElMessage.error(err.message || '操作失败')
  })
}

function loadReviewList() {
  loading.value = true
  const request = reviewTab.value === 'pending' ? listAdminPendingReviews(reviewQuery) : listAdminReviews(reviewQuery)
  request.then((res) => {
    reviewList.value = res.rows || []
    reviewTotal.value = Number(res.total || 0)
  }).catch((err) => {
    ElMessage.error(err.message || '评价列表加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function onReviewTabChange() {
  reviewQuery.pageNum = 1
  loadReviewList()
}

function onReviewSearch() {
  reviewQuery.pageNum = 1
  loadReviewList()
}

function onReviewReset() {
  reviewQuery.keyword = ''
  reviewQuery.pageNum = 1
  reviewQuery.pageSize = 10
  loadReviewList()
}

function handlePanelChange(name) {
  if (name === 'consult') {
    loadConsultList()
    return
  }
  if (name === 'notice') {
    loadNoticeList()
    loadNoticeUnreadCount()
    return
  }
  loadReviewList()
}

loadConsultList()
loadNoticeUnreadCount()
watch(activePanel, (val) => {
  handlePanelChange(val)
})
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}

.notice-summary {
  margin-bottom: 12px;
}
</style>
