<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 130px">
            <el-option label="待审核" value="0" />
            <el-option label="上架" value="1" />
            <el-option label="驳回" value="2" />
            <el-option label="下架" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="openForm()">新增商品</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border>
        <el-table-column prop="productName" label="商品" min-width="180" />
        <el-table-column label="图片" width="110" align="center">
          <template #default="scope">
            <ImagePreview v-if="scope.row.productCover" :src="scope.row.productCover" :width="60" :height="60" />
            <span v-else class="empty-cover">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="farmerName" label="农户" width="120" />
        <el-table-column prop="price" label="价格" width="90" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            {{ statusText(scope.row.status) }}
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="160" />
        <el-table-column label="操作" width="320">
          <template #default="scope">
            <el-button link type="primary" @click="openForm(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="openMediaForm(scope.row)">改图</el-button>
            <el-button link type="danger" @click="removeRow(scope.row)">删除</el-button>
            <el-button link type="success" v-if="isStatus(scope.row.status, '0')" @click="audit(scope.row, '1')">通过</el-button>
            <el-button link type="danger" v-if="isStatus(scope.row.status, '0')" @click="audit(scope.row, '2')">驳回</el-button>
            <el-button link type="warning" v-if="isStatus(scope.row.status, '1')" @click="audit(scope.row, '3')">下架</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        @pagination="loadList"
      />
    </el-card>

    <el-dialog v-model="open" :title="form.productId ? '编辑商品' : '新增商品'" width="720px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="农户ID">
          <el-input-number v-model="form.farmerId" :min="1" />
        </el-form-item>
        <el-form-item label="分类ID">
          <el-input-number v-model="form.categoryId" :min="1" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 160px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <ImageUpload v-model="form.productCover" :limit="1" />
          <el-button v-if="form.productCover" link type="primary" @click="form.productCover = ''">重新上传</el-button>
        </el-form-item>
        <el-form-item label="商品图集">
          <ImageUpload v-model="form.productImages" :limit="6" />
          <el-button v-if="form.productImages" link type="primary" @click="form.productImages = ''">清空图集</el-button>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.productDesc" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="form.auditRemark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mediaOpen" title="修改商品图片" width="640px">
      <el-form :model="mediaForm" label-width="90px">
        <el-form-item label="封面图">
          <ImageUpload v-model="mediaForm.productCover" :limit="1" />
        </el-form-item>
        <el-form-item label="商品图集">
          <ImageUpload v-model="mediaForm.productImages" :limit="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mediaOpen = false">取消</el-button>
        <el-button type="primary" @click="saveMedia">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditAdminProduct, listAdminProducts, removeAdminProduct, saveAdminProduct, updateAdminProductMedia } from '@/api/agri/mall'

const query = reactive({ pageNum: 1, pageSize: 10, status: '', keyword: '' })
const list = ref([])
const total = ref(0)
const open = ref(false)
const mediaOpen = ref(false)
const mediaForm = reactive({ productId: null, productCover: '', productImages: '' })

const statusOptions = [
  { label: '待审核', value: '0' },
  { label: '上架', value: '1' },
  { label: '驳回', value: '2' },
  { label: '下架', value: '3' }
]

const initForm = () => ({
  productId: null,
  farmerId: null,
  categoryId: 100,
  productName: '',
  price: 0,
  stock: 0,
  productCover: '',
  productImages: '',
  productDesc: '',
  status: '1',
  auditRemark: ''
})
const form = reactive(initForm())

function loadList() {
  listAdminProducts(query).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  })
}

function audit(row, status) {
  ElMessageBox.prompt('请输入审核备注', '审核商品').then(({ value }) => {
    auditAdminProduct({ productId: row.productId, status, auditRemark: value }).then(() => {
      ElMessage.success('处理成功')
      loadList()
    })
  })
}

function openForm(row) {
  Object.assign(form, initForm())
  if (row) {
    Object.assign(form, row, {
      status: String(row.status ?? '1'),
      productCover: normalizeImageField(row.productCover),
      productImages: normalizeImageField(row.productImages)
    })
  }
  open.value = true
}

function openMediaForm(row) {
  mediaForm.productId = row.productId
  mediaForm.productCover = normalizeImageField(row.productCover)
  mediaForm.productImages = normalizeImageField(row.productImages)
  mediaOpen.value = true
}

function submit() {
  form.productCover = normalizeImageField(form.productCover)
  form.productImages = normalizeImageField(form.productImages)
  saveAdminProduct(form).then(() => {
    ElMessage.success('保存成功')
    open.value = false
    loadList()
  })
}

function removeRow(row) {
  ElMessageBox.confirm('确认删除该商品？').then(() => {
    removeAdminProduct(row.productId).then(() => {
      ElMessage.success('删除成功')
      loadList()
    })
  })
}

function saveMedia() {
  updateAdminProductMedia({
    productId: mediaForm.productId,
    productCover: normalizeImageField(mediaForm.productCover),
    productImages: normalizeImageField(mediaForm.productImages)
  }).then(() => {
    ElMessage.success('图片更新成功')
    mediaOpen.value = false
    loadList()
  })
}

function isStatus(status, target) {
  return String(status) === target
}

function statusText(status) {
  const target = statusOptions.find(item => item.value === String(status))
  return target ? target.label : '-'
}

function normalizeImageField(value) {
  if (value === null || value === undefined) return ''
  const text = String(value).trim()
  if (!text || text === 'null' || text === 'undefined') return ''
  return text
}

loadList()
</script>

<style scoped>
.empty-cover {
  color: #909399;
  font-size: 12px;
}
</style>
