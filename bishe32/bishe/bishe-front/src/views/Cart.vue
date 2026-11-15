<template>
  <div class="cart-page">
    <section class="cart-head card-modern">
      <div>
        <h2>购物车管理</h2>
        <p>支持数量调整、批量删除和收货信息提交</p>
      </div>
      <div class="head-actions">
        <el-button text type="warning" :disabled="invalidCount === 0" @click="clearInvalid">
          清理失效商品
        </el-button>
        <el-button text type="danger" :disabled="checkedCount === 0" @click="removeChecked">
          删除已选
        </el-button>
      </div>
    </section>

    <section v-if="cartList.length" class="cart-layout">
      <div class="left-panel">
        <div class="cart-toolbar card-modern">
          <div class="tool-left">
            <el-checkbox :model-value="allChecked" @change="setAllChecked">
              全选有效商品
            </el-checkbox>
            <span class="selected-tip">已选 {{ checkedCount }} 种</span>
          </div>
          <div class="tool-right">
            <el-button
              v-for="opt in filterOptions"
              :key="opt.value"
              :type="filterType === opt.value ? 'primary' : 'default'"
              round
              @click="filterType = opt.value"
            >
              {{ opt.label }} {{ opt.count }}
            </el-button>
          </div>
        </div>

        <div v-if="visibleCartList.length" class="item-list">
          <article
            v-for="item in visibleCartList"
            :key="item.cartId"
            class="item-card"
            :class="{ invalid: item.invalidFlag === 1 }"
          >
            <div class="check-col">
              <el-checkbox
                :model-value="item.checked === '1'"
                :disabled="item.invalidFlag === 1"
                @change="(val) => setChecked(item, val)"
              />
            </div>

            <div class="cover-col">
              <img
                :src="imageUrl(item.productCover)"
                alt="cover"
                class="cover"
                @click="goDetail(item)"
              />
              <div v-if="item.invalidFlag === 1" class="invalid-mask">已失效</div>
            </div>

            <div class="info-col">
              <div class="name-row">
                <h3 @click="goDetail(item)">{{ item.productName || '商品已失效或下架' }}</h3>
                <el-tag v-if="item.invalidFlag !== 1" type="success" effect="light">优选</el-tag>
              </div>
              <div class="desc-row">
                <span v-if="item.invalidFlag === 1" class="invalid-text">该商品已下架或库存不足，请清理</span>
                <span v-else>库存 {{ item.stock || 0 }}</span>
              </div>
              <div class="price-row">单价 ￥{{ Number(item.price || 0).toFixed(2) }}</div>
            </div>

            <div class="qty-col">
              <el-input-number
                :model-value="Number(item.quantity || 1)"
                :min="1"
                :max="Math.max(1, Number(item.stock || 1))"
                :disabled="item.invalidFlag === 1"
                @change="(val) => changeQty(item, val)"
              />
              <div class="subtotal">小计 ￥{{ (Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2) }}</div>
            </div>

            <div class="act-col">
              <el-button circle :icon="Delete" @click="removeOne(item.cartId)" />
            </div>
          </article>
        </div>
        <el-empty v-else :description="emptyDesc" />
      </div>

      <aside class="summary card-modern">
        <h3>订单概览</h3>
        <div class="sum-row"><span>商品总数</span><b>{{ checkedItems.reduce((s, i) => s + Number(i.quantity), 0) }} 件</b></div>
        <div class="sum-row"><span>已选种类</span><b>{{ checkedCount }} 种</b></div>
        <div class="sum-row"><span>运费模板</span><b>{{ freightTemplateLabel }}</b></div>
        <div class="sum-row"><span>运费</span><b :class="Number(orderForm.freightAmount || 0) > 0 ? 'fee' : 'free'">￥{{ Number(orderForm.freightAmount || 0).toFixed(2) }}</b></div>
        <div class="sum-row"><span>优惠</span><b class="coupon">-￥{{ couponDiscount.toFixed(2) }}</b></div>
        <div class="sum-line"></div>
        <div class="sum-total">
          <span>应付合计</span>
          <strong>￥{{ payableAmount.toFixed(2) }}</strong>
        </div>

        <el-button type="primary" class="checkout-btn" :disabled="checkedCount === 0" @click="openSubmit">
          去结算订单 ({{ checkedCount }})
        </el-button>

        <div class="safe-list">
          <div><el-icon><CircleCheck /></el-icon> 农家直供</div>
          <div><el-icon><CircleCheck /></el-icon> 安全溯源</div>
        </div>
      </aside>
    </section>

    <section v-else class="empty-wrap card-modern">
      <el-empty description="您的购物车还是空的">
        <el-button type="primary" @click="$router.push('/products')">
          去逛逛
        </el-button>
      </el-empty>
    </section>

    <!-- Premium Submit Dialog -->
    <el-dialog 
      v-model="submitOpen" 
      width="800px" 
      :close-on-click-modal="false" 
      custom-class="premium-order-dialog"
    >
      <template #header>
        <div class="dialog-header-premium">
          <el-icon class="header-icon"><MapLocation /></el-icon>
          <div class="header-text">
            <h3>确认收货及订单信息</h3>
            <p>请核对您的配送地址与优惠选项</p>
          </div>
        </div>
      </template>

      <div class="dialog-body-premium">
        <!-- Quick Address Picker Overhaul -->
        <div v-if="savedAddresses.length" class="address-selector-premium">
          <div class="selector-title">
            <span>选择常用地址</span>
            <el-button link type="primary" @click="$router.push('/profile')">管理地址</el-button>
          </div>
          <div class="address-scroll-row">
            <div 
              v-for="addr in savedAddresses" 
              :key="addr.addressId"
              class="address-card-mini"
              :class="{ active: addr.receiverName === orderForm.receiverName && addr.receiverAddress === orderForm.receiverAddress }"
              @click="applyAddress(addr)"
            >
              <div class="card-top">
                <span class="name">{{ addr.receiverName }}</span>
                <el-tag v-if="addr.isDefault === '1'" size="small" effect="plain">默认</el-tag>
              </div>
              <div class="card-mid">{{ addr.receiverPhone }}</div>
              <div class="card-bot">{{ addr.receiverAddress }}</div>
              <div class="active-check"><el-icon><Check /></el-icon></div>
            </div>
          </div>
        </div>

        <el-form label-position="top" class="submit-form-premium">
          <div class="form-row">
            <el-form-item label="收货人姓名" class="flex-1">
              <el-input v-model="orderForm.receiverName" placeholder="姓名" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="联系电话" class="flex-1">
              <el-input v-model="orderForm.receiverPhone" placeholder="手机号码" prefix-icon="Phone" />
            </el-form-item>
          </div>

          <el-form-item label="省市区">
            <el-cascader
              v-model="selectedRegion"
              :options="regionOptions"
              :props="regionCascaderProps"
              clearable
              filterable
              class="full-width-select"
              placeholder="请选择省/市/区(县)"
              @change="onRegionChange"
            />
          </el-form-item>
          
          <el-form-item label="详细收货地址">
            <el-input 
              v-model="addressDetail"
              placeholder="请输入街道、楼牌号等详细地址"
              prefix-icon="Location"
              @blur="onAddressBlur"
            />
          </el-form-item>

          <el-form-item label="运费试算">
            <div class="freight-preview">
              <span>{{ freightTemplateLabel }}</span>
              <b>￥{{ Number(orderForm.freightAmount || 0).toFixed(2) }}</b>
            </div>
          </el-form-item>

          <el-form-item label="订单备注">
            <el-input 
              v-model="orderForm.buyerRemark" 
              type="textarea" 
              :rows="3" 
              placeholder="如有特殊配送要求，请在此处填写（可选）" 
              resize="none"
            />
          </el-form-item>

          <el-form-item label="可用优惠券">
            <el-select v-model="orderForm.userCouponId" placeholder="请选择优惠券" clearable class="full-width-select">
              <template #prefix>
                <el-icon><Ticket /></el-icon>
              </template>
              <el-option
                v-for="coupon in couponOptions"
                :key="coupon.userCouponId"
                :label="coupon.optionLabel"
                :value="coupon.userCouponId"
                :disabled="coupon.availableFlag !== '1'"
              >
                <div class="coupon-option-item">
                  <span class="c-name">{{ coupon.couponName }}</span>
                  <span class="c-val" v-if="coupon.availableFlag === '1'">预计省 ￥{{ Number(coupon.discountPreview).toFixed(2) }}</span>
                  <span class="c-err" v-else>不满足使用门槛</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer-premium">
          <div class="price-summary-box">
            <span class="lbl">实付款总计</span>
            <span class="unit">￥</span>
            <span class="val">{{ payableAmount.toFixed(2) }}</span>
          </div>
          <div class="footer-acts">
            <el-button @click="submitOpen = false" round>再看看</el-button>
            <el-button 
              type="primary" 
              class="btn-confirm-order"
              :loading="loading" 
              @click="confirmSubmit"
              round
            >
              提交订单
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, CircleCheck, MapLocation, Check, User, Phone, Location, Ticket } from '@element-plus/icons-vue'
import { showConfirmDialog, showFailToast, showSuccessToast } from '@/utils/notify'
import { areaList } from '@vant/area-data'
import {
  clearInvalidCart,
  listAddresses,
  listBuyerCoupons,
  listCart,
  quoteFreight,
  removeCart,
  submitOrder,
  updateCartChecked,
  updateCartCheckedAll,
  updateCartQty
} from '@/api/mall'
import { useMallStore } from '@/store/mall'
import { resolveMediaUrl } from '@/utils/media'

