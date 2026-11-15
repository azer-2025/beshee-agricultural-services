<template>
  <div class="profile-page">
    <div class="profile-grid">
      <section class="card-modern sidebar">
        <div class="avatar-wrap">
          <img :src="avatarUrl" class="avatar" alt="avatar" />
          <el-upload
            class="upload-btn"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
            accept="image/*"
          >
            <el-button size="small" type="primary" plain>更换头像</el-button>
          </el-upload>
        </div>

        <h3>{{ form.nickName || '未设置昵称' }}</h3>
        <p>普通消费者</p>

        <div class="stats">
          <div><b>{{ mallStore.cartCount || 0 }}</b><span>购物车</span></div>
          <div><b>{{ mallStore.reviewPendingCount || 0 }}</b><span>待评价</span></div>
        </div>
      </section>

      <section class="card-modern content">
        <h2>基本资料</h2>
        <p class="sub">维护个人信息以便接收订单通知</p>

        <el-form label-width="90px" class="form-grid">
          <el-form-item label="用户昵称">
            <el-input v-model="form.nickName" placeholder="请设置昵称" />
          </el-form-item>
          <el-form-item label="联系邮箱">
            <el-input v-model="form.email" placeholder="example@mail.com" />
          </el-form-item>
          <el-form-item label="手机号码">
            <el-input v-model="form.phonenumber" placeholder="请输入11位手机号" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.sex">
              <el-radio value="0">男</el-radio>
              <el-radio value="1">女</el-radio>
              <el-radio value="2">保密</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <div class="actions">
          <el-button type="primary" @click="save">保存资料</el-button>
          <span>最后修改：{{ lastUpdateTime || '暂无记录' }}</span>
        </div>
      </section>

      <div class="spacer"></div>

      <section class="card-modern content address-section">
        <div class="header-with-action">
          <h2>收货地址</h2>
          <el-button type="primary" plain @click="handleAddAddress">新增地址</el-button>
        </div>
        <p class="sub">管理您的常用收货地址，结算更快捷</p>

        <div class="address-list">
          <div v-for="addr in addressList" :key="addr.addressId" class="address-item">
            <div class="addr-info">
              <div class="name-phone">
                <span class="name">{{ addr.receiverName }}</span>
                <span class="phone">{{ addr.receiverPhone }}</span>
                <el-tag v-if="addr.isDefault === '1'" size="small" type="danger" effect="dark" round>默认</el-tag>
              </div>
              <div class="detail">{{ addr.receiverAddress }}</div>
            </div>
            <div class="addr-actions">
              <el-button link type="primary" @click="handleEditAddress(addr)">编辑</el-button>
              <el-button link type="danger" @click="handleRemoveAddress(addr.addressId)">删除</el-button>
              <el-button v-if="addr.isDefault === '0'" link @click="handleSetDefault(addr.addressId)">设为默认</el-button>
            </div>
          </div>
          <el-empty v-if="!addressList.length" :image-size="60" description="暂无收货地址" />
        </div>
      </section>
    </div>

    <!-- Address Dialog -->
    <el-dialog 
      v-model="addrDialogOpen" 
      :show-close="false"
      width="540px" 
      class="premium-dialog"
      append-to-body
    >
      <template #header>
        <div class="p-dialog-header">
          <div class="p-dialog-title-wrap">
            <h3 class="p-dialog-title">{{ addrForm.addressId ? '修改收货地址' : '添加新收货地址' }}</h3>
            <p class="p-dialog-subtitle">请确保填写准确的派送信息以保证顺利收货</p>
          </div>
        </div>
      </template>

      <div class="p-dialog-body">
        <el-form :model="addrForm" label-position="top" class="premium-el-form">
          <div class="form-row-split">
            <el-form-item label="收货人" class="flex-1">
              <el-input v-model="addrForm.receiverName" placeholder="收货人真实姓名" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="联系电话" class="flex-1">
              <el-input v-model="addrForm.receiverPhone" placeholder="11位手机号码" maxlength="11" prefix-icon="Phone" />
            </el-form-item>
          </div>

          <el-form-item label="省市区">
            <el-cascader
              v-model="selectedRegion"
              :options="regionOptions"
              :props="regionCascaderProps"
              clearable
              filterable
              placeholder="请选择省/市/区(县)"
            />
          </el-form-item>

          <el-form-item label="详细收货地址">
            <el-input
              v-model="addressDetail"
              type="textarea"
              :rows="3"
              placeholder="街道、楼牌号等详细信息"
            />
          </el-form-item>

          <div class="m-toggle-row-premium">
            <div class="toggle-text">
              <div class="main">设为默认地址</div>
              <div class="sub">结算时将优先选中此地址</div>
            </div>
            <el-switch v-model="addrForm.isDefault" active-value="1" inactive-value="0" />
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="p-dialog-footer">
          <el-button @click="addrDialogOpen = false" round>取消返回</el-button>
          <el-button type="primary" @click="submitAddress" round class="btn-submit-premium">
            {{ addrForm.addressId ? '保存地址修改' : '确认添加地址' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, watch, ref, onMounted } from 'vue'
import { showFailToast, showSuccessToast } from '@/utils/notify'
import { uploadAvatar, updateProfile } from '@/api/auth'
import { listAddresses, saveAddress, removeAddress, setDefaultAddress } from '@/api/mall'
import { useUserStore } from '@/store/user'
import { useMallStore } from '@/store/mall'
import { resolveMediaUrl } from '@/utils/media'
import { areaList } from '@vant/area-data'
import defaultAvatar from '@/assets/profile.jpg'

const userStore = useUserStore()
const mallStore = useMallStore()
const form = reactive({ nickName: '', email: '', phonenumber: '', sex: '2' })
const lastUpdateTime = ref(new Date().toLocaleString())

const addressList = ref([])
const addrDialogOpen = ref(false)
const addrForm = reactive({ addressId: null, receiverName: '', receiverPhone: '', receiverAddress: '', isDefault: '0' })
const selectedRegion = ref([])
const addressDetail = ref('')
const regionOptions = buildRegionOptions(areaList)
const codeToText = {
  ...(areaList.province_list || {}),
  ...(areaList.city_list || {}),
  ...(areaList.county_list || {})
}
const regionCascaderProps = { value: 'value', label: 'label', children: 'children', emitPath: true }
const regionCandidates = buildRegionCandidates(regionOptions)

watch(() => userStore.userInfo, (val) => {
  if (!val) return
  form.nickName = val.nickName || ''
  form.email = val.email || ''
  form.phonenumber = val.phonenumber || ''
  form.sex = val.sex || '2'
}, { immediate: true, deep: true })

const avatarUrl = computed(() => resolveMediaUrl(userStore.userInfo.avatar) || defaultAvatar)

async function handleAvatarChange(uploadFile) {
  if (!uploadFile?.raw) return
  try {
    const fd = new FormData()
    fd.append('avatarfile', uploadFile.raw)
    const res = await uploadAvatar(fd)
    userStore.userInfo.avatar = res.imgUrl
    showSuccessToast('头像更新成功')
  } catch (e) {
    showFailToast(e.message || '头像上传失败')
  }
}

async function save() {
  try {
    await updateProfile(form)
    await userStore.fetchUserInfo()
    lastUpdateTime.value = new Date().toLocaleString()
    showSuccessToast('保存成功')
  } catch (e) {
    showFailToast(e.message || '保存失败')
  }
}

async function loadAddresses() {
  try {
    const res = await listAddresses()
    addressList.value = res.data || []
  } catch (e) {
    showFailToast(e.message || '加载地址失败')
  }
}

function handleAddAddress() {
  Object.assign(addrForm, { addressId: null, receiverName: '', receiverPhone: '', receiverAddress: '', isDefault: '0' })
  selectedRegion.value = []
  addressDetail.value = ''
  addrDialogOpen.value = true
}

function handleEditAddress(addr) {
  Object.assign(addrForm, addr)
  const parsed = splitReceiverAddress(addr.receiverAddress)
  selectedRegion.value = parsed.codes
  addressDetail.value = parsed.detail
  addrDialogOpen.value = true
}

async function submitAddress() {
  if (!addrForm.receiverName || !addrForm.receiverPhone) {
    showFailToast('请完善地址信息')
    return
  }
  if (!selectedRegion.value.length || selectedRegion.value.length !== 3) {
    showFailToast('请选择省市区')
    return
  }
  if (!String(addressDetail.value || '').trim()) {
    showFailToast('请填写详细收货地址')
    return
  }
  const regionText = selectedRegion.value.map(code => codeToText[String(code)]).filter(Boolean)
  if (regionText.length !== 3) {
    showFailToast('省市区选择无效，请重新选择')
    return
  }
  addrForm.receiverAddress = `${regionText.join(' ')} ${String(addressDetail.value).trim()}`
  try {
    await saveAddress(addrForm)
    showSuccessToast(addrForm.addressId ? '更新成功' : '新增成功')
    addrDialogOpen.value = false
    loadAddresses()
  } catch (e) {
    showFailToast(e.message || '提交失败')
  }
}

async function handleRemoveAddress(id) {
  try {
    await removeAddress(id)
    showSuccessToast('删除成功')
    loadAddresses()
  } catch (e) {
    showFailToast(e.message || '删除失败')
  }
}

async function handleSetDefault(id) {
  try {
    await setDefaultAddress(id)
    showSuccessToast('设置默认地址成功')
    loadAddresses()
  } catch (e) {
    showFailToast(e.message || '设置失败')
  }
}

onMounted(() => {
  loadAddresses()
})

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
    if (!citiesByProvince.has(provincePrefix)) citiesByProvince.set(provincePrefix, [])
    citiesByProvince.get(provincePrefix).push({ code: cityCode, name: cityName })
  }

  for (const [countyCodeRaw, countyName] of countyList) {
    const countyCode = String(countyCodeRaw)
    const cityPrefix = countyCode.slice(0, 4)
    if (!countiesByCity.has(cityPrefix)) countiesByCity.set(cityPrefix, [])
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
      return { value: city.code, label: city.name, children: counties }
    })
    return { value: provinceCode, label: provinceName, children: cities }
  })
}

