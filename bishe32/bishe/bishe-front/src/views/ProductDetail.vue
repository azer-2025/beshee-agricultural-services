<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="detail" class="detail-shell card-modern">
      <section class="gallery">
        <el-carousel class="gallery-swipe" :interval="5000" height="560px" indicator-position="outside">
          <el-carousel-item v-for="(img, idx) in detailImages" :key="idx">
            <img :src="imageUrl(img)" class="gallery-image" alt="product" />
          </el-carousel-item>
        </el-carousel>
        <el-button class="back-btn" circle @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </section>

      <section class="info-panel">
        <div class="crumb">商品浏览 / {{ detail.categoryName || '农产品' }}</div>
        <h1 class="title">{{ detail.productName || '-' }}</h1>

        <div class="meta-line">
          <span class="meta-item"><el-icon><Lightning /></el-icon> 已售 {{ detail.salesCount || 0 }}</span>
          <span class="meta-item"><el-icon><Star /></el-icon> 评分 {{ Number(detail.avgScore || 0).toFixed(1) }}</span>
          <span class="meta-item" :class="{ empty: Number(detail.stock || 0) <= 0 }">
            <el-icon><Goods /></el-icon> {{ Number(detail.stock || 0) > 0 ? `库存 ${detail.stock}` : '已售罄' }}
          </span>
        </div>

        <div class="price-box">
          <span class="symbol">￥</span>
          <span class="num">{{ Number(detail.price || 0).toFixed(2) }}</span>
        </div>
        <div v-if="detail.promotionName" class="promo-row">
          <span v-if="Number(detail.promotionDiscount || 0) > 0" class="origin">原价￥{{ Number(detail.originPrice || 0).toFixed(2) }}</span>
          <el-tag size="small" type="danger">
            {{ detail.promotionName }}<span v-if="Number(detail.promotionDiscount || 0) > 0"> 省￥{{ Number(detail.promotionDiscount || 0).toFixed(2) }}</span><span v-else> 进行中</span>
          </el-tag>
        </div>

        <div class="farmer-box">
          <div class="farmer-name"><el-icon><User /></el-icon> 农户账号：{{ detail.farmerName || '认证农户' }}</div>
          <div class="farm-name"><el-icon><Shop /></el-icon> 农场：{{ detail.farmName || '未命名农场' }}</div>
          <div class="farm-address" v-if="detail.farmAddress"><el-icon><Location /></el-icon> 地址：{{ detail.farmAddress }}</div>
          <div class="farmer-phone"><el-icon><Phone /></el-icon> {{ detail.farmerPhone || '联系方式保密' }}</div>
        </div>

        <div class="desc-box">
          <h3>商品描述</h3>
          <p>{{ detail.productDesc || '暂无商品描述' }}</p>
        </div>

        <div class="action-row">
          <div class="qty">
            <span>数量</span>
            <el-input-number
              v-model="detail.buyQty"
              :min="1"
              :max="Math.max(1, Number(detail.stock || 1))"
              :disabled="Number(detail.stock || 0) <= 0"
            />
          </div>
          <el-button class="go-cart" @click="goCart">
            <el-icon><ShoppingCart /></el-icon> 购物车
          </el-button>
          <el-button
            class="add-btn"
            type="primary"
            :disabled="Number(detail.stock || 0) <= 0"
            @click="add"
          >
            {{ Number(detail.stock || 0) > 0 ? '加入购物车' : '暂时缺货' }}
          </el-button>
        </div>
      </section>
    </div>

    <el-empty v-else description="商品不存在或已下架" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Lightning, Star, Goods, User, Phone, ShoppingCart, Shop, Location } from '@element-plus/icons-vue'
import { addCart, getProduct } from '@/api/mall'
import { useMallStore } from '@/store/mall'
import { resolveMediaUrl } from '@/utils/media'
import { showFailToast, showSuccessToast } from '@/utils/notify'

const route = useRoute()
const router = useRouter()
const mallStore = useMallStore()

const loading = ref(false)
const detail = ref(null)
const detailImages = ref([])

function imageUrl(path) {
  return resolveMediaUrl(path)
}

async function loadDetail() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    const res = await getProduct(id)
    const data = res.data
    detail.value = data ? { ...data, buyQty: 1 } : null
    const images = String(data?.productImages || '').split(',').filter(Boolean)
    detailImages.value = images.length ? images : [data?.productCover].filter(Boolean)
  } catch (e) {
    showFailToast(e.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

async function add() {
  if (!detail.value || Number(detail.value.stock || 0) <= 0) return
  try {
    await addCart({ productId: detail.value.productId, quantity: detail.value.buyQty || 1 })
    await mallStore.refreshCartStat()
    showSuccessToast('已加入购物车')
  } catch (e) {
    showFailToast(e.message || '加入购物车失败')
  }
}

function goCart() {
  router.push('/cart')
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  padding: 24px 0;
}

.detail-shell {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 24px;
  padding: 24px;
}

.gallery {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  min-height: 560px;
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.back-btn {
  position: absolute;
  top: 16px;
  left: 16px;
}

.info-panel {
  display: flex;
  flex-direction: column;
}

.crumb {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
}

.title {
  font-size: 34px;
  font-weight: 900;
  margin: 0 0 16px;
}

.meta-line {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 10px;
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 700;
  font-size: 12px;
}

.meta-item.empty {
  background: #fef2f2;
  color: #dc2626;
}

.price-box {
  margin: 12px 0 18px;
  color: #ef4444;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.symbol {
  font-size: 20px;
  font-weight: 700;
}

.num {
  font-size: 42px;
  font-weight: 900;
}

.promo-row {
  margin-top: -12px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.promo-row .origin {
  color: #94a3b8;
  text-decoration: line-through;
  font-size: 13px;
}

.farmer-box {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 16px;
  background: #f8fafc;
}

.farmer-name,
.farm-name,
.farm-address,
.farmer-phone {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: #334155;
}

.farm-name,
.farm-address,
.farmer-phone {
  margin-top: 8px;
  font-size: 14px;
  color: #64748b;
}

.desc-box {
  border-top: 1px dashed #e2e8f0;
  padding-top: 16px;
  margin-bottom: 18px;
}

.desc-box h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.desc-box p {
  margin: 0;
  line-height: 1.8;
  color: #475569;
}

.action-row {
  margin-top: auto;
  display: grid;
  grid-template-columns: auto auto 1fr;
  gap: 12px;
  align-items: center;
}

.qty {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  font-weight: 700;
}

.go-cart {
  height: 40px;
}

.add-btn {
  height: 40px;
}
</style>
