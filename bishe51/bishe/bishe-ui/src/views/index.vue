<template>
  <div class="dashboard-container home">
    <div class="dashboard-header">
      <h2 class="welcome-text">{{ dashboardTitle }}</h2>
      <div class="current-date">{{ currentDate }}</div>
    </div>

    <template v-if="dashboardMode !== 'basic'">
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
                <div class="stat-subtitle">{{ item.subtitle }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :span="16">
          <el-card shadow="hover" :header="salesChartTitle">
            <div ref="salesChartRef" style="height: 350px"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" :header="categoryChartTitle">
            <div ref="categoryChartRef" style="height: 350px"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row bottom-row">
        <el-col :span="12">
          <el-card shadow="hover" :header="statusChartTitle">
            <div ref="statusChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="notice-card">
            <template #header>
              <div class="card-header">
                <span>{{ noticePanelTitle }}</span>
                <el-button
                  v-if="dashboardMode === 'admin'"
                  class="button"
                  text
                  @click="$router.push('/system/notice')"
                >
                  查看更多
                </el-button>
              </div>
            </template>
            <div class="notice-list">
              <div v-for="(notice, index) in notices" :key="index" class="notice-item">
                <el-tag :type="notice.type" size="small" class="notice-tag">{{ notice.tag }}</el-tag>
                <span class="notice-title">{{ notice.title }}</span>
                <span class="notice-time">{{ notice.time }}</span>
              </div>
              <el-empty v-if="!notices.length" description="暂无数据" :image-size="60" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <el-card v-else shadow="hover" class="empty-card">
      <el-empty description="当前账号暂无首页统计权限，请从左侧菜单进入对应业务模块。" :image-size="72" />
    </el-card>
  </div>
</template>

<script setup name="Index">
import * as echarts from 'echarts'
import auth from '@/plugins/auth'
import { getAdminStat, getFarmerStat } from '@/api/agri/stat'

const adminDashboardPerms = [
  'agri:admin:farmer:list',
  'agri:admin:product:list',
  'agri:admin:order:list',
  'agri:admin:dispute:list',
  'agri:admin:operate:list',
  'agri:admin:notice:list',
  'agri:admin:review:list',
  'agri:admin:ai:list'
]

const dashboardMode = computed(() => {
  if (auth.hasPermiOr(adminDashboardPerms)) {
    return 'admin'
  }
  if (auth.hasPermi('agri:farmer:stat:list')) {
    return 'farmer'
  }
  return 'basic'
})

const dashboardTitle = computed(() => {
  if (dashboardMode.value === 'admin') {
    return '农产品销售管理系统管理概览'
  }
  if (dashboardMode.value === 'farmer') {
    return '农户经营概览'
  }
  return '工作台'
})

const salesChartTitle = computed(() => {
  return dashboardMode.value === 'admin' ? '近六个月销售额走势' : '本月销售额走势'
})

const categoryChartTitle = computed(() => {
  return dashboardMode.value === 'admin' ? '农产品分类占比' : '热销商品占比'
})

const statusChartTitle = computed(() => {
  return dashboardMode.value === 'admin' ? '订单状态分布' : '订单完成情况'
})

const noticePanelTitle = computed(() => {
  return dashboardMode.value === 'admin' ? '系统公告 / 最新动态' : '热销商品动态'
})

const currentDate = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const statistics = ref([])
const notices = ref([])

const salesChartRef = ref(null)
const categoryChartRef = ref(null)
const statusChartRef = ref(null)

let salesChart = null
let categoryChart = null
let statusChart = null

function formatAmount(value) {
  const amount = Number(value || 0)
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: amount % 1 === 0 ? 0 : 2,
    maximumFractionDigits: 2
  })
}

function formatCount(value) {
  return Number(value || 0).toLocaleString('zh-CN')
}

function ensureChart(elRef, chartInstance) {
  if (!elRef.value) {
    return null
  }
  if (chartInstance) {
    return chartInstance
  }
  return echarts.init(elRef.value)
}

function renderSalesChart(xAxisData, seriesData) {
  salesChart = ensureChart(salesChartRef, salesChart)
  if (!salesChart) {
    return
  }
  salesChart.setOption(
    {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xAxisData
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '销售额',
          type: 'line',
          smooth: true,
          data: seriesData,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.45)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.04)' }
            ])
          },
          itemStyle: { color: '#409EFF' }
        }
      ]
    },
    true
  )
}

function renderCategoryChart(data) {
  categoryChart = ensureChart(categoryChartRef, categoryChart)
  if (!categoryChart) {
    return
  }
  categoryChart.setOption(
    {
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
          labelLine: { show: false },
          data: data.length ? data : [{ value: 0, name: '暂无数据' }]
        }
      ]
    },
    true
  )
}

function renderStatusChart(data) {
  statusChart = ensureChart(statusChartRef, statusChart)
  if (!statusChart) {
    return
  }
  statusChart.setOption(
    {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'value',
        boundaryGap: [0, 0.01]
      },
      yAxis: {
        type: 'category',
        data: data.map((item) => item.name)
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          data: data.map((item) => item.value),
          itemStyle: {
            color(params) {
              const colorList = ['#F56C6C', '#E6A23C', '#409EFF', '#67C23A', '#909399', '#B5C334']
              return colorList[params.dataIndex % colorList.length]
            },
            borderRadius: [0, 5, 5, 0]
          }
        }
      ]
    },
    true
  )
}

