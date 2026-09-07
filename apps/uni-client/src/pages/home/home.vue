<template>
  <view class="page">
    <view class="header">
      <text class="hello">你好，{{ userName }}</text>
      <text class="logout" @click="logout">退出</text>
    </view>

    <view class="limit-card">
      <text class="limit-label">可借额度（元）</text>
      <text class="limit-value">{{ credit.loanLimit || '--' }}</text>
      <text class="limit-sub">信用分 {{ credit.creditScore || '--' }} · {{ credit.creditLevel || '评估中' }}</text>
      <button class="refresh" size="mini" @click="loadCredit">刷新</button>
    </view>

    <view class="menu-grid">
      <view class="menu-item" @click="goApply">
        <text class="icon">💰</text>
        <text class="text">申请贷款</text>
      </view>
      <view class="menu-item" @click="goProgress">
        <text class="icon">📋</text>
        <text class="text">审核进度</text>
      </view>
      <view class="menu-item" @click="goRepay">
        <text class="icon">💳</text>
        <text class="text">还款计划</text>
      </view>
      <view class="menu-item" @click="goServer">
        <text class="icon">⚙️</text>
        <text class="text">服务器</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">最近申请</text>
      <view v-if="!recent" class="empty">暂无申请</view>
      <view v-else class="item">
        <text class="item-no">{{ recent.applicationNo }}</text>
        <text class="item-meta">{{ recent.loanAmount }} 元 · {{ statusText(recent.status) }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { request, getUserId, clearSession, statusText, getApiBase } from '@/utils/api.js'

export default {
  data() {
    return {
      userName: '用户',
      credit: {},
      recent: null,
    }
  },
  onShow() {
    if (!getUserId()) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadCredit()
    this.loadRecent()
  },
  methods: {
    statusText,
    async loadCredit() {
      try {
        const data = await request(`/api/credit/users/${getUserId()}/evaluation`)
        if (data.success !== false) this.credit = data
      } catch (_) {}
    },
    async loadRecent() {
      try {
        const res = await request(`/api/loan-applications/user/${getUserId()}`)
        const list = res.data || []
        this.recent = list.length ? list[0] : null
      } catch (_) {}
    },
    goApply() {
      uni.navigateTo({ url: '/pages/loan/apply' })
    },
    goProgress() {
      uni.navigateTo({ url: '/pages/loan/progress' })
    },
    goRepay() {
      uni.navigateTo({ url: '/pages/repay/repay' })
    },
    goServer() {
      uni.showModal({
        title: '服务器地址',
        content: getApiBase(),
        showCancel: false,
      })
    },
    logout() {
      clearSession()
      uni.reLaunch({ url: '/pages/login/login' })
    },
  },
}
</script>

<style scoped>
.page { padding: 32rpx; min-height: 100vh; background: #f5f1ff; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.hello { font-size: 36rpx; font-weight: 600; }
.logout { color: #7b5cf2; font-size: 26rpx; }
.limit-card { background: linear-gradient(135deg, #7b5cf2, #9b7bff); border-radius: 24rpx; padding: 36rpx; color: #fff; position: relative; }
.limit-label { font-size: 26rpx; opacity: 0.9; }
.limit-value { display: block; font-size: 72rpx; font-weight: 700; margin: 12rpx 0; }
.limit-sub { font-size: 24rpx; opacity: 0.85; }
.refresh { position: absolute; right: 24rpx; top: 24rpx; background: rgba(255,255,255,0.25); color: #fff; }
.menu-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20rpx; margin: 32rpx 0; }
.menu-item { background: #fff; border-radius: 20rpx; padding: 32rpx; text-align: center; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06); }
.icon { font-size: 48rpx; display: block; }
.text { font-size: 28rpx; margin-top: 12rpx; display: block; }
.section { background: #fff; border-radius: 20rpx; padding: 28rpx; }
.section-title { font-weight: 600; font-size: 30rpx; }
.empty { color: #999; margin-top: 16rpx; font-size: 26rpx; }
.item { margin-top: 16rpx; padding: 20rpx; background: #faf8ff; border-radius: 12rpx; }
.item-no { font-weight: 600; display: block; }
.item-meta { font-size: 24rpx; color: #666; margin-top: 8rpx; display: block; }
</style>