const router = useRouter()
const mallStore = useMallStore()

const cartList = ref([])
const filterType = ref('all')
const submitOpen = ref(false)
const loading = ref(false)
const quoteLoading = ref(false)
const orderForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  receiverProvinceCode: '',
  receiverCityCode: '',
  receiverDistrictCode: '',
  buyerRemark: '',
  userCouponId: null,
  freightAmount: 0,
  freightTemplateName: '',
  freightRegionName: '',
  freightRegionCode: '',
  paymentChannel: '2'
})
const buyerCoupons = ref([])
const savedAddresses = ref([])
const selectedRegion = ref([])
const addressDetail = ref('')
const regionOptions = buildRegionOptions(areaList)
const codeToText = {
  ...(areaList.province_list || {}),
  ...(areaList.city_list || {}),
  ...(areaList.county_list || {})
}
const regionCascaderProps = { value: 'value', label: 'label', children: 'children', emitPath: true }
const regionCandidates = buildRegionCandidates(regionOptions)

const validItems = computed(() => cartList.value.filter(i => i.invalidFlag !== 1))
const checkedItems = computed(() => validItems.value.filter(i => i.checked === '1'))
const checkedCount = computed(() => checkedItems.value.length)
const invalidCount = computed(() => cartList.value.filter(i => i.invalidFlag === 1).length)
const allChecked = computed(() => validItems.value.length > 0 && checkedCount.value === validItems.value.length)
const totalAmount = computed(() => checkedItems.value.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0))
const originPayAmount = computed(() => Number(totalAmount.value || 0) + Number(orderForm.freightAmount || 0))
const selectedCoupon = computed(() => buyerCoupons.value.find(item => String(item.userCouponId) === String(orderForm.userCouponId)) || null)
const freightTemplateLabel = computed(() => {
  if (quoteLoading.value) return '运费试算中...'
  if (!orderForm.receiverAddress) return '待选择收货地址'
  if (!orderForm.freightTemplateName) return '默认免运费'
  if (orderForm.freightRegionName && orderForm.freightRegionName !== '-') {
    return `${orderForm.freightTemplateName}（${orderForm.freightRegionName}）`
  }
  if (orderForm.freightRegionCode && orderForm.freightRegionCode !== '-') {
    return `${orderForm.freightTemplateName}（${orderForm.freightRegionCode}）`
  }
  return orderForm.freightTemplateName
})
const couponDiscount = computed(() => {
  const coupon = selectedCoupon.value
  if (!coupon || coupon.availableFlag !== '1') return 0
  const val = Number(coupon.discountPreview || 0)
  if (!Number.isFinite(val) || val <= 0) return 0
  return Math.min(val, Number(originPayAmount.value || 0))
})
const payableAmount = computed(() => Math.max(0, Number(originPayAmount.value || 0) - Number(couponDiscount.value || 0)))
const visibleCartList = computed(() => {
  if (filterType.value === 'valid') return validItems.value
  if (filterType.value === 'invalid') return cartList.value.filter(i => i.invalidFlag === 1)
  return cartList.value
})
const emptyDesc = computed(() => {
  if (!cartList.value.length) return '购物车为空'
  if (filterType.value === 'valid') return '暂无有效商品'
  if (filterType.value === 'invalid') return '暂无失效商品'
  return '暂无商品'
})
const filterOptions = computed(() => [
  { label: '全部', value: 'all', count: cartList.value.length },
  { label: '有效', value: 'valid', count: validItems.value.length },
  { label: '失效', value: 'invalid', count: invalidCount.value }
])
const couponOptions = computed(() => {
  return buyerCoupons.value.map(item => {
    const preview = Number(item.discountPreview || 0)
    const desc = item.couponType === '1'
      ? `满${Number(item.thresholdAmount || 0).toFixed(2)}减${Number(item.discountAmount || 0).toFixed(2)}`
      : `${Number(item.discountRate || 0)}折`
    return {
      ...item,
      optionLabel: `${item.couponName}（${desc}，预计优惠￥${preview.toFixed(2)}）`
    }
  })
})

