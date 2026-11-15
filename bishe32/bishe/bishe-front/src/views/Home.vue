<template>
  <div class="home-page-premium">
    <section class="dashboard-grid-premium">
      <!-- Premium Consult Card -->
      <div class="card-modern-premium consult-card-premium">
        <div class="card-header-premium">
          <div class="header-icon-box">
            <el-icon><ChatLineRound /></el-icon>
          </div>
          <div class="header-text-box">
            <div class="card-title-premium">发起咨询</div>
            <div class="card-subtitle-premium">专业的农技专家在线为您解答</div>
          </div>
        </div>
        
        <el-form label-position="top" class="consult-form-premium">
          <el-form-item label="相关订单（订单号或订单ID）">
            <el-input v-model="consultForm.orderRef" placeholder="请输入订单号（如 AG177...）或订单ID" prefix-icon="Ticket" />
          </el-form-item>
          <el-form-item label="咨询主题">
            <el-input v-model="consultForm.title" placeholder="例如：物流时效、农产品存储等" prefix-icon="EditPen" />
          </el-form-item>
          <el-form-item label="详细描述">
            <el-input 
              v-model="consultForm.content" 
              type="textarea" 
              :rows="4" 
              placeholder="请详细描述您遇到的问题或诉求..." 
              resize="none"
            />
          </el-form-item>
        </el-form>
        
        <el-button type="primary" class="submit-btn-premium" @click="submitConsult">
          <span>提交咨询申请</span>
          <el-icon class="el-icon--right"><Promotion /></el-icon>
        </el-button>
      </div>

      <!-- Premium Records Card -->
      <div class="card-modern-premium record-card-premium">
        <div class="card-header-premium">
          <div class="header-icon-box history">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="header-text-box">
            <div class="card-title-premium">咨询记录</div>
            <div class="card-subtitle-premium">最近 {{ consults.length }} 条处理动态</div>
          </div>
        </div>

        <div v-if="consults.length" class="history-list-premium">
          <div v-for="item in consults" :key="item.disputeId" class="history-item-premium">
            <div class="item-main-row">
              <span class="order-no-badge">#{{ item.orderNo }}</span>
              <span class="time-stamp">{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="item-status-row">
              <el-tag size="small" :type="consultStatusType(item.status)" effect="dark" round>
                {{ consultStatusText(item.status) }}
              </el-tag>
            </div>
            <div class="item-detail-row">
              <span class="farmer-name">{{ item.title || '订单咨询' }}</span>
            </div>
            <div class="reply-line">问题：{{ plainText(item.content) || '-' }}</div>
            <div class="reply-line" v-if="item.handleResult">回复：{{ plainText(item.handleResult) }}</div>
            <div class="reply-line pending" v-else>回复：管理员暂未回复</div>
          </div>
        </div>
        <el-empty v-else description="暂无历史咨询记录" />
      </div>
    </section>

    <section class="section-modern-premium card-modern-premium">
      <div class="section-header-premium">
        <div>
          <h2 class="section-title-premium">今日优选</h2>
          <p class="section-subtitle-premium">甄选当季新鲜农家特产，顺丰直达</p>
        </div>
        <el-button class="view-all-btn" @click="goProducts">
          查看全部商品
        </el-button>
      </div>

      <div class="category-row-premium">
        <el-button
          v-for="opt in categoryOptions"
          :key="opt.value"
          class="category-pill"
          :type="categoryId === opt.value ? 'primary' : 'default'"
          @click="categoryId = opt.value; loadProducts()"
        >
          {{ opt.text }}
        </el-button>
      </div>

      <div v-if="products.length" class="product-grid-premium">
        <article v-for="item in products" :key="item.productId" class="product-card-premium">
          <div class="img-wrapper-premium">
            <img :src="imageUrl(item.productCover)" alt="cover" @click="goDetail(item.productId)" />
          </div>
          <div class="product-info-premium">
            <div class="p-name-premium" @click="goDetail(item.productId)">{{ item.productName }}</div>
            <div class="p-farm-premium">农场：{{ item.farmName || '未命名农场' }}</div>
            <div class="p-farmer-premium">农户：{{ item.farmerName || '-' }}</div>
            <div class="p-farm-address-premium" v-if="item.farmAddress">地址：{{ item.farmAddress }}</div>
            <div class="p-price-row-premium">
              <div class="p-price-box">
                <div class="p-price-val">￥{{ Number(item.price || 0).toFixed(2) }}</div>
                <div v-if="Number(item.promotionDiscount || 0) > 0" class="p-origin-price">
                  原价￥{{ Number(item.originPrice || 0).toFixed(2) }}
                </div>
              </div>
              <el-input-number 
                v-model="item.buyQty" 
                size="small" 
                class="qty-stepper-premium"
                :min="1" 
                :max="Math.max(1, Number(item.stock || 1))" 
              />
            </div>
            <div v-if="item.promotionName" class="p-promotion-tag">
              <span>{{ item.promotionName }}</span>
              <span v-if="Number(item.promotionDiscount || 0) > 0"> 已优惠￥{{ Number(item.promotionDiscount || 0).toFixed(2) }}</span>
              <span v-else> 进行中</span>
            </div>
            <el-button
              type="primary"
              class="add-cart-btn-premium"
              :disabled="Number(item.stock || 0) <= 0"
              @click="add(item)"
            >
              {{ Number(item.stock || 0) > 0 ? '加入购物车' : '暂时缺货' }}
            </el-button>
          </div>
        </article>
      </div>
      <el-empty v-else description="暂无可展示商品" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChatLineRound, DataLine, EditPen, Promotion, Ticket } from '@element-plus/icons-vue'
