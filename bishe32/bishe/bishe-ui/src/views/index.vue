<template>
  <div class="dashboard-container home">
    <div class="dashboard-header">
      <div>
        <h2 class="welcome-text">{{ pageTitle }}</h2>
        <div class="header-desc">{{ pageDescription }}</div>
      </div>
      <div class="header-actions">
        <el-radio-group v-if="isFarmer" v-model="farmerPeriod" @change="loadDashboard">
          <el-radio-button label="day">今日</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
        </el-radio-group>
        <div class="current-date">{{ currentDate }}</div>
      </div>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="item in statistics" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.prefix }}{{ item.value }}{{ item.suffix }}</div>
              <div class="stat-footer">{{ item.note }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <template v-if="isFarmer">
      <el-row :gutter="20" class="chart-row">
        <el-col :span="16">
          <el-card shadow="hover" :header="`${farmerPeriodLabel}销售趋势`">
            <div ref="salesChartRef" class="chart chart-lg"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" header="热销商品排行">
            <div ref="secondaryChartRef" class="chart chart-lg"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row bottom-row">
        <el-col :span="10">
          <el-card shadow="hover" header="订单完成情况">
            <div ref="tertiaryChartRef" class="chart chart-md"></div>
          </el-card>
        </el-col>
        <el-col :span="14">
          <el-card shadow="hover" header="热销商品明细">
            <el-table :data="farmerHotProducts" class="hot-table" empty-text="暂无销售数据">
              <el-table-column type="index" label="#" width="60" align="center" />
              <el-table-column prop="productName" label="商品名称" min-width="180" />
              <el-table-column prop="saleQty" label="销量" width="100" align="center" />
              <el-table-column label="销售额" width="140" align="right">
                <template #default="{ row }">¥ {{ formatNumber(row.saleAmount, 2) }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <template v-else>
      <el-row :gutter="20" class="chart-row">
        <el-col :span="16">
          <el-card shadow="hover" header="近六个月销售额趋势">
            <div ref="salesChartRef" class="chart chart-lg"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" header="农产品分类占比">
            <div ref="secondaryChartRef" class="chart chart-lg"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row bottom-row">
        <el-col :span="12">
          <el-card shadow="hover" header="订单状态分布">
            <div ref="tertiaryChartRef" class="chart chart-md"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="notice-card">
            <template #header>
              <div class="card-header">
                <span>系统公告 / 最新动态</span>
                <el-button class="button" text @click="$router.push('/system/notice')">查看更多</el-button>
              </div>
            </template>
            <div class="notice-list">
              <div v-for="notice in adminNotices" :key="`${notice.title}-${notice.time}`" class="notice-item">
                <el-tag :type="notice.type" size="small" class="notice-tag">{{ notice.tag }}</el-tag>
                <span class="notice-title">{{ notice.title }}</span>
                <span class="notice-time">{{ notice.time }}</span>
              </div>
              <el-empty v-if="adminNotices.length === 0" description="暂无动态" :image-size="60" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup name="Index">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getAdminStat, getFarmerStat } from '@/api/agri/stat'
import useUserStore from '@/store/modules/user'
const userStore = useUserStore()

const isFarmer = computed(() => {
  return userStore.roles.includes('farmer') || userStore.permissions.includes('agri:farmer:stat:list')
})

const currentDate = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const farmerPeriod = ref('day')

const adminSummary = ref({
  totalSales: 0,
  totalOrders: 0,
  totalFarmers: 0,
  totalProducts: 0
})
const adminSalesCurve = ref([])
const adminCategoryDistribution = ref([])
const adminStatusDistribution = ref([])
const adminNotices = ref([])

const farmerSummary = ref({
  totalOrders: 0,
  totalSales: 0,
  completedOrders: 0
})
const farmerCurve = ref([])
const farmerHotProducts = ref([])
const farmerLowStockCount = ref(0)

const salesChartRef = ref(null)
const secondaryChartRef = ref(null)
const tertiaryChartRef = ref(null)

const chartInstances = {
  sales: null,
  secondary: null,
  tertiary: null
}

const pageTitle = computed(() => {
  return isFarmer.value ? '农户经营数据看板' : '助农产品商城管理概览'
})

