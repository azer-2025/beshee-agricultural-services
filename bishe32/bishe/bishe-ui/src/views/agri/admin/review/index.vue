<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="mb12">
        <el-form-item>
          <el-radio-group v-model="reviewTab" @change="onTabChange">
            <el-radio-button label="pending">待评价</el-radio-button>
            <el-radio-button label="reviewed">已评价</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="订单号/商品/买家/农户" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border v-loading="loading">
        <template v-if="reviewTab === 'pending'">
          <el-table-column prop="orderNo" label="订单号" min-width="200" />
          <el-table-column prop="buyerName" label="买家" width="120" />
          <el-table-column prop="farmerName" label="农户" width="120" />
          <el-table-column prop="productName" label="商品" min-width="160" />
          <el-table-column label="金额" width="110">
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
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button link type="primary" @click="openCreate(scope.row)">新增评价</el-button>
            </template>
          </el-table-column>
        </template>

        <template v-else>
          <el-table-column prop="orderNo" label="订单号" min-width="200" />
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
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" @click="removeRow(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </template>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        @pagination="loadList"
      />
    </el-card>

    <el-dialog v-model="open" :title="form.reviewId ? '编辑评价' : '新增评价'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="订单号">
          <el-input :model-value="form.orderNo || '-'" disabled />
        </el-form-item>
        <el-form-item label="商品">
          <el-input :model-value="form.productName || '-'" disabled />
        </el-form-item>
        <el-form-item label="质量评分">
          <el-rate v-model="form.qualityStar" :max="5" />
        </el-form-item>
        <el-form-item label="包装评分">
          <el-rate v-model="form.packageStar" :max="5" />
        </el-form-item>
        <el-form-item label="服务评分">
          <el-rate v-model="form.serviceStar" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { listAdminPendingReviews, listAdminReviews, removeAdminReview, saveAdminReview } from '@/api/agri/mall'

const loading = ref(false)
const saving = ref(false)
const open = ref(false)
const reviewTab = ref('pending')
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const form = reactive(createInitForm())

function createInitForm() {
  return {
    reviewId: null,
    itemId: null,
    orderNo: '',
    productName: '',
    qualityStar: 5,
    packageStar: 5,
    serviceStar: 5,
    content: ''
  }
}

function resetForm() {
  Object.assign(form, createInitForm())
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

function loadList() {
  loading.value = true
  const request = reviewTab.value === 'pending' ? listAdminPendingReviews(query) : listAdminReviews(query)
  request.then((res) => {
    list.value = res.rows || []
    total.value = Number(res.total || 0)
  }).catch((err) => {
    ElMessage.error(err.message || '评价列表加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function onTabChange() {
  query.pageNum = 1
  loadList()
}

function onSearch() {
  query.pageNum = 1
  loadList()
}

function onReset() {
  query.keyword = ''
  query.pageNum = 1
  query.pageSize = 10
  loadList()
}

function openCreate(row) {
  resetForm()
  form.itemId = row.itemId
  form.orderNo = row.orderNo || ''
  form.productName = row.productName || ''
  open.value = true
}

function openEdit(row) {
  resetForm()
  form.reviewId = row.reviewId
  form.itemId = row.orderItemId
  form.orderNo = row.orderNo || ''
  form.productName = row.productName || ''
  form.qualityStar = Number(row.qualityStar || 5)
  form.packageStar = Number(row.packageStar || 5)
  form.serviceStar = Number(row.serviceStar || 5)
  form.content = plainText(row.content)
  open.value = true
}

function submitForm() {
  if (!form.reviewId && !form.itemId) {
    ElMessage.warning('缺少订单项信息')
    return
  }
  saving.value = true
  saveAdminReview({
    reviewId: form.reviewId,
    itemId: form.itemId,
    qualityStar: Number(form.qualityStar || 5),
    packageStar: Number(form.packageStar || 5),
    serviceStar: Number(form.serviceStar || 5),
    content: form.content || ''
  }).then(() => {
    ElMessage.success(form.reviewId ? '评价更新成功' : '评价新增成功')
    open.value = false
    loadList()
  }).catch((err) => {
    ElMessage.error(err.message || '保存失败')
  }).finally(() => {
    saving.value = false
  })
}

function removeRow(row) {
  ElMessageBox.confirm('确认删除该评价吗？', '提示', { type: 'warning' }).then(() => {
    return removeAdminReview(row.reviewId)
  }).then(() => {
    ElMessage.success('删除成功')
    loadList()
  }).catch(() => {})
}

loadList()
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}
</style>
