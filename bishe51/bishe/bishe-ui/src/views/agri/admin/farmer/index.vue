<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 130px">
            <el-option label="待审核" value="0" />
            <el-option label="已审核" value="1" />
            <el-option label="已驳回" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border>
        <el-table-column prop="userName" label="账号" width="130" />
        <el-table-column prop="nickName" label="昵称" width="120" />
        <el-table-column prop="farmName" label="农场" min-width="180" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="160" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="success" v-if="scope.row.status === '0'" @click="audit(scope.row, '1')">通过</el-button>
            <el-button link type="danger" v-if="scope.row.status === '0'" @click="audit(scope.row, '2')">驳回</el-button>
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
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditAdminFarmerApply, listAdminFarmerApplies } from '@/api/agri/mall'

const query = reactive({ pageNum: 1, pageSize: 10, status: '', keyword: '' })
const list = ref([])
const total = ref(0)
const STATUS_MAP = { '0': '待审核', '1': '已审核', '2': '已驳回' }

function statusText(status) {
  return STATUS_MAP[String(status)] || `状态${status}`
}

function statusTag(status) {
  const code = String(status)
  if (code === '1') return 'success'
  if (code === '2') return 'danger'
  return 'warning'
}

function loadList() {
  listAdminFarmerApplies(query).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  })
}

function audit(row, status) {
  ElMessageBox.prompt('请输入审核备注', status === '1' ? '通过申请' : '驳回申请').then(({ value }) => {
    auditAdminFarmerApply({ applyId: row.applyId, status, auditRemark: value }).then(() => {
      ElMessage.success('处理成功')
      loadList()
    })
  })
}

loadList()
</script>
