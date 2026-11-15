<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <div class="title-block">
            <span>购物车管理</span>
            <el-tag type="info">商品 {{ cartList.length }} 件</el-tag>
            <el-tag v-if="invalidCount > 0" type="warning">失效 {{ invalidCount }} 件</el-tag>
          </div>
          <div>
            <el-button @click="toggleAll(true)">一键勾选</el-button>
            <el-button @click="toggleAll(false)">取消勾选</el-button>
            <el-button type="danger" plain @click="removeChecked">删除已选</el-button>
            <el-button type="warning" @click="clearInvalid">清理失效商品</el-button>
            <el-button type="primary" @click="submitOrder">提交订单</el-button>
          </div>
        </div>
      </template>

      <div class="toolbar-row">
        <el-checkbox :model-value="allChecked" :indeterminate="indeterminate" @change="onCheckAllChange">
          全选有效商品
        </el-checkbox>
        <el-radio-group v-model="filterType" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="valid">仅有效</el-radio-button>
          <el-radio-button label="invalid">仅失效</el-radio-button>
        </el-radio-group>
      </div>

      <el-table
        v-if="visibleCartList.length > 0"
        :data="visibleCartList"
        row-key="cartId"
        border
        :row-class-name="tableRowClassName"
        v-loading="loading"
      >
        <el-table-column width="60" label="选">
          <template #default="scope">
            <el-checkbox
              :model-value="scope.row.checked === '1'"
              :disabled="scope.row.invalidFlag === 1"
              @change="val => setChecked(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="220">
          <template #default="scope">
            <div class="product-cell">
              <el-image :src="imageUrl(scope.row.productCover)" class="thumb" />
              <div>
                <div>{{ scope.row.productName || '商品已失效' }}</div>
                <div class="invalid" v-if="scope.row.invalidFlag === 1">失效商品（已下架/库存不足）</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="110">
          <template #default="scope">￥{{ scope.row.price || 0 }}</template>
        </el-table-column>
        <el-table-column label="数量" width="180">
          <template #default="scope">
            <el-input-number
              :model-value="scope.row.quantity"
              :min="1"
              :max="Math.max(1, Number(scope.row.stock || 1))"
              :disabled="scope.row.invalidFlag === 1"
              @change="val => updateQty(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="库存" width="90" prop="stock" />
        <el-table-column label="小计" width="120">
          <template #default="scope">￥{{ (Number(scope.row.price || 0) * Number(scope.row.quantity || 0)).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="scope">
            <el-button link type="danger" @click="removeItem(scope.row.cartId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else-if="!loading" :description="emptyDesc" />

      <div class="summary" v-if="visibleCartList.length > 0">
        已选 {{ checkedCount }} 件，预计支付：<span>￥{{ checkedAmount.toFixed(2) }}</span>
      </div>
    </el-card>

    <el-dialog v-model="orderOpen" title="提交订单" width="520px">
      <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="90px">
        <el-form-item label="收货人" prop="receiverName"><el-input v-model="orderForm.receiverName" /></el-form-item>
        <el-form-item label="手机号" prop="receiverPhone"><el-input v-model="orderForm.receiverPhone" /></el-form-item>
        <el-form-item label="收货地址" prop="receiverAddress"><el-input v-model="orderForm.receiverAddress" /></el-form-item>
        <el-form-item label="运费"><el-input-number v-model="orderForm.freightAmount" :min="0" /></el-form-item>
        <el-form-item label="支付通道">
          <el-select v-model="orderForm.paymentChannel" style="width: 100%">
            <el-option value="1" label="模拟支付SDK" />
            <el-option value="2" label="支付宝沙箱" />
          </el-select>
        </el-form-item>
        <el-form-item label="买家备注">
          <el-input v-model="orderForm.buyerRemark" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderOpen = false">取消</el-button>
        <el-button type="primary" @click="confirmSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resolveMediaUrl } from '@/utils/media'
import {
  clearBuyerInvalidCart,
  listBuyerCart,
  removeBuyerCart,
  submitBuyerOrder,
  updateBuyerCartChecked,
  updateBuyerCartCheckedAll,
  updateBuyerCartQty
} from '@/api/agri/mall'

const loading = ref(false)
const filterType = ref('all')
const cartList = ref([])
const orderOpen = ref(false)
const orderFormRef = ref(null)
const orderForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  freightAmount: 0,
  paymentChannel: '1',
  buyerRemark: ''
})

const orderRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const validItems = computed(() => cartList.value.filter(i => i.invalidFlag !== 1))
const invalidCount = computed(() => cartList.value.filter(i => i.invalidFlag === 1).length)
const checkedItems = computed(() => validItems.value.filter(i => i.checked === '1'))
const checkedIds = computed(() => checkedItems.value.map(i => i.cartId))
const checkedCount = computed(() => checkedItems.value.length)
const checkedAmount = computed(() => checkedItems.value
  .reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0))