async function loadData() {
  try {
    const res = await listCart()
    cartList.value = res.data || []
    await mallStore.refreshCartStat()
  } catch (e) {
    showFailToast(e.message || '加载购物车失败')
  }
}

async function setChecked(item, checked) {
  if (item.invalidFlag === 1) return
  try {
    await updateCartChecked({ cartId: item.cartId, checked: checked ? '1' : '0' })
    item.checked = checked ? '1' : '0'
    await mallStore.refreshCartStat()
  } catch (e) {
    showFailToast(e.message || '更新勾选失败')
  }
}

async function setAllChecked(checked) {
  if (!validItems.value.length) {
    showFailToast('当前没有可勾选商品')
    return
  }
  try {
    await updateCartCheckedAll({ checked: checked ? '1' : '0' })
    validItems.value.forEach(item => {
      item.checked = checked ? '1' : '0'
    })
    await mallStore.refreshCartStat()
  } catch (e) {
    showFailToast(e.message || '全选失败')
  }
}

async function changeQty(item, val) {
  if (!val || item.invalidFlag === 1) return
  try {
    await updateCartQty({ cartId: item.cartId, quantity: Number(val) })
    item.quantity = Number(val)
  } catch (e) {
    showFailToast(e.message || '修改数量失败')
  }
}

