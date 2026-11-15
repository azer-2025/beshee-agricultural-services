<template>
  <div class="products-page">
    <section class="toolbar card-modern">
      <div class="toolbar-row">
        <div class="title-wrap">
          <h2>发现好物</h2>
          <p>精选农产品，支持按分类与排序快速筛选</p>
        </div>
        <div class="search-wrap">
          <el-input v-model="query.keyword" placeholder="搜索商品/农户" clearable @keyup.enter="loadProducts">
            <template #append>
              <el-button type="primary" @click="loadProducts">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <div class="filter-row">
        <div class="category-list">
          <el-button
            :type="activeCategory === '0' ? 'primary' : 'default'"
            round
            @click="onCategoryChange('0')"
          >
            全部
          </el-button>
          <el-button
            v-for="item in categoryList"
            :key="item.categoryId"
            :type="activeCategory === String(item.categoryId) ? 'primary' : 'default'"
            round
            @click="onCategoryChange(String(item.categoryId))"
          >
            {{ item.categoryName }}
          </el-button>
        </div>

        <div class="right-filters">
          <el-select v-model="query.sort" style="width: 160px" @change="loadProducts">
            <el-option v-for="item in sortOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="query.onlyStock" style="width: 140px" @change="loadProducts">
            <el-option label="全部商品" value="0" />
            <el-option label="仅看有货" value="1" />
          </el-select>
        </div>
      </div>
    </section>

    <section v-if="products.length" class="grid">
      <article v-for="item in products" :key="item.productId" class="product-card">
        <div class="cover" @click="openDetail(item.productId)">
          <img :src="imageUrl(item.productCover)" alt="cover" />
          <span class="badge" v-if="Number(item.stock || 0) <= 0">售罄</span>
        </div>

        <div class="content">
          <div class="meta">
            <span>{{ getCategoryName(item.categoryId) }}</span>
            <span>{{ item.farmName || '未命名农场' }}</span>
          </div>
          <div class="farmer-account">农户：{{ item.farmerName || '-' }}</div>
          <div class="farm-address" v-if="item.farmAddress">地址：{{ item.farmAddress }}</div>
          <h3 @click="openDetail(item.productId)">{{ item.productName }}</h3>
          <div class="price-row">
            <div class="price-box">
              <div class="price">￥{{ Number(item.price || 0).toFixed(2) }}</div>
              <div v-if="Number(item.promotionDiscount || 0) > 0" class="origin-price">
                原价￥{{ Number(item.originPrice || 0).toFixed(2) }}
              </div>
            </div>
            <el-input-number v-model="item.buyQty" :min="1" :max="Math.max(1, Number(item.stock || 1))" />
          </div>
          <div v-if="item.promotionName" class="promotion-tip">
            <span>{{ item.promotionName }}</span>
            <span v-if="Number(item.promotionDiscount || 0) > 0"> 已优惠￥{{ Number(item.promotionDiscount || 0).toFixed(2) }}</span>
            <span v-else> 进行中</span>
          </div>
          <el-button
            type="primary"
            class="add-btn"
            :disabled="Number(item.stock || 0) <= 0"
            @click="add(item)"
          >
            {{ Number(item.stock || 0) > 0 ? '加入购物车' : '暂时缺货' }}
          </el-button>
        </div>
      </article>
    </section>

    <el-empty v-else description="暂无匹配商品" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addCart, listCategories, listProducts } from '@/api/mall'
import { useMallStore } from '@/store/mall'
import { resolveMediaUrl } from '@/utils/media'
import { showFailToast, showSuccessToast } from '@/utils/notify'

const router = useRouter()
const mallStore = useMallStore()

const categoryList = ref([])
const activeCategory = ref('0')
const products = ref([])
const query = reactive({
  keyword: '',
  categoryId: '',
  sort: 'default',
  onlyStock: '0'
})

const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '价格从低到高', value: 'priceAsc' },
  { label: '价格从高到低', value: 'priceDesc' },
  { label: '销量优先', value: 'salesDesc' }
]

function imageUrl(path) {
  return resolveMediaUrl(path)
}

function getCategoryName(id) {
  const target = categoryList.value.find(c => c.categoryId === id)
  return target ? target.categoryName : '优选特产'
}

function onCategoryChange(val) {
  activeCategory.value = val
  query.categoryId = val === '0' ? '' : val
  loadProducts()
}

function openDetail(productId) {
  router.push(`/products/${productId}`)
}

async function add(item) {
  try {
    await addCart({ productId: item.productId, quantity: item.buyQty || 1 })
    await mallStore.refreshCartStat()
    showSuccessToast('已加入购物车')
  } catch (e) {
    showFailToast(e.message || '加入失败')
  }
}

async function loadCategories() {
  const res = await listCategories()
  categoryList.value = res.data || []
}

async function loadProducts() {
  const params = { pageNum: 1, pageSize: 24, keyword: query.keyword }
  if (query.categoryId) params.categoryId = query.categoryId
  if (query.onlyStock === '1') params.onlyStock = '1'
  if (query.sort && query.sort !== 'default') params.sort = query.sort

  try {
    let list = (await listProducts(params)).rows || []
    list = list.map(i => ({ ...i, buyQty: 1 }))

    if (query.sort === 'priceAsc') {
      list.sort((a, b) => Number(a.price || 0) - Number(b.price || 0))
    } else if (query.sort === 'priceDesc') {
      list.sort((a, b) => Number(b.price || 0) - Number(a.price || 0))
    } else if (query.sort === 'salesDesc') {
      list.sort((a, b) => Number(b.salesCount || 0) - Number(a.salesCount || 0))
    } else {
      list.sort((a, b) => Number(b.productId || 0) - Number(a.productId || 0))
    }

    if (query.onlyStock === '1') {
      list = list.filter(item => Number(item.stock || 0) > 0)
    }

    products.value = list
  } catch (e) {
    showFailToast(e.message || '加载商品失败')
  }
}

onMounted(async () => {
  await loadCategories()
  await loadProducts()
})
</script>

<style scoped>
.products-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
  padding: 24px 0;
}

.toolbar {
  padding: 24px;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.title-wrap h2 {
  margin: 0;
  font-size: 32px;
  font-weight: 900;
}

.title-wrap p {
  margin: 6px 0 0;
  color: #64748b;
}

.search-wrap {
  width: 480px;
}

.filter-row {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.category-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.right-filters {
  display: flex;
  gap: 10px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.product-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.cover {
  position: relative;
  height: 200px;
  cursor: pointer;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge {
  position: absolute;
  right: 10px;
  top: 10px;
  background: #dc2626;
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.content {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta {
  display: flex;
  justify-content: space-between;
  color: #64748b;
  font-size: 12px;
}

.farmer-account {
  color: #334155;
  font-size: 12px;
  font-weight: 600;
  margin-top: -2px;
}

.farm-address {
  color: #94a3b8;
  font-size: 12px;
  margin-top: -4px;
  line-height: 1.4;
}

.content h3 {
  margin: 0;
  font-size: 16px;
  line-height: 1.4;
  cursor: pointer;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price {
  font-size: 24px;
  font-weight: 900;
  color: #dc2626;
}

.origin-price {
  font-size: 12px;
  color: #94a3b8;
  text-decoration: line-through;
}

.promotion-tip {
  color: #dc2626;
  font-size: 12px;
  font-weight: 700;
  margin-top: -4px;
}

.add-btn {
  width: 100%;
}
</style>