const emptyDesc = computed(() => {
  if (!cartList.value.length) return '购物车为空'
  if (filterType.value === 'valid') return '暂无有效商品'
  if (filterType.value === 'invalid') return '暂无失效商品'
  return '暂无商品'
})

const allChecked = computed(() => validItems.value.length > 0 && checkedItems.value.length === validItems.value.length)
const indeterminate = computed(() => checkedItems.value.length > 0 && checkedItems.value.length < validItems.value.length)

const visibleCartList = computed(() => {
  if (filterType.value === 'valid') return cartList.value.filter(i => i.invalidFlag !== 1)
  if (filterType.value === 'invalid') return cartList.value.filter(i => i.invalidFlag === 1)
  return cartList.value
})

function loadCart() {
  loading.value = true
  listBuyerCart().then(res => {
    cartList.value = res.data || []
  }).catch(err => {
    ElMessage.error(err.message || '购物车加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function updateQty(row, val) {
  if (!val || Number(val) < 1) return
  updateBuyerCartQty({ cartId: row.cartId, quantity: Number(val) }).then(() => {
    row.quantity = Number(val)
  }).catch(err => {
    ElMessage.error(err.message || '数量修改失败')
  })
}

function setChecked(row, checked) {
  if (row.invalidFlag === 1) return
  updateBuyerCartChecked({ cartId: row.cartId, checked: checked ? '1' : '0' }).then(() => {
    row.checked = checked ? '1' : '0'
  }).catch(err => {
    ElMessage.error(err.message || '勾选状态更新失败')
  })
}

function onCheckAllChange(flag) {
  toggleAll(!!flag)
}

function toggleAll(flag) {
  if (validItems.value.length === 0) {
    ElMessage.warning('暂无可勾选商品')
    return
  }
  updateBuyerCartCheckedAll({ checked: flag ? '1' : '0' }).then(() => {
    cartList.value.forEach(item => {
      if (item.invalidFlag !== 1) {
        item.checked = flag ? '1' : '0'
      }
    })
  }).catch(err => {
    ElMessage.error(err.message || '批量勾选失败')
  })
}

async function clearInvalid() {
  if (invalidCount.value === 0) {
    ElMessage.info('当前没有失效商品')
    return
  }
  try {
    await ElMessageBox.confirm('确认清理所有失效商品吗？', '提示', { type: 'warning' })
    await clearBuyerInvalidCart()
    ElMessage.success('已清理失效商品')
    loadCart()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '清理失败')
    }
  }
}

async function removeItem(cartId) {
  try {
    await ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' })
    await removeBuyerCart(cartId)
    ElMessage.success('删除成功')
    loadCart()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '删除失败')
    }
  }
}

async function removeChecked() {
  if (checkedIds.value.length === 0) {
    ElMessage.warning('请先勾选商品')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除已勾选的 ${checkedIds.value.length} 件商品吗？`, '提示', { type: 'warning' })
    await removeBuyerCart(checkedIds.value.join(','))
    ElMessage.success('批量删除成功')
    loadCart()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '批量删除失败')
    }
  }
}

function submitOrder() {
  if (checkedCount.value === 0) {
    ElMessage.warning('请先勾选商品')
    return
  }
  orderOpen.value = true
}

function confirmSubmit() {
  if (!orderFormRef.value) return
  orderFormRef.value.validate(valid => {
    if (!valid) return
    submitBuyerOrder(orderForm).then(res => {
      ElMessage.success(`订单创建成功，共 ${res.data.orderCount} 笔，总金额 ￥${Number(res.data.totalPay || 0).toFixed(2)}`)
      orderOpen.value = false
      orderForm.buyerRemark = ''
      loadCart()
    }).catch(err => {
      ElMessage.error(err.message || '订单提交失败')
    })
  })
}

function tableRowClassName({ row }) {
  return row.invalidFlag === 1 ? 'invalid-row' : ''
}

onMounted(loadCart)

function imageUrl(path) {
  return resolveMediaUrl(path)
}
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.title-block { display: flex; align-items: center; gap: 8px; }
.toolbar-row { margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; }
.product-cell { display: flex; gap: 12px; align-items: center; }
.thumb { width: 52px; height: 52px; border-radius: 8px; }
.invalid { margin-top: 4px; color: #f56c6c; font-size: 12px; }
.summary { margin-top: 14px; text-align: right; color: #333; }
.summary span { color: #f56c6c; font-size: 18px; font-weight: 700; }
:deep(.invalid-row) { background: #fff7f7; }
</style>
