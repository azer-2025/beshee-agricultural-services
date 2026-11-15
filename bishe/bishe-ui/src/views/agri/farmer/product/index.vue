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
        <el-form-item label="商品名称">
          <el-input v-model="query.keyword" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
          <el-button @click="openForm()">新增商品</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border>
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="categoryId" label="分类ID" width="90" />
        <el-table-column prop="price" label="价格" width="90" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="90" />
        <el-table-column prop="auditRemark" label="审核备注" min-width="160" />
        <el-table-column label="操作" width="170">
          <template #default="scope">
            <el-button link type="primary" @click="openForm(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="remove(scope.row)">删除</el-button>
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

    <el-dialog v-model="open" :title="form.productId ? '编辑商品' : '新增商品'" width="700px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类ID"><el-input-number v-model="form.categoryId" :min="1" /></el-form-item>
        <el-form-item label="商品名称"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="封面图">
          <ImageUpload v-model="form.productCover" :limit="1" />
        </el-form-item>
        <el-form-item label="商品图集">
          <ImageUpload v-model="form.productImages" :limit="6" />
        </el-form-item>
        <el-form-item label="商品描述"><el-input v-model="form.productDesc" type="textarea" rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFarmerProducts, removeFarmerProduct, saveFarmerProduct } from '@/api/agri/mall'

const query = reactive({ pageNum: 1, pageSize: 10, status: '', keyword: '' })
const list = ref([])
const total = ref(0)
const open = ref(false)
const form = reactive({ productId: null, categoryId: 100, productName: '', price: 0, stock: 0, productCover: '', productImages: '', productDesc: '' })

function loadList() {
  listFarmerProducts(query).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  })
}

function openForm(row) {
  if (!row) {
    Object.assign(form, { productId: null, categoryId: 100, productName: '', price: 0, stock: 0, productCover: '', productImages: '', productDesc: '' })
  } else {
    Object.assign(form, row)
  }
  open.value = true
}

function submit() {
  saveFarmerProduct(form).then(() => {
    ElMessage.success('提交成功，等待管理员审核')
    open.value = false
    loadList()
  })
}

function remove(row) {
  ElMessageBox.confirm('确认删除该商品？').then(() => {
    removeFarmerProduct(row.productId).then(() => {
      ElMessage.success('删除成功')
      loadList()
    })
  })
}

loadList()
</script>
