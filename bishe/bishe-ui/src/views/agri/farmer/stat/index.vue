<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="toolbar">
        <el-radio-group v-model="period" @change="loadStat">
          <el-radio-button label="day">今日</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
        </el-radio-group>
      </div>
      <el-row :gutter="16" class="mb16">
        <el-col :span="8"><el-statistic title="订单量" :value="summary.totalOrders || 0" /></el-col>
        <el-col :span="8"><el-statistic title="销售额" :value="Number(summary.totalSales || 0)" prefix="￥" /></el-col>
        <el-col :span="8"><el-statistic title="低库存预警" :value="lowStockCount || 0" /></el-col>
      </el-row>
      <div ref="chartRef" class="chart"></div>
    </el-card>

    <el-card shadow="never" class="mt16">
      <template #header>热销排行</template>
      <el-table :data="hotProducts" border>
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="saleQty" label="销量" width="100" />
        <el-table-column prop="saleAmount" label="销售额" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onMounted, ref } from 'vue'
import { getFarmerStats } from '@/api/agri/mall'

const period = ref('day')
const summary = ref({})
const hotProducts = ref([])
const lowStockCount = ref(0)
const chartRef = ref()
let chart

function renderChart(curve = []) {
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: curve.map(i => i.statDate) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, data: curve.map(i => Number(i.salesAmount || 0)) }]
  })
}

function loadStat() {
  getFarmerStats({ period: period.value }).then(async res => {
    const data = res.data || {}
    summary.value = data.summary || {}
    hotProducts.value = data.hotProducts || []
    lowStockCount.value = data.lowStockCount || 0
    await nextTick()
    renderChart(data.curve || [])
  })
}

onMounted(loadStat)
</script>

<style scoped>
.toolbar { display: flex; justify-content: flex-end; margin-bottom: 16px; }
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.chart { width: 100%; height: 320px; }
</style>