async function clearInvalid() {
  if (invalidCount.value === 0) {
    showFailToast('当前没有失效商品')
    return
  }
  try {
    await showConfirmDialog({ title: '提示', message: '确认清理失效商品吗？' })
    await clearInvalidCart()
    showSuccessToast('清理成功')
    await loadData()
  } catch (e) {
    if (e?.message) showFailToast(e.message)
  }
}

async function removeOne(cartId) {
  try {
    await showConfirmDialog({ title: '提示', message: '确认删除该商品吗？' })
    await removeCart(cartId)
    showSuccessToast('删除成功')
    await loadData()
  } catch (e) {
    if (e?.message) showFailToast(e.message)
  }
}

async function removeChecked() {
  const ids = checkedItems.value.map(i => i.cartId)
  if (!ids.length) {
    showFailToast('请先勾选商品')
    return
  }
  try {
    await showConfirmDialog({ title: '提示', message: `确认删除已选 ${ids.length} 件商品吗？` })
    await removeCart(ids.join(','))
    showSuccessToast('删除成功')
    await loadData()
  } catch (e) {
    if (e?.message) showFailToast(e.message)
  }
}

async function openSubmit() {
  if (!checkedCount.value) {
    showFailToast('请先勾选商品')
    return
  }
  await loadAddresses()
  syncReceiverAddress()
  await refreshFreightQuote()
  await loadCoupons()
  submitOpen.value = true
}

async function loadAddresses() {
  try {
    const res = await listAddresses()
    savedAddresses.value = res.data || []
    if (savedAddresses.value.length > 0 && !orderForm.receiverName) {
      const def = savedAddresses.value.find(a => a.isDefault === '1') || savedAddresses.value[0]
      await applyAddress(def)
    } else if (!savedAddresses.value.length && orderForm.receiverAddress) {
      const parsed = splitReceiverAddress(orderForm.receiverAddress)
      selectedRegion.value = parsed.codes
      addressDetail.value = parsed.detail || String(orderForm.receiverAddress || '').trim()
    }
  } catch (e) {
    savedAddresses.value = []
  }
}

