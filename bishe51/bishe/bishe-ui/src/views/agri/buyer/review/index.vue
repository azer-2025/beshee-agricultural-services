<template>
  <div class="app-container">
    <el-card shadow="never" class="mb16">
      <div class="top-bar">
        <div class="title-wrap">
          <span class="title">评价互动中心</span>
          <el-tag type="warning">待评价 {{ pendingList.length }}</el-tag>
          <el-tag type="success">已评价 {{ reviewedList.length }}</el-tag>
        </div>
        <div class="actions">
          <el-button v-if="focusOrderId" @click="clearFocus">清除订单筛选</el-button>
          <el-button @click="goOrders">返回订单页</el-button>
        </div>
      </div>
      <div class="hint" v-if="focusOrderId">当前仅展示订单 {{ focusOrderId }} 的待评价商品</div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>待评价商品</template>
          <el-table :data="pendingList" border>
            <el-table-column prop="orderNo" label="订单号" width="170" />
            <el-table-column prop="productName" label="商品" />
            <el-table-column label="完成时间" width="170">
              <template #default="scope">{{ formatDateTime(scope.row.finishTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  v-hasPermi="['agri:buyer:review:add']"
                  @click="openReview(scope.row)"
                >评价</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pendingList.length === 0" description="暂无待评价商品" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header>已评价记录</template>
          <el-timeline v-if="reviewedList.length">
            <el-timeline-item v-for="item in reviewedList" :key="item.reviewId" :timestamp="formatDateTime(item.createTime)">
              <div>{{ item.productName }}（质量{{ item.qualityStar }} / 包装{{ item.packageStar }} / 服务{{ item.serviceStar }}）</div>
              <div class="content">{{ item.content || '无文字评价' }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无评价记录" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="reviewOpen" title="提交评价" width="520px">
      <el-form :model="reviewForm" label-width="85px">
        <el-form-item label="质量评分"><el-rate v-model="reviewForm.qualityStar" /></el-form-item>
        <el-form-item label="包装评分"><el-rate v-model="reviewForm.packageStar" /></el-form-item>
        <el-form-item label="服务评分"><el-rate v-model="reviewForm.serviceStar" /></el-form-item>
        <el-form-item label="文字评价">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewOpen = false">取消</el-button>
        <el-button type="primary" v-hasPermi="['agri:buyer:review:add']" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { addBuyerReview, listBuyerPendingReviews, listBuyerReviews } from '@/api/agri/mall'

const route = useRoute()
const router = useRouter()
const pendingRaw = ref([])
const reviewedList = ref([])
const reviewOpen = ref(false)
const reviewForm = reactive({ itemId: null, qualityStar: 5, packageStar: 5, serviceStar: 5, content: '' })
const focusOrderId = computed(() => Number(route.query.orderId || 0) || 0)

const pendingList = computed(() => {
  if (!focusOrderId.value) return pendingRaw.value
  return pendingRaw.value.filter(item => Number(item.orderId) === focusOrderId.value)
})

function formatDateTime(value) {
  return parseTime(value) || '-'
}

function loadData() {
  listBuyerPendingReviews().then(res => {
    pendingRaw.value = res.data || []
  }).catch(err => {
    ElMessage.error(err.message || '待评价数据加载失败')
  })
  listBuyerReviews().then(res => {
    reviewedList.value = res.data || []
  }).catch(err => {
    ElMessage.error(err.message || '评价记录加载失败')
  })
}

function openReview(row) {
  reviewForm.itemId = row.itemId
  reviewForm.qualityStar = 5
  reviewForm.packageStar = 5
  reviewForm.serviceStar = 5
  reviewForm.content = ''
  reviewOpen.value = true
}

function submitReview() {
  if (!reviewForm.itemId) {
    ElMessage.warning('请选择待评价商品')
    return
  }
  addBuyerReview(reviewForm).then(() => {
    ElMessage.success('评价提交成功')
    reviewOpen.value = false
    loadData()
  }).catch(err => {
    ElMessage.error(err.message || '评价提交失败')
  })
}

function clearFocus() {
  router.replace('/buyer/review')
}

function goOrders() {
  router.push('/buyer/order')
}

onMounted(loadData)
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.top-bar { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.title-wrap { display: flex; align-items: center; gap: 8px; }
.title { font-size: 18px; font-weight: 700; color: #303133; }
.actions { display: flex; gap: 8px; }
.hint { margin-top: 8px; color: #909399; font-size: 12px; }
.content { margin-top: 6px; color: #666; }
</style>
