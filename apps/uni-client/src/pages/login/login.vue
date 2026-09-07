<template>
  <view class="page">
    <view class="hero">
      <text class="logo">闪借</text>
      <text class="sub">互联网个人贷款 · 安卓客户端</text>
    </view>

    <view class="card">
      <text class="label">服务器地址</text>
      <input v-model="apiBase" class="input" placeholder="http://你的服务器IP:8080" />
      <text class="hint">真机请填电脑局域网 IP 或云服务器域名</text>
    </view>

    <view class="card">
      <text class="label">手机号</text>
      <input v-model="phone" class="input" type="number" maxlength="11" placeholder="11位手机号" />
      <text class="label">密码</text>
      <input v-model="password" class="input" password placeholder="登录密码" />
      <button class="btn" :loading="loading" @click="login">登录</button>
      <button class="btn ghost" @click="fillDemo">演示账号</button>
    </view>
  </view>
</template>

<script>
import { request, setApiBase, getApiBase, saveSession } from '@/utils/api.js'

export default {
  data() {
    return {
      apiBase: getApiBase(),
      phone: '',
      password: '',
      loading: false,
    }
  },
  onLoad() {
    const token = uni.getStorageSync('token')
    if (token) {
      uni.reLaunch({ url: '/pages/home/home' })
    }
  },
  methods: {
    fillDemo() {
      this.phone = '13800138000'
      this.password = '123456'
    },
    async login() {
      if (!this.phone || !this.password) {
        uni.showToast({ title: '请填写账号密码', icon: 'none' })
        return
      }
      setApiBase(this.apiBase)
      this.loading = true
      try {
        const data = await request('/api/users/login', {
          method: 'POST',
          data: { phoneNumber: this.phone, password: this.password },
        })
        if (!data.success) throw new Error(data.message || '登录失败')
        saveSession(data.token, (data.userInfo && data.userInfo.userId) || '')
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => uni.reLaunch({ url: '/pages/home/home' }), 400)
      } catch (e) {
        uni.showToast({ title: e.message, icon: 'none', duration: 3000 })
      } finally {
        this.loading = false
      }
    },
  },
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 48rpx 32rpx; background: linear-gradient(180deg, #7b5cf2 0%, #f5f1ff 45%); }
.hero { text-align: center; padding: 60rpx 0 40rpx; color: #fff; }
.logo { font-size: 64rpx; font-weight: 700; display: block; }
.sub { font-size: 26rpx; opacity: 0.9; margin-top: 12rpx; display: block; }
.card { background: #fff; border-radius: 24rpx; padding: 32rpx; margin-bottom: 24rpx; box-shadow: 0 8rpx 32rpx rgba(88,58,196,0.12); }
.label { display: block; font-size: 26rpx; color: #666; margin-bottom: 12rpx; margin-top: 16rpx; }
.label:first-child { margin-top: 0; }
.input { background: #f8f6ff; border-radius: 16rpx; padding: 20rpx; font-size: 28rpx; }
.hint { font-size: 22rpx; color: #999; margin-top: 8rpx; display: block; }
.btn { margin-top: 32rpx; background: linear-gradient(135deg, #7b5cf2, #9b7bff); color: #fff; border-radius: 999rpx; font-size: 30rpx; }
.btn.ghost { background: #f0ebff; color: #7b5cf2; margin-top: 16rpx; }
</style>
