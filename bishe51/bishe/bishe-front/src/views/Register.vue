<template>
  <div class="register-page">
    <div class="left-panel">
      <div class="left-content">
        <div class="logo"><el-icon><Shop /></el-icon></div>
        <h1>加入我们</h1>
        <p>创建账号后即可体验咨询、下单、订单追踪和评价互动。</p>
        <div class="tags">
          <span><el-icon><SuccessFilled /></el-icon> 免费注册</span>
          <span><el-icon><SuccessFilled /></el-icon> 快速开通</span>
          <span><el-icon><SuccessFilled /></el-icon> 服务完整</span>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="card">
        <div class="head">
          <h2>账号注册</h2>
          <p>请填写下方信息</p>
        </div>

        <el-form :model="form" label-position="top">
          <el-form-item label="账号">
            <el-input v-model="form.username" :prefix-icon="User" placeholder="请输入账号" autocomplete="username" />
          </el-form-item>
          <el-form-item label="设置密码">
            <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password placeholder="请输入密码" autocomplete="new-password" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="form.confirmPassword" :prefix-icon="Lock" type="password" show-password placeholder="请再次输入密码" autocomplete="new-password" />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="captcha-row">
              <el-input v-model="form.code" :prefix-icon="Warning" placeholder="请输入验证码" />
              <div class="captcha-box">
                <img :src="captcha" @click="loadCaptcha" alt="captcha" />
              </div>
            </div>
          </el-form-item>
        </el-form>

        <el-button type="primary" class="submit" :loading="loading" @click="handleRegister">提交注册</el-button>

        <div class="foot">已有账号？<router-link to="/login">立即登录</router-link></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Warning, Shop, SuccessFilled } from '@element-plus/icons-vue'
import { showFailToast, showSuccessToast } from '@/utils/notify'
import { getCaptcha, register } from '@/api/auth'

const router = useRouter()
const captcha = ref('')
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  code: '',
  uuid: ''
})

function loadCaptcha() {
  getCaptcha().then(res => {
    form.uuid = res.uuid
    captcha.value = 'data:image/gif;base64,' + res.img
  })
}

async function handleRegister() {
  if (!form.username) return showFailToast('请输入账号')
  if (!form.password) return showFailToast('请输入密码')
  if (form.password !== form.confirmPassword) return showFailToast('两次输入的密码不一致')
  if (!form.code) return showFailToast('请输入验证码')

  loading.value = true
  try {
    await register(form)
    showSuccessToast('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    showFailToast(e.message || '注册失败')
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
}

.left-panel {
  background: linear-gradient(140deg, #164e63, #0f172a);
  color: #fff;
  display: grid;
  place-items: center;
  padding: 60px;
}

.left-content {
  max-width: 520px;
}

.logo {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.12);
  display: grid;
  place-items: center;
  font-size: 34px;
  margin-bottom: 20px;
}

.left-content h1 {
  margin: 0;
  font-size: 44px;
  font-weight: 900;
}

.left-content p {
  margin: 16px 0 24px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.8;
}

.tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tags span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.18);
  background: rgba(255,255,255,0.08);
}

.right-panel {
  background: #f8fafc;
  display: grid;
  place-items: center;
  padding: 40px;
}

.card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 24px;
  padding: 38px;
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.05);
}

.head {
  text-align: center;
  margin-bottom: 16px;
}

.head h2 {
  margin: 0;
  font-size: 30px;
  font-weight: 900;
}

.head p {
  margin: 6px 0 0;
  color: #64748b;
}

:deep(.el-form-item) {
  margin-bottom: 14px;
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 10px;
}

.captcha-box {
  height: 40px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
}

.captcha-box img {
  height: 100%;
  width: 100%;
  object-fit: cover;
}

.submit {
  width: 100%;
  height: 48px;
  margin-top: 4px;
}

.foot {
  margin-top: 18px;
  text-align: center;
  color: #64748b;
}

.foot a {
  text-decoration: none;
  color: #0ea5b7;
  font-weight: 700;
}
</style>