const pageDescription = computed(() => {
  if (isFarmer.value) {
    return `${farmerPeriodLabel.value}经营表现与商品销售数据`
  }
  return '平台经营总览与监管动态'
})

const farmerPeriodLabel = computed(() => (farmerPeriod.value === 'month' ? '本月' : '今日'))

const farmerCompletionRate = computed(() => {
  const totalOrders = Number(farmerSummary.value.totalOrders || 0)
  const completedOrders = Number(farmerSummary.value.completedOrders || 0)
  if (!totalOrders) {
    return 0
  }
  return Number(((completedOrders / totalOrders) * 100).toFixed(1))
})

const statistics = computed(() => {
  if (isFarmer.value) {
    return [
      {
        title: `${farmerPeriodLabel.value}订单量`,
        value: formatNumber(farmerSummary.value.totalOrders),
        prefix: '',
        suffix: ' 单',
        icon: 'ShoppingCart',
        color: '#67C23A',
        note: '已支付订单统计'
      },
      {
        title: `${farmerPeriodLabel.value}销售额`,
        value: formatNumber(farmerSummary.value.totalSales, 2),
        prefix: '¥ ',
        suffix: '',
        icon: 'Money',
        color: '#409EFF',
        note: '已支付订单成交金额'
      },
      {
        title: '已完成订单',
        value: formatNumber(farmerSummary.value.completedOrders),
        prefix: '',
        suffix: ' 单',
        icon: 'CircleCheck',
        color: '#E6A23C',
        note: `订单完成率 ${farmerCompletionRate.value}%`
      },
      {
        title: '低库存预警',
        value: formatNumber(farmerLowStockCount.value),
        prefix: '',
        suffix: ' 件',
        icon: 'Warning',
        color: '#F56C6C',
        note: '库存小于等于 20 的商品'
      }
    ]
  }

  return [
    {
      title: '累计销售额',
      value: formatNumber(adminSummary.value.totalSales, 2),
      prefix: '¥ ',
      suffix: '',
      icon: 'Money',
      color: '#409EFF',
      note: '平台已支付订单成交额'
    },
    {
      title: '总订单量',
      value: formatNumber(adminSummary.value.totalOrders),
      prefix: '',
      suffix: ' 单',
      icon: 'ShoppingCart',
      color: '#67C23A',
      note: '平台已支付订单数量'
    },
    {
      title: '农户总数',
      value: formatNumber(adminSummary.value.totalFarmers),
      prefix: '',
      suffix: ' 位',
      icon: 'User',
      color: '#E6A23C',
      note: '已入驻平台的农户账号'
    },
    {
      title: '在售商品',
      value: formatNumber(adminSummary.value.totalProducts),
      prefix: '',
      suffix: ' 件',
      icon: 'Goods',
      color: '#F56C6C',
      note: '审核通过且上架中的商品'
    }
  ]
})

function formatNumber(value, digits = 0) {
  const number = Number(value || 0)
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

function setChart(name, chartRef, option) {
  if (!chartRef.value) {
    return
  }
  if (!chartInstances[name]) {
    chartInstances[name] = echarts.init(chartRef.value)
  }
  chartInstances[name].setOption(option, true)
}

function renderAdminCharts() {
  setChart('sales', salesChartRef, {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: adminSalesCurve.value.map(item => item.statMonth)
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: adminSalesCurve.value.map(item => Number(item.salesAmount || 0)),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.45)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        itemStyle: { color: '#409EFF' }
      }
    ]
  })

  setChart('secondary', secondaryChartRef, {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center' },
    series: [
      {
        name: '分类占比',
        type: 'pie',
        radius: ['42%', '70%'],
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: adminCategoryDistribution.value.length > 0 ? adminCategoryDistribution.value : [{ value: 0, name: '暂无数据' }]
      }
    ]
  })

  setChart('tertiary', tertiaryChartRef, {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: adminStatusDistribution.value.map(item => item.name)
    },
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: adminStatusDistribution.value.map(item => Number(item.value || 0)),
        itemStyle: {
          color(params) {
            const colorList = ['#F56C6C', '#E6A23C', '#409EFF', '#67C23A', '#909399', '#DCDFE6', '#B5C334', '#FE8463']
            return colorList[params.dataIndex % colorList.length]
          },
          borderRadius: [0, 6, 6, 0]
        }
      }
    ]
  })
}