function splitReceiverAddress(fullAddress) {
  const source = String(fullAddress || '').trim().replace(/\s+/g, ' ')
  if (!source) return { codes: [], detail: '' }
  const compactSource = source.replace(/\s+/g, '')
  let matched = null
  for (const item of regionCandidates) {
    if (
      source === item.withSpace ||
      source.startsWith(`${item.withSpace} `) ||
      compactSource.startsWith(item.compact)
    ) {
      if (!matched || item.compact.length > matched.compact.length) matched = item
    }
  }
  if (!matched) return { codes: [], detail: source }

  let detail = source
  if (source.startsWith(matched.withSpace)) {
    detail = source.slice(matched.withSpace.length).trim()
  } else {
    detail = source
    for (const label of matched.labels) {
      if (detail.startsWith(label)) detail = detail.slice(label.length).trim()
    }
  }
  return { codes: matched.codes, detail }
}
</script>

<style scoped>
.profile-page {
  padding: 24px 0;
}

.profile-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
}

.sidebar {
  display: grid;
  gap: 12px;
  align-content: start;
}

.avatar-wrap {
  display: grid;
  justify-items: center;
  gap: 10px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f1f5f9;
}

.sidebar h3 {
  margin: 0;
  text-align: center;
  font-size: 22px;
}