async function applyAddress(addr) {
  orderForm.receiverName = addr.receiverName
  orderForm.receiverPhone = addr.receiverPhone
  const parsed = splitReceiverAddress(addr.receiverAddress)
  selectedRegion.value = parsed.codes
  addressDetail.value = parsed.detail
  syncReceiverAddress()
  await refreshFreightQuote()
  await loadCoupons()
}

async function onAddressBlur() {
  syncReceiverAddress()
  await refreshFreightQuote()
  await loadCoupons()
}

async function onRegionChange() {
  syncReceiverAddress()
  await refreshFreightQuote()
  await loadCoupons()
}

function syncReceiverAddress() {
  const detail = String(addressDetail.value || '').trim()
  const selectedCodes = Array.isArray(selectedRegion.value) ? selectedRegion.value.map(code => String(code || '')) : []
  orderForm.receiverProvinceCode = selectedCodes[0] || ''
  orderForm.receiverCityCode = selectedCodes[1] || ''
  orderForm.receiverDistrictCode = selectedCodes[2] || ''
  const regionText = (selectedRegion.value || [])
    .map(code => codeToText[String(code)])
    .filter(Boolean)
  if (regionText.length === 3) {
    orderForm.receiverAddress = `${regionText.join(' ')}${detail ? ` ${detail}` : ''}`.trim()
    return
  }
  orderForm.receiverAddress = detail
}

async function refreshFreightQuote() {
  if (!checkedCount.value || !String(orderForm.receiverAddress || '').trim()) {
    orderForm.freightAmount = 0
    orderForm.freightTemplateName = ''
    orderForm.freightRegionName = ''
    orderForm.freightRegionCode = ''
    return
  }
  quoteLoading.value = true
  try {
    const res = await quoteFreight({
      receiverAddress: orderForm.receiverAddress,
      receiverProvinceCode: orderForm.receiverProvinceCode,
      receiverCityCode: orderForm.receiverCityCode,
      receiverDistrictCode: orderForm.receiverDistrictCode
    })
    const quote = res.data || {}
    orderForm.freightAmount = Number(quote.freightAmount || 0)
    orderForm.freightTemplateName = quote.templateName || ''
    orderForm.freightRegionName = quote.regionName || ''
    orderForm.freightRegionCode = quote.regionCode || ''
  } catch (e) {
    orderForm.freightAmount = 0
    orderForm.freightTemplateName = ''
    orderForm.freightRegionName = ''
    orderForm.freightRegionCode = ''
    showFailToast(e.message || '运费试算失败')
  } finally {
    quoteLoading.value = false
  }
}

async function confirmSubmit() {
  syncReceiverAddress()
  if (!orderForm.receiverName || !orderForm.receiverPhone || !orderForm.receiverAddress) {
    showFailToast('请补全收货信息')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(orderForm.receiverPhone)) {
    showFailToast('手机号格式不正确')
    return
  }
  await refreshFreightQuote()
  await loadCoupons()
  loading.value = true
  try {
    const payload = {
      ...orderForm,
      userCouponId: orderForm.userCouponId || null
    }
    const res = await submitOrder(payload)
    showSuccessToast(`提交成功，共${res.data.orderCount}笔订单`)
    submitOpen.value = false
    orderForm.buyerRemark = ''
    orderForm.userCouponId = null
    orderForm.freightAmount = 0
    orderForm.freightTemplateName = ''
    orderForm.freightRegionName = ''
    orderForm.freightRegionCode = ''
    buyerCoupons.value = []
    await loadData()
  } catch (e) {
    showFailToast(e.message || '提交失败')
  } finally {
    loading.value = false
  }
}

async function loadCoupons() {
  try {
    const res = await listBuyerCoupons({ orderAmount: originPayAmount.value })
    buyerCoupons.value = res.data || []
    if (orderForm.userCouponId && !buyerCoupons.value.some(item => String(item.userCouponId) === String(orderForm.userCouponId))) {
      orderForm.userCouponId = null
    }
  } catch (e) {
    buyerCoupons.value = []
  }
}