function renderFarmerCharts() {
  setChart('sales', salesChartRef, {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: farmerCurve.value.map(item => String(item.statDate || '').slice(5))
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        data: farmerCurve.value.map(item => Number(item.salesAmount || 0)),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.4)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        },
        itemStyle: { color: '#67C23A' }
      }
    ]
  })

  const hotProducts = farmerHotProducts.value.slice(0, 5)
  setChart('secondary', secondaryChartRef, {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '4%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: hotProducts.length > 0 ? hotProducts.map(item => item.productName) : ['暂无数据']
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        data: hotProducts.length > 0 ? hotProducts.map(item => Number(item.saleQty || 0)) : [0],
        itemStyle: {
          color: '#E6A23C',
          borderRadius: [0, 6, 6, 0]
        }
      }
    ]
  })

  const totalOrders = Number(farmerSummary.value.totalOrders || 0)
  const completedOrders = Number(farmerSummary.value.completedOrders || 0)
  const pendingOrders = Math.max(totalOrders - completedOrders, 0)
  const completionData = totalOrders > 0
    ? [
        { value: completedOrders, name: '已完成' },
        { value: pendingOrders, name: '处理中' }
      ]
    : [{ value: 1, name: '暂无订单' }]

  setChart('tertiary', tertiaryChartRef, {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center' },
    series: [
      {
        name: '订单情况',
        type: 'pie',
        radius: ['45%', '72%'],
        label: {
          formatter: totalOrders > 0 ? '{b}\n{d}%' : '{b}'
        },
        data: completionData,
        color: totalOrders > 0 ? ['#67C23A', '#409EFF'] : ['#DCDFE6']
      }
    ]
  })
}

async function loadDashboard() {
  if (isFarmer.value) {
    const res = await getFarmerStat({ period: farmerPeriod.value })
    const data = res.data || {}
    farmerSummary.value = {
      totalOrders: Number(data.summary?.totalOrders || 0),
      totalSales: Number(data.summary?.totalSales || 0),
      completedOrders: Number(data.summary?.completedOrders || 0)
    }
    farmerCurve.value = data.curve || []
    farmerHotProducts.value = data.hotProducts || []
    farmerLowStockCount.value = Number(data.lowStockCount || 0)
    await nextTick()
    renderFarmerCharts()
    return
  }

  const res = await getAdminStat()
  const data = res.data || {}
  adminSummary.value = {
    totalSales: Number(data.summary?.totalSales || 0),
    totalOrders: Number(data.summary?.totalOrders || 0),
    totalFarmers: Number(data.summary?.totalFarmers || 0),
    totalProducts: Number(data.summary?.totalProducts || 0)
  }
  adminSalesCurve.value = data.salesCurve || []
  adminCategoryDistribution.value = data.categoryDistribution || []
  adminStatusDistribution.value = data.statusDistribution || []
  adminNotices.value = data.notices || []
  await nextTick()
  renderAdminCharts()
}

function handleResize() {
  Object.values(chartInstances).forEach(chart => chart?.resize())
}

function disposeCharts() {
  Object.keys(chartInstances).forEach(key => {
    chartInstances[key]?.dispose()
    chartInstances[key] = null
  })
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.dashboard-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.welcome-text {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.header-desc {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-date {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-info {
  min-width: 0;
}

.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  line-height: 1.2;
  color: #303133;
  font-weight: 700;
}

.stat-footer {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}

.chart-row {
  margin-bottom: 20px;
}

.bottom-row {
  margin-bottom: 0;
}

.chart {
  width: 100%;
}

.chart-lg {
  height: 350px;
}

.chart-md {
  height: 300px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice-card {
  height: 100%;
}

.notice-list {
  min-height: 300px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-tag {
  flex-shrink: 0;
}

.notice-title {
  flex: 1;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  flex-shrink: 0;
  color: #909399;
  font-size: 12px;
}

.hot-table {
  min-height: 300px;
}

@media (max-width: 992px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }
}
</style>