.sidebar p {
  margin: 0;
  text-align: center;
  color: #64748b;
}

.stats {
  margin-top: 6px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.stats > div {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  text-align: center;
}

.stats b {
  display: block;
  font-size: 24px;
  color: #0f172a;
}

.stats span {
  color: #64748b;
  font-size: 12px;
}

.content h2 {
  margin: 0;
  font-size: 30px;
  font-weight: 900;
}

.header-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub {
  margin: 4px 0 16px;
  color: #64748b;
}

.form-grid {
  max-width: 700px;
}

.actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #64748b;
}

.spacer {
  grid-column: 2;
  height: 16px;
}

.address-section {
  grid-column: 2;
}

.address-list {
  display: grid;
  gap: 12px;
}

.address-item {
  padding: 16px;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;
}

.address-item:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.name-phone {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.name {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

.phone {
  color: #64748b;
  font-size: 14px;
}

.detail {
  font-size: 14px;
  color: #475569;
}

.addr-actions {
  display: flex;
  gap: 8px;
}

.addr-dialog-form {
  padding: 0 20px;
}

/* Premium Dialog Styling */
.p-dialog-header {
  padding: 24px 32px 0;
}

.p-dialog-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.p-dialog-subtitle {
  margin: 4px 0 0;
  font-size: 14px;
  color: #64748b;
}

.p-dialog-body {
  padding: 24px 32px;
}

.form-row-split {
  display: flex;
  gap: 20px;
}

.flex-1 {
  flex: 1;
}

.premium-el-form :deep(.el-form-item__label) {
  font-weight: 700;
  color: #475569;
  font-size: 13px;
  padding-bottom: 4px;
}

.premium-el-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 12px;
  padding: 4px 12px;
}

.premium-el-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #3b82f6 inset;
}

.m-toggle-row-premium {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  padding: 16px;
  border-radius: 16px;
  margin-top: 8px;
}

.toggle-text .main {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.toggle-text .sub {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}

.p-dialog-footer {
  padding: 0 32px 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-submit-premium {
  padding: 0 32px !important;
  font-weight: 700 !important;
}
</style>