import { showFailToast, showSuccessToast } from '@/utils/notify'
import { addCart, createDispute, listCategories, listConsults, listOrders, listProducts } from '@/api/mall'
import { useMallStore } from '@/store/mall'
import { resolveMediaUrl } from '@/utils/media'

const router = useRouter()
const mallStore = useMallStore()
const orders = ref([])
const consults = ref([])
const products = ref([])
const categoryList = ref([])
const categoryId = ref(0)
const consultForm = reactive({ orderRef: '', title: '', content: '' })

const categoryOptions = computed(() => {
  const options = [{ text: '全部分类', value: 0 }]
  categoryList.value.forEach(i => options.push({ text: i.categoryName, value: i.categoryId }))
  return options
})

const PAY_STATUS = {
  '0': '待支付',
  '1': '已支付',
  '2': '已退款'
}

const ORDER_STATUS = {
  '0': '待接单',
  '1': '待发货',
  '2': '运输中',
  '3': '待收货',
  '4': '已完成',
  '5': '已取消',
  '6': '退款处理中',
  '7': '已退款'
}

function payStatusText(val) {
  return PAY_STATUS[String(val)] || `状态${val}`
}

function orderStatusText(val) {
  return ORDER_STATUS[String(val)] || `状态${val}`
}

function consultStatusText(val) {
  const code = String(val)
  if (code === '0') return '待处理'
  if (code === '1') return '处理中'
  if (code === '2') return '已回复'
  return '未知状态'
}

function consultStatusType(val) {
  const code = String(val)
  if (code === '0') return 'warning'
  if (code === '1') return 'primary'
  if (code === '2') return 'success'
  return 'info'
}

function formatTime(time) {
  if (!time) return '-'
  return String(time).replace('T', ' ').slice(0, 19)
}

