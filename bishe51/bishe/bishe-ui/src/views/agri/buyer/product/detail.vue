<template>
  <div class="app-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="header-row">
          <div class="title">商品详情</div>
          <div class="header-actions">
            <el-button @click="goBack">返回商品列表</el-button>
            <el-button type="success" @click="goCart">去购物车</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading && !detail" description="商品不存在或已下架" />

      <template v-else-if="detail">
        <el-row :gutter="18">
          <el-col :span="13">
            <el-carousel height="360px" indicator-position="outside">
              <el-carousel-item v-for="(img, idx) in detailImages" :key="idx">
                <el-image :src="imageUrl(img)" fit="cover" class="detail-image" :preview-src-list="previewImages" />
              </el-carousel-item>
            </el-carousel>
          </el-col>
          <el-col :span="11">
            <div class="name">{{ detail.productName || '-' }}</div>
            <div class="price">￥{{ Number(detail.price || 0).toFixed(2) }}</div>
            <div class="meta">分类：{{ detail.categoryName || '-' }}</div>
            <div class="meta">库存：{{ detail.stock || 0 }}，销量：{{ detail.salesCount || 0 }}</div>
            <div class="meta">评分：{{ Number(detail.avgScore || 0).toFixed(1) }} / 5</div>
            <div class="meta">农户：{{ detail.farmerName || '-' }} / {{ detail.farmerPhone || '-' }}</div>
            <div class="desc">{{ detail.productDesc || '暂无图文描述' }}</div>
            <div class="actions">
              <el-input-number
                v-model="detail.buyQty"
                :min="1"
                :max="Math.max(1, Number(detail.stock || 1))"
                :disabled="Number(detail.stock || 0) <= 0"
              />
              <el-button
                type="success"
                :disabled="Number(detail.stock || 0) <= 0"
                @click="addCart"
              >
                加入购物车
              </el-button>
            </div>
          </el-col>
        </el-row>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addBuyerCart, getBuyerProduct } from '@/api/agri/mall'
import { resolveMediaUrl } from '@/utils/media'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)
const detailImages = ref([])
const previewImages = computed(() => detailImages.value.map(item => imageUrl(item)))

function loadDetail() {
  const productId = Number(route.params.productId)
  if (!productId) {
    detail.value = null
    return
  }
  loading.value = true
  getBuyerProduct(productId).then(res => {
    const data = res.data || null
    detail.value = data ? { ...data, buyQty: 1 } : null
    const imgs = String(data?.productImages || '').split(',').filter(Boolean)
    detailImages.value = imgs.length ? imgs : [data?.productCover].filter(Boolean)
  }).catch(err => {
    ElMessage.error(err.message || '详情加载失败')
    detail.value = null
  }).finally(() => {
    loading.value = false
  })
}

function addCart() {
  if (!detail.value) return
  if (Number(detail.value.stock || 0) <= 0) {
    ElMessage.warning('当前商品库存不足')
    return
  }
  addBuyerCart({ productId: detail.value.productId, quantity: detail.value.buyQty || 1 }).then(() => {
    ElMessage.success('已加入购物车')
  }).catch(err => {
    ElMessage.error(err.message || '加入购物车失败')
  })
}

function goBack() {
  router.push('/buyer/product')
}

function goCart() {
  router.push('/buyer/cart')
}

function imageUrl(path) {
  return resolveMediaUrl(path)
}

onMounted(loadDetail)
</script>

<style scoped>
.header-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.title { font-size: 18px; font-weight: 700; }
.header-actions { display: flex; gap: 8px; }
.detail-image { width: 100%; height: 360px; border-radius: 10px; }
.name { font-size: 22px; line-height: 1.4; font-weight: 700; color: #303133; }
.price { margin-top: 10px; font-size: 28px; font-weight: 700; color: #f56c6c; }
.meta { margin-top: 8px; color: #606266; font-size: 14px; }
.desc {
  margin-top: 14px;
  padding: 12px;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
  line-height: 1.7;
  color: #606266;
}
.actions { margin-top: 16px; display: flex; align-items: center; gap: 10px; }
</style>