function goDetail(item) {
  if (item.invalidFlag === 1) return
  router.push(`/products/${item.productId}`)
}

function imageUrl(path) {
  return resolveMediaUrl(path)
}

watch([submitOpen, checkedCount, totalAmount], async ([open]) => {
  if (!open) return
  await refreshFreightQuote()
  await loadCoupons()
})

onMounted(loadData)

function buildRegionCandidates(options) {
  const result = []
  for (const province of options || []) {
    for (const city of province.children || []) {
      for (const district of city.children || []) {
        const labels = [province.label, city.label, district.label]
        const codes = [province.value, city.value, district.value]
        result.push({
          labels,
          codes,
          withSpace: labels.join(' '),
          compact: labels.join('')
        })
      }
    }
  }
  return result
}

function buildRegionOptions(list) {
  const provinceList = Object.entries(list?.province_list || {})
  const cityList = Object.entries(list?.city_list || {})
  const countyList = Object.entries(list?.county_list || {})

  const citiesByProvince = new Map()
  const countiesByCity = new Map()

  for (const [cityCodeRaw, cityName] of cityList) {
    const cityCode = String(cityCodeRaw)
    const provincePrefix = cityCode.slice(0, 2)
    if (!citiesByProvince.has(provincePrefix)) citiesByProvince.set(provincePrefix, [])
    citiesByProvince.get(provincePrefix).push({ code: cityCode, name: cityName })
  }

  for (const [countyCodeRaw, countyName] of countyList) {
    const countyCode = String(countyCodeRaw)
    const cityPrefix = countyCode.slice(0, 4)
    if (!countiesByCity.has(cityPrefix)) countiesByCity.set(cityPrefix, [])
    countiesByCity.get(cityPrefix).push({ code: countyCode, name: countyName })
  }

  return provinceList.map(([provinceCodeRaw, provinceName]) => {
    const provinceCode = String(provinceCodeRaw)
    const provincePrefix = provinceCode.slice(0, 2)
    const cities = (citiesByProvince.get(provincePrefix) || []).map((city) => {
      const cityPrefix = city.code.slice(0, 4)
      const counties = (countiesByCity.get(cityPrefix) || []).map((county) => ({
        value: county.code,
        label: county.name
      }))
      return { value: city.code, label: city.name, children: counties }
    })
    return { value: provinceCode, label: provinceName, children: cities }
  })
}

function splitReceiverAddress(fullAddress) {
  const source = String(fullAddress || '').trim().replace(/\s+/g, ' ')
  if (!source) return { codes: [], detail: '' }
  const compactSource = source.replace(/\s+/g, '')
  let matched = null
  for (const item of regionCandidates) {
    if (
      source === item.withSpace ||
      source.startsWith(`${item.withSpace} `) ||
      compactSource.startsWith(item.compact)
    ) {
      if (!matched || item.compact.length > matched.compact.length) matched = item
    }
  }
  if (!matched) return { codes: [], detail: source }

  let detail = source
  if (source.startsWith(matched.withSpace)) {
    detail = source.slice(matched.withSpace.length).trim()
  } else {
    detail = source
    for (const label of matched.labels) {
      if (detail.startsWith(label)) detail = detail.slice(label.length).trim()
    }
  }
  return { codes: matched.codes, detail }
}
</script>

<style scoped>
.cart-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 24px 0;
}

.cart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cart-head h2 {
  margin: 0;
  font-size: 32px;
  font-weight: 900;
}

.cart-head p {
  margin: 6px 0 0;
  color: #64748b;
}

.head-actions {
  display: flex;
  gap: 6px;
}

.cart-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  align-items: start;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tool-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-tip {
  color: #64748b;
  font-size: 13px;
}

.tool-right {
  display: flex;
  gap: 8px;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-card {
  display: grid;
  grid-template-columns: 36px 110px 1fr 220px 50px;
  gap: 14px;
  align-items: center;
  padding: 14px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
}

.item-card.invalid {
  opacity: 0.88;
  background: #f8fafc;
}

.cover-col {
  position: relative;
}

.cover {
  width: 110px;
  height: 84px;
  border-radius: 10px;
  object-fit: cover;
  cursor: pointer;
}

.invalid-mask {
  position: absolute;
  inset: 0;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.5);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 800;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name-row h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
  cursor: pointer;
}

