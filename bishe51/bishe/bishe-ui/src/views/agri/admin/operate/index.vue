<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="运费模板" name="freight">
          <el-button type="primary" @click="openFreight()">新增模板</el-button>
          <el-alert class="mt12 freight-guide" :closable="false" type="info">
            <template #title>运费计算说明</template>
            <div>1. 系统会根据买家收货地址的“省份”自动识别区域（华东、华中等）。</div>
            <div>2. 匹配到模板后按规则计算：单笔订单运费 = 首费 + (商品件数 - 1) * 续费。</div>
            <div>3. 一个订单里每个农户会拆成独立子单，运费按子单分别计算后汇总。</div>
          </el-alert>
          <el-table :data="freightList" border class="mt12">
            <el-table-column prop="templateName" label="模板名称" min-width="160" />
            <el-table-column label="适用地区" min-width="220">
              <template #default="scope">
                <el-tag effect="light">{{ freightRegionText(scope.row.regionCode) }}</el-tag>
                <span class="region-code">{{ scope.row.regionCode || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="运费规则" min-width="220">
              <template #default="scope">{{ freightRuleText(scope.row) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="freightStatusType(scope.row.status)">
                  {{ freightStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="scope">
                <el-button link type="primary" @click="openFreight(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="removeFreight(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="优惠券" name="coupon">
          <el-button type="primary" @click="openCoupon()">新增优惠券</el-button>
          <el-table :data="couponList" border class="mt12">
            <el-table-column prop="couponName" label="名称" min-width="150" />
            <el-table-column prop="couponType" label="类型" width="90" />
            <el-table-column prop="thresholdAmount" label="门槛" width="90" />
            <el-table-column prop="discountAmount" label="减免" width="90" />
            <el-table-column prop="discountRate" label="折扣率" width="90" />
            <el-table-column prop="remainCount" label="剩余" width="90" />
            <el-table-column prop="totalCount" label="总量" width="90" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button link type="success" @click="openIssue(scope.row)">发放</el-button>
                <el-button link type="primary" @click="openCoupon(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="removeCouponRow(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="促销活动" name="promotion">
          <el-button type="primary" @click="openPromotion()">新增活动</el-button>
          <el-table :data="promotionList" border class="mt12">
            <el-table-column prop="promotionName" label="活动名称" min-width="150" />
            <el-table-column label="类型" width="90">
              <template #default="scope">
                {{ promotionTypeText(scope.row.promotionType) }}
              </template>
            </el-table-column>
            <el-table-column label="开始时间" width="180">
              <template #default="scope">{{ formatDateTime(scope.row.startTime) }}</template>
            </el-table-column>
            <el-table-column label="结束时间" width="180">
              <template #default="scope">{{ formatDateTime(scope.row.endTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="scope">
                <el-button link type="primary" @click="openPromotion(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="removePromotionRow(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="freightOpen" title="运费模板" width="620px">
      <el-form :model="freightForm" label-width="100px">
        <el-form-item label="模板名称">
          <el-select
            v-model="freightForm.templateName"
            filterable
            clearable
            placeholder="请选择模板名称"
            style="width: 100%"
          >
            <el-option
              v-for="item in agri_freight_template_name"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="地区编码">
          <el-select
            v-model="freightForm.regionCode"
            filterable
            clearable
            placeholder="请选择地区编码"
            style="width: 100%"
          >
            <el-option
              v-for="item in agri_freight_region_code"
              :key="item.value"
              :label="`${item.label}（${item.value}）`"
              :value="item.value"
            />
          </el-select>
          <div class="form-tip">建议使用平台预设编码，避免买家地址匹配不到模板。</div>
        </el-form-item>
        <el-form-item label="首费(元)">
          <el-input-number v-model="freightForm.firstFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续费(元/件)">
          <el-input-number v-model="freightForm.extraFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="freightForm.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="freightForm.remark" type="textarea" :rows="2" maxlength="120" show-word-limit placeholder="可选：例如包含的省份说明" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="freightOpen=false">取消</el-button><el-button type="primary" @click="saveFreight">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="couponOpen" title="优惠券" width="620px">
      <el-form :model="couponForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="couponForm.couponName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="couponForm.couponType"><el-option value="1" label="满减" /><el-option value="2" label="折扣" /></el-select></el-form-item>
        <el-form-item label="门槛"><el-input-number v-model="couponForm.thresholdAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="减免"><el-input-number v-model="couponForm.discountAmount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="折扣率"><el-input-number v-model="couponForm.discountRate" :min="0" :max="10" :precision="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="couponOpen=false">取消</el-button><el-button type="primary" @click="saveCouponForm">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="issueOpen" title="发放优惠券" width="680px">
      <el-form :model="issueForm" label-width="90px">
        <el-form-item label="优惠券">
          <el-tag type="success">{{ issueTarget.couponName || '-' }}</el-tag>
          <span class="issue-hint">剩余 {{ issueTarget.remainCount || 0 }} 张</span>
        </el-form-item>
        <el-form-item label="发放用户">
          <el-select
            v-model="issueForm.userIds"
            multiple
            filterable
            remote
            reserve-keyword
            :remote-method="searchUsers"
            :loading="userLoading"
            placeholder="输入昵称/账号搜索并选择用户"
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.userId"
              :label="`${user.nickName || user.userName}（${user.userName}）`"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueOpen=false">取消</el-button>
        <el-button type="primary" :loading="issueLoading" @click="submitIssue">确认发放</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="promotionOpen" title="促销活动" width="620px">
      <el-form :model="promotionForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="promotionForm.promotionName" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="promotionForm.promotionType"><el-option value="1" label="限时购" /><el-option value="2" label="满减" /><el-option value="3" label="主题" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="promotionForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="promotionForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="promotionForm.content" type="textarea" rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="promotionOpen=false">取消</el-button><el-button type="primary" @click="savePromotionForm">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCurrentInstance, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUser } from '@/api/system/user'
import { parseTime } from '@/utils/ruoyi'
import {
  issueCoupon,
  listCoupons,
  listFreightTemplates,
  listPromotions,
  removeCoupon,
  removeFreightTemplate,
  removePromotion,
  saveCoupon,
  saveFreightTemplate,
  savePromotion
} from '@/api/agri/mall'

const { proxy } = getCurrentInstance()
const { agri_freight_template_name, agri_freight_region_code } = proxy.useDict('agri_freight_template_name', 'agri_freight_region_code')

const activeTab = ref('freight')
const freightList = ref([])
const couponList = ref([])
const promotionList = ref([])

const freightOpen = ref(false)
const couponOpen = ref(false)
const issueOpen = ref(false)
const promotionOpen = ref(false)
const issueLoading = ref(false)
const userLoading = ref(false)

const freightForm = ref({ templateId: null, templateName: '', regionCode: '', firstFee: 0, extraFee: 0, status: '0', remark: '' })
const couponForm = ref({ couponId: null, couponName: '', couponType: '1', thresholdAmount: 0, discountAmount: 0, discountRate: 0, status: '0' })
const promotionForm = ref({ promotionId: null, promotionName: '', promotionType: '1', startTime: '', endTime: '', content: '', status: '0' })
const issueTarget = ref({})
const issueForm = ref({ couponId: null, userIds: [] })
const userOptions = ref([])

function formatDateTime(value) {
  return parseTime(value) || '-'
}

function promotionTypeText(value) {
  const type = String(value ?? '')
  if (type === '1') return '限时购'
  if (type === '2') return '满减'
  if (type === '3') return '主题'
  return '-'
}

function loadAll() {
  listFreightTemplates({}).then(res => {
    freightList.value = (res.rows || []).map((row) => ({
      ...row,
      status: String(row.status ?? '0'),
      firstFee: Number(row.firstFee || 0),
      extraFee: Number(row.extraFee || 0)
    }))
  })
  listCoupons({}).then(res => { couponList.value = res.rows || [] })
  listPromotions({}).then(res => { promotionList.value = res.rows || [] })
}

function openFreight(row) {
  freightForm.value = row
    ? {
      templateId: row.templateId ?? null,
      templateName: row.templateName || '',
      regionCode: row.regionCode || '',
      firstFee: Number(row.firstFee || 0),
      extraFee: Number(row.extraFee || 0),
      status: String(row.status ?? '0'),
      remark: row.remark || ''
    }
    : { templateId: null, templateName: '', regionCode: '', firstFee: 0, extraFee: 0, status: '0', remark: '' }
  freightOpen.value = true
}

function saveFreight() {
  const templateName = String(freightForm.value.templateName || '').trim()
  const regionCode = String(freightForm.value.regionCode || '').trim().toUpperCase()
  if (!templateName) {
    ElMessage.warning('请选择模板名称')
    return
  }
  if (!regionCode) {
    ElMessage.warning('请选择地区编码')
    return
  }
  const payload = {
    ...freightForm.value,
    templateName,
    regionCode,
    status: String(freightForm.value.status ?? '0')
  }
  saveFreightTemplate(payload).then(() => {
    ElMessage.success('保存成功')
    freightOpen.value = false
    loadAll()
  })
}

function freightRegionText(regionCode) {
  const code = String(regionCode || '').trim().toUpperCase()
  const hit = (agri_freight_region_code.value || []).find(item => String(item.value || '').trim().toUpperCase() === code)
  return hit ? hit.label : '自定义地区'
}

function freightRuleText(row) {
  return `首件 ￥${Number(row.firstFee || 0).toFixed(2)}，续件每件 ￥${Number(row.extraFee || 0).toFixed(2)}`
}

function freightStatusText(status) {
  return String(status) === '0' ? '启用' : '停用'
}

function freightStatusType(status) {
  return String(status) === '0' ? 'success' : 'info'
}

function removeFreight(row) {
  ElMessageBox.confirm('确认删除该模板？').then(() => {
    removeFreightTemplate(row.templateId).then(() => {
      ElMessage.success('删除成功')
      loadAll()
    })
  })
}

function openCoupon(row) {
  couponForm.value = row ? { ...row } : { couponId: null, couponName: '', couponType: '1', thresholdAmount: 0, discountAmount: 0, discountRate: 0, status: '0' }
  couponOpen.value = true
}

function saveCouponForm() {
  saveCoupon(couponForm.value).then(() => {
    ElMessage.success('保存成功')
    couponOpen.value = false
    loadAll()
  })
}

function removeCouponRow(row) {
  ElMessageBox.confirm('确认删除该优惠券？').then(() => {
    removeCoupon(row.couponId).then(() => {
      ElMessage.success('删除成功')
      loadAll()
    })
  })
}

function openIssue(row) {
  issueTarget.value = { ...row }
  issueForm.value = { couponId: row.couponId, userIds: [] }
  userOptions.value = []
  issueOpen.value = true
  searchUsers('')
}

function searchUsers(keyword) {
  userLoading.value = true
  listUser({ pageNum: 1, pageSize: 50, userName: keyword || '' }).then(res => {
    userOptions.value = res.rows || []
  }).finally(() => {
    userLoading.value = false
  })
}

function submitIssue() {
  if (!issueForm.value.userIds.length) {
    ElMessage.warning('请选择发放用户')
    return
  }
  issueLoading.value = true
  issueCoupon(issueForm.value).then(() => {
    ElMessage.success('发放成功')
    issueOpen.value = false
    loadAll()
  }).finally(() => {
    issueLoading.value = false
  })
}

function openPromotion(row) {
  promotionForm.value = row ? { ...row } : { promotionId: null, promotionName: '', promotionType: '1', startTime: '', endTime: '', content: '', status: '0' }
  promotionOpen.value = true
}

function savePromotionForm() {
  savePromotion(promotionForm.value).then(() => {
    ElMessage.success('保存成功')
    promotionOpen.value = false
    loadAll()
  })
}

function removePromotionRow(row) {
  ElMessageBox.confirm('确认删除该活动？').then(() => {
    removePromotion(row.promotionId).then(() => {
      ElMessage.success('删除成功')
      loadAll()
    })
  })
}

loadAll()
</script>

<style scoped>
.mt12 { margin-top: 12px; }
.issue-hint { margin-left: 12px; color: #64748b; }
.freight-guide { line-height: 1.9; }
.region-code { margin-left: 8px; color: #94a3b8; font-size: 12px; }
.form-tip { font-size: 12px; color: #94a3b8; line-height: 1.6; margin-top: 4px; }
</style>
