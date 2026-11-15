<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="5">
        <el-card shadow="never" class="category-card">
          <template #header>
            <span>分类</span>
          </template>
          <el-menu :default-active="String(query.categoryId || '')" @select="handleCategory">
            <el-menu-item index="">全部商品</el-menu-item>
            <el-menu-item v-for="item in categoryList" :key="item.categoryId" :index="String(item.categoryId)">
              {{ item.categoryName }}
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :span="19">
        <el-card shadow="never">
          <el-form :inline="true" :model="query">
            <el-form-item>
              <el-input v-model="query.keyword" placeholder="搜索商品" clearable @keyup.enter="loadProducts" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadProducts">查询</el-button>
            </el-form-item>
            <el-form-item>
              <el-select v-model="ui.sortKey" style="width: 160px">
                <el-option label="综合排序" value="default" />
                <el-option label="价格从低到高" value="priceAsc" />
                <el-option label="价格从高到低" value="priceDesc" />
                <el-option label="销量优先" value="salesDesc" />
                <el-option label="评分优先" value="scoreDesc" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-switch
                v-model="ui.onlyStock"
                inline-prompt
                active-text="仅看有货"
                inactive-text="全部"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="resetQuery">重置</el-button>
              <el-button type="success" @click="goCart">去购物车</el-button>
            </el-form-item>
          </el-form>

          <el-row v-loading="ui.loading" :gutter="16">
            <el-col :span="8" v-for="item in displayProducts" :key="item.productId" class="mb16">
              <el-card shadow="hover" class="product-item">
                <el-image
                  :src="imageUrl(item.productCover)"
                  :preview-src-list="[imageUrl(item.productCover)]"
                  fit="cover"
                  class="cover"
                  @click="openDetail(item.productId)"
                />
                <div class="title">{{ item.productName }}</div>
                <div class="meta">农户：{{ item.farmerName || '-' }}</div>
                <div class="meta">评分：{{ Number(item.avgScore || 0).toFixed(1) }} / 5</div>
                <div class="meta">销量：{{ item.salesCount || 0 }}，库存：{{ item.stock || 0 }}</div>
                <div class="price">￥{{ item.price }}</div>
                <div class="tags">
                  <el-tag size="small" type="success">在售</el-tag>
                  <el-tag v-if="Number(item.stock || 0) <= 0" size="small" type="danger">缺货</el-tag>
                </div>
                <div class="actions">
                  <el-button link type="primary" @click="openDetail(item.productId)">详情页</el-button>
                  <el-input-number
                    v-model="item.buyQty"
                    :min="1"
                    :max="Number(item.stock || 1)"
                    :disabled="Number(item.stock || 0) <= 0"
                    size="small"
                  />
                  <el-button
                    type="success"
                    size="small"
                    :disabled="Number(item.stock || 0) <= 0"
                    @click="addCart(item)"
                  >
                    加入购物车
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-empty v-if="!ui.loading && displayProducts.length === 0" description="暂无商品，换个条件试试" />

          <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="query.pageNum"
            v-model:limit="query.pageSize"
            @pagination="loadProducts"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addBuyerCart, listBuyerCategories, listBuyerProducts } from '@/api/agri/mall'
import { resolveMediaUrl } from '@/utils/media'

const router = useRouter()
const categoryList = ref([])
const productList = ref([])
const total = ref(0)
const ui = reactive({
  loading: false,
  onlyStock: false,
  sortKey: 'default'
})

const query = reactive({
  pageNum: 1,
  pageSize: 9,
  categoryId: '',
  keyword: ''
})

const displayProducts = computed(() => {
  let list = [...productList.value]
  if (ui.onlyStock) {
    list = list.filter(item => Number(item.stock || 0) > 0)
  }
  switch (ui.sortKey) {
    case 'priceAsc':
      list.sort((a, b) => Number(a.price || 0) - Number(b.price || 0))
      break
    case 'priceDesc':
      list.sort((a, b) => Number(b.price || 0) - Number(a.price || 0))
      break
    case 'salesDesc':
      list.sort((a, b) => Number(b.salesCount || 0) - Number(a.salesCount || 0))
      break
    case 'scoreDesc':
      list.sort((a, b) => Number(b.avgScore || 0) - Number(a.avgScore || 0))
      break
    default:
      list.sort((a, b) => Number(b.productId || 0) - Number(a.productId || 0))
      break
  }
  return list
})

function loadCategories() {
  listBuyerCategories().then(res => {
    categoryList.value = res.data || []
  }).catch(err => {
    ElMessage.error(err.message || '分类加载失败')
  })
}

function loadProducts() {
  ui.loading = true
  listBuyerProducts(query).then(res => {
    productList.value = (res.rows || []).map(item => ({ ...item, buyQty: 1 }))
    total.value = res.total || 0
  }).catch(err => {
    ElMessage.error(err.message || '商品加载失败')
  }).finally(() => {
    ui.loading = false
  })
}

function handleCategory(val) {
  query.categoryId = val
  query.pageNum = 1
  loadProducts()
}

function addCart(item) {
  if (Number(item.stock || 0) <= 0) {
    ElMessage.warning('当前商品库存不足')
    return
  }
  addBuyerCart({ productId: item.productId, quantity: item.buyQty || 1 }).then(() => {
    ElMessage.success('已加入购物车')
  }).catch(err => {
    ElMessage.error(err.message || '加入购物车失败')
  })
}

function openDetail(productId) {
  router.push('/buyer/product-detail/' + productId)
}

function resetQuery() {
  query.keyword = ''
  query.categoryId = ''
  query.pageNum = 1
  ui.onlyStock = false
  ui.sortKey = 'default'
  loadProducts()
}

function goCart() {
  router.push('/buyer/cart')
}

function imageUrl(path) {
  return resolveMediaUrl(path)
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.product-item .cover { width: 100%; height: 180px; border-radius: 8px; cursor: pointer; }
.product-item .title { margin-top: 10px; font-weight: 600; }
.product-item .meta { margin-top: 6px; color: #666; font-size: 13px; }
.product-item .price { margin-top: 8px; color: #f56c6c; font-size: 18px; font-weight: 700; }
.product-item .tags { margin-top: 8px; display: flex; gap: 8px; }
.product-item .actions { margin-top: 10px; display: flex; gap: 6px; align-items: center; }
</style>
