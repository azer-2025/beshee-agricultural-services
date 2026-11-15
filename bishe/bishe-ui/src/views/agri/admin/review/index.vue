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
      <div class="tip">已完成订单的评价由前台买家提交，此处仅查看待评价与已评价情况。</div>

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
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { listAdminPendingReviews, listAdminReviews } from '@/api/agri/mall'

const loading = ref(false)
const reviewTab = ref('pending')
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

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

loadList()
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}

.tip {
  margin-bottom: 12px;
  color: #909399;
  font-size: 13px;
}
</style>
