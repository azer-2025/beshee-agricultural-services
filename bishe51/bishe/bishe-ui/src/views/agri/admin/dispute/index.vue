<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="mb12">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 140px">
            <el-option label="待处理" value="0" />
            <el-option label="处理中" value="1" />
            <el-option label="已解决" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="订单号/标题/买家/农户" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="farmerName" label="农户" width="120" />
        <el-table-column prop="title" label="纠纷主题" min-width="170" show-overflow-tooltip />
        <el-table-column label="纠纷内容" min-width="210" show-overflow-tooltip>
          <template #default="scope">{{ plainText(scope.row.content) || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="处理结果" min-width="210" show-overflow-tooltip>
          <template #default="scope">{{ plainText(scope.row.handleResult) || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="openHandle(scope.row)">
              {{ String(scope.row.status) === '2' ? '查看' : '处理' }}
            </el-button>
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

    <el-dialog v-model="open" title="纠纷处理" width="920px">
      <div v-if="currentRow" class="dispute-detail-wrap">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentRow.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(currentRow.status) }}</el-descriptions-item>
          <el-descriptions-item label="买家">{{ currentRow.buyerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="农户">{{ currentRow.farmerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(currentRow.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDateTime(currentRow.handleTime) }}</el-descriptions-item>
          <el-descriptions-item label="纠纷主题" :span="2">{{ currentRow.title || '-' }}</el-descriptions-item>
          <el-descriptions-item label="纠纷内容" :span="2">
            <div class="rich-block" v-html="safeHtml(currentRow.content)"></div>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form :model="form" label-width="90px" class="mt16">
        <el-form-item label="处理状态">
          <el-select v-model="form.status" style="width: 220px">
            <el-option label="处理中" value="1" />
            <el-option label="已解决" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果">
          <Editor v-model="form.handleResult" :min-height="220" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { handleAdminDispute, listAdminDisputes } from '@/api/agri/mall'

const query = reactive({ pageNum: 1, pageSize: 10, status: '', keyword: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const open = ref(false)
const currentRow = ref(null)
const form = reactive({ disputeId: null, status: '1', handleResult: '' })

function statusText(status) {
  const map = { '0': '待处理', '1': '处理中', '2': '已解决' }
  return map[String(status)] || `状态${status}`
}

function statusTag(status) {
  const code = String(status)
  if (code === '0') return 'danger'
  if (code === '1') return 'warning'
  return 'success'
}

function formatDateTime(val) {
  return parseTime(val) || '-'
}

function plainText(html) {
  if (!html) return ''
  return String(html)
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function safeHtml(html) {
  if (!html) return '<p>-</p>'
  return String(html)
}

function onSearch() {
  query.pageNum = 1
  loadList()
}

function onReset() {
  query.status = ''
  query.keyword = ''
  query.pageNum = 1
  query.pageSize = 10
  loadList()
}

function loadList() {
  loading.value = true
  listAdminDisputes(query).then(res => {
    list.value = res.rows || []
    total.value = Number(res.total || 0)
  }).catch(err => {
    ElMessage.error(err.message || '纠纷列表加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function openHandle(row) {
  currentRow.value = row
  form.disputeId = row.disputeId
  form.status = row.status === '0' ? '1' : String(row.status || '1')
  form.handleResult = row.handleResult || ''
  open.value = true
}

function isRichEmpty(html) {
  return plainText(html).length === 0
}

function submit() {
  if (!form.disputeId) {
    ElMessage.warning('缺少纠纷ID')
    return
  }
  if (String(form.status) === '2' && isRichEmpty(form.handleResult)) {
    ElMessage.warning('请填写处理结果')
    return
  }
  submitting.value = true
  handleAdminDispute({
    disputeId: form.disputeId,
    status: form.status,
    handleResult: form.handleResult
  }).then(() => {
    ElMessage.success('处理完成')
    open.value = false
    loadList()
  }).catch(err => {
    ElMessage.error(err.message || '处理提交失败')
  }).finally(() => {
    submitting.value = false
  })
}

loadList()
</script>

<style scoped>
.mb12 { margin-bottom: 12px; }
.mt16 { margin-top: 16px; }
.dispute-detail-wrap { margin-bottom: 8px; }
.rich-block {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px 12px;
  background: #fafafa;
  line-height: 1.6;
  word-break: break-word;
}
</style>
