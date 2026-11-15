<template>
  <div class="front-shell">
    <header class="header-main-modern">
      <div class="header-content">
        <div class="header-main__left">
          <div class="brand-mark-glow">
            <el-icon><Shop /></el-icon>
          </div>
          <div class="brand-text">
            <div class="brand-title">农产品销售管理系统</div>
            <div class="brand-sub">农产品销售管理系统 · 一站式服务</div>
          </div>
        </div>

        <nav class="header-main__nav-modern">
          <router-link
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="nav-item-modern"
            :class="{ active: isActive(item.to) }"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
            <el-badge v-if="item.badge" :value="item.badge" class="nav-badge-dot" />
          </router-link>
        </nav>

        <div class="header-main__right-modern">
          <div class="user-profile-mini">
            <img class="user-avatar-modern" :src="avatarUrl" alt="avatar" />
            <div class="user-info-text">
              <span class="user-name-modern">{{ userStore.userInfo.nickName || '用户' }}</span>
              <span class="user-role-tag">消费者</span>
            </div>
          </div>
          <el-button class="logout-btn-minimal" circle @click="logout" title="退出登录">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </div>
    </header>

    <section class="hero-main-modern" v-if="route.meta?.title">
      <div class="hero-content">
        <div class="hero-text-area">
          <h1 class="hero-title">{{ route.meta.title }}</h1>
          <p class="hero-subtitle">{{ route.meta.subtitle }}</p>
        </div>
        <div class="hero-decoration">
          <div class="hero-circle hero-circle-1"></div>
          <div class="hero-circle hero-circle-2"></div>
        </div>
      </div>
    </section>

    <main class="front-main">
      <router-view />
    </main>

    <button
      type="button"
      class="ai-float-entry"
      :class="{ active: route.path === '/ai-service' }"
      @click="router.push('/ai-service')"
    >
      <span class="ai-float-entry__icon">
        <el-icon><Service /></el-icon>
      </span>
      <span class="ai-float-entry__copy">
        <strong>AI 客服</strong>
        <small>订单、退款、推荐秒回</small>
      </span>
    </button>

    <el-dialog
      v-model="promotionDialogVisible"
      title="当前促销活动"
      width="680px"
      :close-on-click-modal="false"
      append-to-body
    >
      <div v-if="activePromotions.length" class="promo-popup-list">
        <div v-for="item in activePromotions" :key="item.promotionId" class="promo-popup-item">
          <div class="promo-popup-head">
            <span class="promo-popup-name">{{ item.promotionName || '促销活动' }}</span>
            <el-tag size="small" type="danger">{{ promotionTypeText(item.promotionType) }}</el-tag>
          </div>
          <div class="promo-popup-time">
            活动时间：{{ formatTime(item.startTime) }} 至 {{ formatTime(item.endTime) }}
          </div>
          <div class="promo-popup-content">{{ item.content || '全场优惠进行中，快去选购吧。' }}</div>
        </div>
      </div>
      <el-empty v-else description="当前暂无进行中的促销活动" />
      <template #footer>
        <el-button type="primary" @click="promotionDialogVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Shop,
  Service,
  ChatDotRound,
  Grid,
  ShoppingCart,
  Tickets,
  Comment,
  User,
  SwitchButton
} from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/profile.jpg'
import { useUserStore } from '@/store/user'
import { useMallStore } from '@/store/mall'
import { listActivePromotions } from '@/api/mall'
import { resolveMediaUrl } from '@/utils/media'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const mallStore = useMallStore()
const promotionDialogVisible = ref(false)
const activePromotions = ref([])

const navItems = computed(() => [
  { to: '/', label: '互动咨询', icon: ChatDotRound, badge: '' },
  { to: '/ai-service', label: 'AI客服', icon: Service, badge: '' },
  { to: '/products', label: '商品浏览', icon: Grid, badge: '' },
  { to: '/cart', label: '购物车', icon: ShoppingCart, badge: mallStore.cartCount || '' },
  { to: '/orders', label: '订单跟踪', icon: Tickets, badge: '' },
  { to: '/reviews', label: '评价互动', icon: Comment, badge: mallStore.reviewPendingCount || '' },
  { to: '/profile', label: '个人信息', icon: User, badge: '' }
])

const avatarUrl = computed(() => resolveMediaUrl(userStore.userInfo.avatar) || defaultAvatar)

const PROMOTION_TYPE_MAP = {
  '1': '限时购',
  '2': '满减',
  '3': '主题活动'
}

function isActive(path) {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

function logout() {
  userStore.logout()
  router.push('/login')
}

function promotionTypeText(value) {
  return PROMOTION_TYPE_MAP[String(value)] || '促销活动'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function promotionSeenKey() {
  const userId = userStore.userInfo?.userId || 'guest'
  return `front_seen_promotion_${userId}`
}

async function loadPromotionPopup() {
  if (!userStore.token) return
  try {
    const res = await listActivePromotions()
    const list = res.data || []
    activePromotions.value = list
    if (!list.length) return
    const signature = list.map(item => `${item.promotionId}_${item.updateTime || ''}`).join('|')
    const key = promotionSeenKey()
    if (sessionStorage.getItem(key) === signature) {
      return
    }
    sessionStorage.setItem(key, signature)
    promotionDialogVisible.value = true
  } catch (e) {
    // ignore promotion errors to avoid blocking page render
  }
}

async function refreshTopStat() {
  if (!userStore.token) return
  try {
    await mallStore.refreshCartStat()
  } catch (e) {
    // ignore cart stat errors to avoid blocking page render
  }
  try {
    await mallStore.refreshReviewStat()
  } catch (e) {
    // ignore review stat errors to avoid blocking page render
  }
}

onMounted(() => {
  refreshTopStat()
  loadPromotionPopup()
})
watch(() => route.fullPath, refreshTopStat)
</script>

<style scoped>
.promo-popup-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.promo-popup-item {
  border: 1px solid #fee2e2;
  background: #fff7ed;
  border-radius: 12px;
  padding: 12px 14px;
}

.promo-popup-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.promo-popup-name {
  font-size: 16px;
  font-weight: 700;
  color: #9a3412;
}

.promo-popup-time {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 6px;
}

.promo-popup-content {
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
}

.ai-float-entry {
  position: fixed;
  right: 24px;
  bottom: 28px;
  z-index: 60;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 196px;
  padding: 14px 16px;
  border: 0;
  border-radius: 20px;
  background: linear-gradient(135deg, #166534, #22c55e);
  color: #ffffff;
  cursor: pointer;
  box-shadow: 0 18px 36px rgba(21, 128, 61, 0.26);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.ai-float-entry:hover {
  transform: translateY(-2px);
  box-shadow: 0 22px 42px rgba(21, 128, 61, 0.32);
}

.ai-float-entry.active {
  opacity: 0.82;
}

.ai-float-entry__icon {
  width: 42px;
  height: 42px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.18);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.ai-float-entry__copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.2;
}

.ai-float-entry__copy strong {
  font-size: 15px;
  font-weight: 800;
}

.ai-float-entry__copy small {
  margin-top: 3px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
}

@media (max-width: 768px) {
  .ai-float-entry {
    right: 16px;
    bottom: 18px;
    min-width: auto;
    padding: 12px 14px;
    border-radius: 18px;
  }

  .ai-float-entry__copy small {
    display: none;
  }
}
</style>
