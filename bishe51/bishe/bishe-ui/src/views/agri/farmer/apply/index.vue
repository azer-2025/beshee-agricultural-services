<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>农户入驻申请</span>
          <el-tag>{{ statusLabel }}</el-tag>
        </div>
      </template>
      <el-form :model="form" label-width="120px" style="max-width: 720px">
        <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="农场名称"><el-input v-model="form.farmName" /></el-form-item>
        <el-form-item label="农场地址">
          <el-cascader
            v-model="selectedFarmRegion"
            :options="farmRegionOptions"
            :props="farmRegionCascaderProps"
            clearable
            filterable
            style="width: 100%"
            placeholder="请选择省/市/区(县)"
          />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input
            v-model="farmAddressDetail"
            type="textarea"
            rows="3"
            placeholder="请输入街道、门牌号等详细信息"
          />
        </el-form-item>
        <el-form-item label="资质图片">
          <ImageUpload v-model="form.qualificationImg" :limit="1" />
        </el-form-item>
        <el-form-item label="审核备注" v-if="form.auditRemark">
          <el-input v-model="form.auditRemark" type="textarea" rows="3" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { areaList } from '@vant/area-data'
import { getFarmerApply, submitFarmerApply } from '@/api/agri/mall'

const form = reactive({
  applyId: null,
  realName: '',
  idCard: '',
  phone: '',
  farmName: '',
  farmAddress: '',
  qualificationImg: '',
  status: '0',
  auditRemark: ''
})
const selectedFarmRegion = ref([])
const farmAddressDetail = ref('')
const farmRegionOptions = buildRegionOptions(areaList)
const farmCodeToText = {
  ...(areaList.province_list || {}),
  ...(areaList.city_list || {}),
  ...(areaList.county_list || {})
}
const farmRegionCascaderProps = { value: 'value', label: 'label', children: 'children', emitPath: true }
const farmRegionCandidates = buildRegionCandidates(farmRegionOptions)

const statusLabel = computed(() => {
  if (form.status === '1') return '已通过'
  if (form.status === '2') return '已驳回'
  return '待审核'
})

function loadApply() {
  getFarmerApply().then(res => {
    if (res.data) {
      Object.assign(form, res.data)
      const parsed = splitFarmAddress(form.farmAddress)
      selectedFarmRegion.value = parsed.codes
      farmAddressDetail.value = parsed.detail
    }
  })
}

function submit() {
  if (!selectedFarmRegion.value.length || selectedFarmRegion.value.length !== 3) {
    ElMessage.warning('请选择省市县')
    return
  }
  const regionText = selectedFarmRegion.value.map(code => farmCodeToText[String(code)]).filter(Boolean)
  if (regionText.length !== 3) {
    ElMessage.warning('省市县选择无效，请重新选择')
    return
  }
  const detail = String(farmAddressDetail.value || '').trim()
  form.farmAddress = detail ? `${regionText.join(' ')} ${detail}` : regionText.join(' ')
  submitFarmerApply(form).then(() => {
    ElMessage.success('申请已提交')
    loadApply()
  })
}

onMounted(loadApply)

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
    if (!citiesByProvince.has(provincePrefix)) {
      citiesByProvince.set(provincePrefix, [])
    }
    citiesByProvince.get(provincePrefix).push({ code: cityCode, name: cityName })
  }

  for (const [countyCodeRaw, countyName] of countyList) {
    const countyCode = String(countyCodeRaw)
    const cityPrefix = countyCode.slice(0, 4)
    if (!countiesByCity.has(cityPrefix)) {
      countiesByCity.set(cityPrefix, [])
    }
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
      return {
        value: city.code,
        label: city.name,
        children: counties
      }
    })
    return {
      value: provinceCode,
      label: provinceName,
      children: cities
    }
  })
}

function splitFarmAddress(fullAddress) {
  const source = String(fullAddress || '').trim().replace(/\s+/g, ' ')
  if (!source) {
    return { codes: [], detail: '' }
  }
  const compactSource = source.replace(/\s+/g, '')
  let matched = null
  for (const item of farmRegionCandidates) {
    if (
      source === item.withSpace ||
      source.startsWith(`${item.withSpace} `) ||
      compactSource.startsWith(item.compact)
    ) {
      if (!matched || item.compact.length > matched.compact.length) {
        matched = item
      }
    }
  }
  if (!matched) {
    return { codes: [], detail: source }
  }

  let detail = source
  if (source.startsWith(matched.withSpace)) {
    detail = source.slice(matched.withSpace.length).trim()
  } else {
    detail = source
    for (const label of matched.labels) {
      if (detail.startsWith(label)) {
        detail = detail.slice(label.length).trim()
      }
    }
  }
  return { codes: matched.codes, detail }
}
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
