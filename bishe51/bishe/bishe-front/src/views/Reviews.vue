<template>
  <div class="reviews-page">
    <section class="card-modern topbar">
      <el-segmented v-model="activeTab" :options="tabOptions" />
      <div class="focus" v-if="focusOrderId">当前仅显示订单 #{{ focusOrderId }} 的待评价商品</div>
    </section>

    <section v-if="activeTab === 'pending'" class="list">
      <div v-if="pendingList.length" class="cards">
        <article v-for="item in pendingList" :key="item.itemId" class="card-modern review-card">
          <img :src="imageUrl(item.productCover)" class="cover" alt="cover" />
          <div class="info">
            <h3>{{ item.productName }}</h3>
            <p>订单号：{{ item.orderNo }}，完成时间：{{ formatTime(item.finishTime) }}</p>
            <el-button type="primary" @click="openReview(item)">立即评价</el-button>
          </div>
        </article>
      </div>
      <el-empty v-else description="暂无待评价商品" />
    </section>

    <section v-else class="list">
      <div v-if="reviewedList.length" class="cards done">
        <article v-for="item in reviewedList" :key="item.reviewId" class="card-modern done-card">
          <div class="head">
            <img :src="imageUrl(item.productCover)" class="mini" alt="cover" />
            <div>
              <h3>{{ item.productName }}</h3>
              <p>{{ formatTime(item.createTime) }}</p>
            </div>
          </div>
          <div class="rates">
            <div>质量 <el-rate :model-value="item.qualityStar" disabled /></div>
            <div>包装 <el-rate :model-value="item.packageStar" disabled /></div>
            <div>服务 <el-rate :model-value="item.serviceStar" disabled /></div>
          </div>
          <div class="content">{{ item.content || '用户未填写文字评价' }}</div>
        </article>
      </div>
      <el-empty v-else description="暂无评价记录" />
    </section>

    <el-dialog v-model="reviewOpen" title="提交评价" width="640px">
      <el-form label-width="90px">
        <el-form-item label="商品质量">
          <el-rate v-model="reviewForm.qualityStar" />
        </el-form-item>
        <el-form-item label="包装物流">
          <el-rate v-model="reviewForm.packageStar" />
        </el-form-item>
        <el-form-item label="服务态度">
          <el-rate v-model="reviewForm.serviceStar" />
        </el-form-item>
        <el-form-item label="文字评价">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewOpen = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { addReview, listPendingReviews, listReviews } from '@/api/mall'
import { useMallStore } from '@/store/mall'
import { resolveMediaUrl } from '@/utils/media'
import { showFailToast, showSuccessToast } from '@/utils/notify'

const route = useRoute()
const mallStore = useMallStore()

const activeTab = ref('pending')
const pendingRaw = ref([])
const reviewedList = ref([])
const reviewOpen = ref(false)

const reviewForm = reactive({
  itemId: null,
  qualityStar: 5,
  packageStar: 5,
  serviceStar: 5,
  content: ''
})

const tabOptions = [
  { label: '待评价', value: 'pending' },
  { label: '已评价', value: 'done' }
]

const focusOrderId = computed(() => Number(route.query.orderId || 0) || 0)
const pendingList = computed(() => {
  if (!focusOrderId.value) return pendingRaw.value
  return pendingRaw.value.filter(item => Number(item.orderId) === focusOrderId.value)
})

function formatTime(time) {
  if (!time) return '-'
  return String(time).replace('T', ' ').slice(0, 19)
}

function imageUrl(path) {
  return resolveMediaUrl(path)
}

async function loadData() {
  try {
    const [pendingRes, reviewedRes] = await Promise.all([listPendingReviews(), listReviews()])
    pendingRaw.value = pendingRes.data || []
    reviewedList.value = reviewedRes.data || []
    await mallStore.refreshReviewStat()
  } catch (e) {
    showFailToast(e.message || '加载评价数据失败')
  }
}

function openReview(row) {
  reviewForm.itemId = row.itemId
  reviewForm.qualityStar = 5
  reviewForm.packageStar = 5
  reviewForm.serviceStar = 5
  reviewForm.content = ''
  reviewOpen.value = true
}

async function submitReview() {
  if (!reviewForm.itemId) {
    showFailToast('请选择待评价商品')
    return
  }
  if (reviewForm.content && reviewForm.content.length > 200) {
    showFailToast('文字评价最多 200 字')
    return
  }
  try {
    await addReview(reviewForm)
    showSuccessToast('评价提交成功')
    reviewOpen.value = false
    await loadData()
  } catch (e) {
    showFailToast(e.message || '评价提交失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.reviews-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 24px 0;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.focus {
  color: #0891b2;
  font-size: 13px;
  font-weight: 700;
}

.cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.review-card {
  display: flex;
  gap: 12px;
  align-items: center;
}

.cover {
  width: 96px;
  height: 96px;
  border-radius: 10px;
  object-fit: cover;
}

.info h3 {
  margin: 0 0 6px;
}

.info p {
  margin: 0 0 10px;
  color: #64748b;
  font-size: 13px;
}

.done-card .head {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 8px;
}

.done-card .mini {
  width: 54px;
  height: 54px;
  border-radius: 8px;
  object-fit: cover;
}

.done-card h3 {
  margin: 0;
}

.done-card p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.rates {
  display: grid;
  gap: 6px;
  margin-bottom: 8px;
}

.content {
  color: #334155;
  line-height: 1.7;
}
</style>
