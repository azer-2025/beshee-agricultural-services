<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="query" class="mb12">
        <el-form-item label="买家ID">
          <el-input v-model="query.buyerId" clearable placeholder="输入买家ID" style="width: 140px" />
        </el-form-item>
        <el-form-item label="模式">
          <el-select v-model="query.replyMode" clearable style="width: 140px">
            <el-option label="remote" value="remote" />
            <el-option label="fallback" value="fallback" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="买家/账号/消息内容" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="buyerId" label="买家ID" width="100" />
        <el-table-column prop="buyerName" label="买家昵称" width="140" />
        <el-table-column prop="buyerUserName" label="登录账号" width="140" />
        <el-table-column label="角色" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'assistant' ? 'success' : 'info'">
              {{ scope.row.role === 'assistant' ? 'AI' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="模式" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.replyMode === 'remote' ? 'primary' : 'warning'">
              {{ scope.row.replyMode || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="服务商" width="160" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型" width="160" show-overflow-tooltip />
        <el-table-column label="消息内容" min-width="360" show-overflow-tooltip>
          <template #default="scope">{{ plainText(scope.row.content) || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
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
import { ElMessage } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { listAdminAiChats } from '@/api/agri/mall'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, buyerId: '', replyMode: '', keyword: '' })

function formatDateTime(value) {
  return parseTime(value) || '-'
}

function plainText(text) {
  if (!text) return ''
  return String(text)
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function loadList() {
  loading.value = true
  listAdminAiChats(query).then((res) => {
    list.value = res.rows || []
    total.value = Number(res.total || 0)
  }).catch((err) => {
    ElMessage.error(err.message || 'AI客服记录加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function onSearch() {
  query.pageNum = 1
  loadList()
}

function onReset() {
  query.pageNum = 1
  query.pageSize = 10
  query.buyerId = ''
  query.replyMode = ''
  query.keyword = ''
  loadList()
}

loadList()
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}
</style>
