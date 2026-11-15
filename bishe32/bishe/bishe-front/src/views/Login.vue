<template>
  <div class="login-container-web">
    <div class="login-side-info">
      <div class="info-content">
        <div class="brand-logo-main"><el-icon><Shop /></el-icon></div>
        <h1 class="brand-name-large">助农就业商城</h1>
        <p class="brand-description">
          连接城市与乡野，助力街道就业。我们提供咨询、优选农产品与订单全流程服务。
        </p>

        <div class="feature-pills">
          <div class="pill-item"><el-icon><SuccessFilled /></el-icon><span>专业咨询</span></div>
          <div class="pill-item"><el-icon><SuccessFilled /></el-icon><span>精选特产</span></div>
          <div class="pill-item"><el-icon><SuccessFilled /></el-icon><span>订单追踪</span></div>
        </div>
      </div>
      <div class="deco-circle circle-1"></div>
      <div class="deco-circle circle-2"></div>
    </div>

    <div class="login-side-form">
      <div class="login-form-card">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账号以继续</p>
        </div>

        <el-form :model="form" label-position="top" class="modern-inputs">
          <el-form-item label="账号">
            <el-input v-model="form.username" placeholder="请输入账号" autocomplete="username" :prefix-icon="User" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" :prefix-icon="Lock" show-password />
          </el-form-item>

          <el-form-item label="验证码">
            <div class="captcha-row">
              <el-input v-model="form.code" placeholder="请输入验证码" :prefix-icon="Warning" />
              <div class="captcha-box">
                <img :src="captcha" @click="loadCaptcha" alt="captcha" />
              </div>
            </div>
          </el-form-item>
        </el-form>

        <el-button
          type="primary"
          class="btn-login-premium"
          @click="submit"
          :loading="loading"
        >
          安全登录
        </el-button>

        <div class="login-footer-links">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Warning, Shop, SuccessFilled } from '@element-plus/icons-vue'
import { showFailToast } from '@/utils/notify'
import { getCaptcha } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const captcha = ref('')
const loading = ref(false)
const form = reactive({ username: '', password: '', code: '', uuid: '' })

function loadCaptcha() {
  getCaptcha().then(res => {
    form.uuid = res.uuid
    captcha.value = 'data:image/gif;base64,' + res.img
  })
}

async function submit() {
  if (!form.code) {
    showFailToast('请输入验证码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    router.push('/')
  } catch (e) {
    showFailToast(e.message || '登录失败')
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.login-container-web {
  min-height: 100vh;
  display: flex;
  background: #fff;
  overflow: hidden;
}

.login-side-info {
  flex: 1.2;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  color: #fff;
}

.info-content {
  max-width: 500px;
  position: relative;
  z-index: 10;
}

.brand-logo-main {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  display: grid;
  place-items: center;
  font-size: 32px;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.brand-name-large {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
}

.brand-description {
  font-size: 18px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 40px;
}

.feature-pills {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pill-item {
  background: rgba(255, 255, 255, 0.06);
  padding: 10px 20px;
  border-radius: 99px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: rgba(59, 130, 246, 0.15);
  top: -100px;
  left: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: rgba(147, 197, 253, 0.1);
  bottom: -50px;
  right: -50px;
}

.login-side-form {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8fafc;
}

.login-form-card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  padding: 48px;
  border-radius: 28px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.04);
}

.form-header {
  margin-bottom: 30px;
  text-align: center;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
}

.form-header p {
  color: #64748b;
}

.modern-inputs :deep(.el-form-item) {
  margin-bottom: 16px;
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

.btn-login-premium {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  margin-top: 6px;
}

.login-footer-links {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.login-footer-links a {
  color: #0ea5b7;
  font-weight: 700;
  text-decoration: none;
  margin-left: 4px;
}
</style>