function plainText(text) {
  if (!text) return ''
  return String(text)
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function loadOrders() {
  return listOrders({ pageNum: 1, pageSize: 6 }).then(res => {
    orders.value = res.rows || []
  })
}

function loadConsults() {
  return listConsults({ pageNum: 1, pageSize: 10 }).then(res => {
    consults.value = res.rows || []
  })
}

function loadCategories() {
  listCategories().then(res => {
    categoryList.value = res.data || []
  })
}

function loadProducts() {
  const params = { pageNum: 1, pageSize: 6 }
  if (categoryId.value) params.categoryId = categoryId.value
  listProducts(params).then(res => {
    products.value = (res.rows || []).map(i => ({ ...i, buyQty: 1 }))
  })
}

function imageUrl(path) {
  return resolveMediaUrl(path)
}

function add(item) {
  addCart({ productId: item.productId, quantity: item.buyQty || 1 }).then(async () => {
    await mallStore.refreshCartStat()
    showSuccessToast('加入购物车成功')
  }).catch(err => showFailToast(err.message || '加入失败'))
}

function localizeConsultError(err) {
  const message = String(err?.message || '').trim()
  if (!message) return '提交失败'
  if (message.includes('Order not found') || message.includes('For input string')) {
    return '未找到订单，请确认订单号或订单ID是否正确'
  }
  return message
}

async function resolveOrderId(orderRef) {
  const text = String(orderRef || '').trim()
  if (!text) return null
  if (/^\d+$/.test(text)) {
    return Number(text)
  }
  const normalized = text.replace(/^#/, '').toUpperCase()

  const localHit = (orders.value || []).find(item => String(item.orderNo || '').toUpperCase() === normalized)
  if (localHit?.orderId) {
    return Number(localHit.orderId)
  }

  let pageNum = 1
  const pageSize = 100
  while (pageNum <= 20) {
    const res = await listOrders({ pageNum, pageSize })
    const rows = res?.rows || []
    const hit = rows.find(item => String(item.orderNo || '').toUpperCase() === normalized)
    if (hit?.orderId) {
      return Number(hit.orderId)
    }
    if (rows.length < pageSize) {
      break
    }
    pageNum += 1
  }
  return null
}

async function submitConsult() {
  if (!consultForm.orderRef) {
    showFailToast('请填写订单号或订单ID')
    return
  }
  if (!consultForm.title || !consultForm.content) {
    showFailToast('请填写咨询主题和详细描述')
    return
  }
  try {
    const orderId = await resolveOrderId(consultForm.orderRef)
    if (!orderId) {
      showFailToast('未找到订单，请确认订单号或订单ID是否正确')
      return
    }
    await createDispute({
      orderId,
      title: consultForm.title,
      content: consultForm.content
    })
    showSuccessToast('咨询申请已提交')
    consultForm.orderRef = ''
    consultForm.title = ''
    consultForm.content = ''
    await Promise.all([loadOrders(), loadConsults()])
  } catch (err) {
    showFailToast(localizeConsultError(err))
  }
}

function goProducts() {
  router.push('/products')
}

function goDetail(productId) {
  router.push(`/products/${productId}`)
}

onMounted(() => {
  loadOrders()
  loadConsults()
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.home-page-premium {
  padding: 32px 0;
  max-width: 1240px;
  margin: 0 auto;
}

.dashboard-grid-premium {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 40px;
}

.card-modern-premium {
  background: #ffffff;
  border-radius: 28px;
  border: 1px solid rgba(241, 245, 249, 0.8);
  padding: 32px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card-header-premium {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.header-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: #eff6ff;
  color: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.header-icon-box.history {
  background: #fef2f2;
  color: #ef4444;
}

.card-title-premium {
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.card-subtitle-premium {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

.consult-form-premium {
  margin-bottom: 24px;
}

.consult-form-premium :deep(.el-form-item__label) {
  font-weight: 800;
  color: #334155;
  padding-bottom: 8px;
}

.submit-btn-premium {
  width: 100%;
  height: 52px !important;
  border-radius: 14px !important;
  font-weight: 900 !important;
  font-size: 15px !important;
  box-shadow: 0 10px 20px rgba(59, 130, 246, 0.2) !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* History List Styles */
.history-list-premium {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 440px;
  overflow-y: auto;
  padding-right: 8px;
}

.history-list-premium::-webkit-scrollbar {
  width: 4px;
}

.history-list-premium::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

.history-item-premium {
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 20px;
  padding: 20px;
  transition: all 0.25s;
}

.history-item-premium:hover {
  background: #ffffff;
  border-color: #3b82f6;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.item-main-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no-badge {
  font-family: monospace;
  font-weight: 800;
  color: #1e293b;
  font-size: 14px;
}

.time-stamp {
  font-size: 12px;
  color: #94a3b8;
}

.item-status-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.item-detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}

.farmer-name {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

.reply-line {
  margin-top: 8px;
  color: #475569;
  font-size: 13px;
  line-height: 1.5;
}

.reply-line.pending {
  color: #f59e0b;
}

.price-val-small {
  font-size: 16px;
  font-weight: 900;
  color: #0f172a;
}

/* Section Products */
.section-modern-premium {
  padding: 40px;
}

.section-header-premium {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.section-title-premium {
  font-size: 32px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -1px;
}

.section-subtitle-premium {
  color: #64748b;
  margin-top: 6px;
  font-size: 15px;
}

.view-all-btn {
  height: 40px !important;
  border-radius: 12px !important;
  font-weight: 700 !important;
}

.category-row-premium {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 32px;
}

.category-pill {
  height: 36px !important;
  padding: 0 20px !important;
  font-weight: 700 !important;
  border-radius: 99px !important;
}

.product-grid-premium {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.product-card-premium {
  background: #fff;
  border-radius: 24px;
  border: 1px solid #f1f5f9;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card-premium:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  border-color: rgba(59, 130, 246, 0.3);
}

.img-wrapper-premium {
  height: 200px;
  overflow: hidden;
  position: relative;
}

.img-wrapper-premium img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.product-card-premium:hover .img-wrapper-premium img {
  transform: scale(1.1);
}

.product-info-premium {
  padding: 24px;
}

.p-name-premium {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
  cursor: pointer;
}

.p-farm-premium {
  font-size: 13px;
  color: #475569;
  margin-bottom: 4px;
  font-weight: 600;
}

.p-farmer-premium {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.p-farm-address-premium {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 16px;
}

.p-price-row-premium {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.p-price-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.p-price-val {
  font-size: 26px;
  font-weight: 900;
  color: #ef4444;
}

.p-origin-price {
  font-size: 12px;
  color: #94a3b8;
  text-decoration: line-through;
}

.p-promotion-tag {
  margin-bottom: 14px;
  font-size: 12px;
  color: #dc2626;
  font-weight: 700;
}

.qty-stepper-premium :deep(.el-input-number__decrease),
.qty-stepper-premium :deep(.el-input-number__increase) {
  border: none;
  background: #f1f5f9;
}

.add-cart-btn-premium {
  width: 100%;
  height: 48px !important;
  border-radius: 12px !important;
  font-weight: 800 !important;
  letter-spacing: 0.5px;
}

@media (max-width: 1024px) {
  .dashboard-grid-premium { grid-template-columns: 1fr; }
  .home-page-premium { padding: 24px 16px; }
}
</style>