.desc-row {
  margin-top: 7px;
  color: #64748b;
  font-size: 13px;
}

.invalid-text {
  color: #dc2626;
}

.price-row {
  margin-top: 9px;
  color: #0f172a;
  font-weight: 700;
}

.qty-col {
  display: grid;
  justify-items: end;
  gap: 8px;
}

.subtotal {
  color: #ef4444;
  font-weight: 800;
}

.act-col {
  display: flex;
  justify-content: center;
}

.summary {
  position: sticky;
  top: 96px;
}

.summary h3 {
  margin: 0 0 12px;
  font-size: 22px;
}

.sum-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 9px;
  color: #334155;
}

.free {
  color: #16a34a;
}

.fee {
  color: #0f766e;
}

.coupon {
  color: #ea580c;
}

.sum-line {
  border-top: 1px dashed #cbd5e1;
  margin: 12px 0;
}

.sum-total {
  display: flex;
  align-items: end;
  justify-content: space-between;
  margin-bottom: 14px;
}

.sum-total strong {
  color: #ef4444;
  font-size: 28px;
}

.checkout-btn {
  width: 100%;
  height: 44px;
}

.safe-list {
  margin-top: 12px;
  display: grid;
  gap: 6px;
  color: #475569;
}

.safe-list div {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.empty-wrap {
  padding: 56px 20px;
}

.submit-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 12px;
}

.submit-form :deep(.el-form-item:nth-child(3)),
.submit-form :deep(.el-form-item:nth-child(4)),
.submit-form :deep(.el-form-item:nth-child(5)) {
  grid-column: 1 / -1;
}

.dialog-foot {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Premium Dialog Styles */
.premium-order-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.dialog-header-premium {
  padding: 24px 32px;
  background: linear-gradient(to right, #f8fafc, #ffffff);
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 16px;
  border-radius: 20px 20px 0 0;
}

.header-icon {
  font-size: 32px;
  color: #3b82f6;
  background: #eff6ff;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-text h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}

.header-text p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.dialog-body-premium {
  padding: 32px;
}

.address-selector-premium {
  margin-bottom: 32px;
}

.selector-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.selector-title span {
  font-size: 14px;
  font-weight: 800;
  color: #334155;
}

.address-scroll-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 12px;
  scrollbar-width: none;
}

.address-scroll-row::-webkit-scrollbar {
  display: none;
}

.address-card-mini {
  min-width: 240px;
  max-width: 240px;
  background: #f8fafc;
  border: 2px solid #f1f5f9;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}

.address-card-mini:hover {
  border-color: #3b82f6;
  background: #fff;
}

.address-card-mini.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-top .name {
  font-weight: 800;
  color: #1e293b;
}

.card-mid {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.card-bot {
  font-size: 13px;
  color: #475569;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.active-check {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #3b82f6;
  color: #fff;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: none;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.3);
}

.address-card-mini.active .active-check {
  display: flex;
}

.submit-form-premium .form-row {
  display: flex;
  gap: 16px;
}

.flex-1 { flex: 1; }

.submit-form-premium :deep(.el-form-item__label) {
  font-weight: 800;
  color: #334155;
  padding-bottom: 8px;
}

.freight-preview {
  width: 100%;
  min-height: 42px;
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #334155;
}

.freight-preview b {
  color: #0f766e;
  font-size: 16px;
}

.full-width-select { width: 100%; }

.coupon-option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.coupon-option-item .c-val {
  color: #ef4444;
  font-weight: 800;
}

.coupon-option-item .c-err {
  color: #94a3b8;
  font-size: 12px;
}

.dialog-footer-premium {
  padding: 24px 32px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-summary-box .lbl {
  font-size: 14px;
  color: #64748b;
  margin-right: 12px;
}

.price-summary-box .unit {
  font-size: 16px;
  color: #ef4444;
  font-weight: 800;
}

.price-summary-box .val {
  font-size: 32px;
  color: #ef4444;
  font-weight: 900;
  letter-spacing: -1px;
}

.btn-confirm-order {
  height: 48px !important;
  padding: 0 32px !important;
  font-weight: 900 !important;
  font-size: 16px !important;
  box-shadow: 0 10px 20px rgba(59, 130, 246, 0.2) !important;
}
</style>