function resetDashboard() {
  statistics.value = []
  notices.value = []
}

function applyAdminDashboard(data = {}) {
  const summary = data.summary || {}
  const categoryDistribution = data.categoryDistribution || []
  const statusDistribution = data.statusDistribution || []
  const salesCurve = data.salesCurve || []
  const latestNotices = data.notices || []

  statistics.value = [
    { title: '累计销售额', value: formatAmount(summary.totalSales), prefix: '¥ ', suffix: '', icon: 'Money', color: '#409EFF', subtitle: '已支付订单累计金额' },
    { title: '总订单量', value: formatCount(summary.totalOrders), prefix: '', suffix: ' 单', icon: 'ShoppingCart', color: '#67C23A', subtitle: '商城累计订单数' },
    { title: '农户总数', value: formatCount(summary.totalFarmers), prefix: '', suffix: ' 位', icon: 'User', color: '#E6A23C', subtitle: '已通过审核农户' },
    { title: '在售商品', value: formatCount(summary.totalProducts), prefix: '', suffix: ' 件', icon: 'Goods', color: '#F56C6C', subtitle: '当前上架商品数' }
  ]

  notices.value = latestNotices
  renderSalesChart(
    salesCurve.map((item) => item.statMonth),
    salesCurve.map((item) => Number(item.salesAmount || 0))
  )
  renderCategoryChart(categoryDistribution)
  renderStatusChart(statusDistribution)
}

function applyFarmerDashboard(data = {}) {
  const summary = data.summary || {}
  const curve = data.curve || []
  const hotProducts = (data.hotProducts || []).slice(0, 5)
  const lowStockCount = Number(data.lowStockCount || 0)
  const totalOrders = Number(summary.totalOrders || 0)
  const completedOrders = Number(summary.completedOrders || 0)

  statistics.value = [
    { title: '本月销售额', value: formatAmount(summary.totalSales), prefix: '¥ ', suffix: '', icon: 'Money', color: '#409EFF', subtitle: '本月已完成交易金额' },
    { title: '本月订单量', value: formatCount(totalOrders), prefix: '', suffix: ' 单', icon: 'ShoppingCart', color: '#67C23A', subtitle: '本月产生订单数' },
    { title: '已完成订单', value: formatCount(completedOrders), prefix: '', suffix: ' 单', icon: 'Checked', color: '#E6A23C', subtitle: '已确认完成订单数' },
    { title: '低库存预警', value: formatCount(lowStockCount), prefix: '', suffix: ' 件', icon: 'Warning', color: '#F56C6C', subtitle: '库存低于 20 的商品数' }
  ]

  notices.value = hotProducts.map((item) => ({
    tag: '热销',
    title: item.productName || '未命名商品',
    time: `销量 ${formatCount(item.saleQty)}`,
    type: 'danger'
  }))

  renderSalesChart(
    curve.map((item) => String(item.statDate || '').slice(5)),
    curve.map((item) => Number(item.salesAmount || 0))
  )
  renderCategoryChart(
    hotProducts.map((item) => ({
      name: item.productName || '未命名商品',
      value: Number(item.saleAmount || item.saleQty || 0)
    }))
  )
  renderStatusChart([
    { name: '已完成', value: completedOrders },
    { name: '待处理', value: Math.max(totalOrders - completedOrders, 0) }
  ])
}

async function loadDashboard() {
  resetDashboard()
  if (dashboardMode.value === 'admin') {
    const res = await getAdminStat()
    applyAdminDashboard(res.data || {})
    return
  }
  if (dashboardMode.value === 'farmer') {
    const res = await getFarmerStat({ period: 'month' })
    applyFarmerDashboard(res.data || {})
  }
}

function handleResize() {
  salesChart?.resize()
  categoryChart?.resize()
  statusChart?.resize()
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  categoryChart?.dispose()
  statusChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);

  .dashboard-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;

    .welcome-text {
      font-size: 24px;
      font-weight: 700;
      color: #303133;
      margin: 0;
    }

    .current-date {
      color: #606266;
      font-size: 14px;
    }
  }

  .stat-cards {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30px;
      color: #fff;
      margin-right: 15px;
      flex-shrink: 0;
    }

    .stat-info {
      flex: 1;
      min-width: 0;
    }

    .stat-title {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 6px;
      line-height: 1.2;
    }

    .stat-subtitle {
      font-size: 12px;
      color: #909399;
    }
  }

  .chart-row {
    margin-bottom: 20px;
  }

  .bottom-row {
    .notice-card {
      height: 360px;
      overflow-y: auto;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .notice-list {
      .notice-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #ebeef5;

        &:last-child {
          border-bottom: none;
        }
      }

      .notice-tag {
        margin-right: 10px;
        flex-shrink: 0;
      }

      .notice-title {
        flex: 1;
        font-size: 14px;
        color: #606266;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .notice-time {
        font-size: 12px;
        color: #909399;
        margin-left: 10px;
        flex-shrink: 0;
      }
    }
  }

  .empty-card {
    min-height: 420px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
