<template>
  <view class="page">
    <button class="refresh" size="mini" @click="load">刷新</button>
    <view v-if="plans.length === 0" class="empty">暂无待还计划</view>
    <view v-for="p in plans" :key="p.id" class="card">
      <view class="row">
        <text class="period">第 {{ p.periodNo }} 期</text>
        <text :class="['tag', p.status]">{{ planStatus(p.status) }}</text>
      </view>
      <text class="amount">应还 ¥{{ p.totalAmount }}</text>
      <text class="date">到期日 {{ p.dueDate }}</text>
      <button v-if="p.status === 'PENDING' || p.status === 'OVERDUE'" class="pay" size="mini" @click="pay(p)">还款</button>
    </view>
  </view>
</template>

<script>
import { request } from '@/utils/api.js'

export default {
  data() {
    return { plans: [] }
  },
  onShow() {
    this.load()
  },
  methods: {
    planStatus(s) {
      return { PENDING: '待还', PAID: '已还', OVERDUE: '逾期' }[s] || s
    },
    async load() {
      try {
        const data = await request('/api/repayment/plans/user')
        this.plans = Array.isArray(data) ? data : (data.data || [])
      } catch (e) {
        uni.showToast({ title: e.message, icon: 'none' })
      }
    },
    async pay(plan) {
      try {
        await request('/api/repayment/pay', {
          method: 'POST',
          data: { planId: plan.id, amount: plan.totalAmount },
        })
        uni.showToast({ title: '还款成功', icon: 'success' })
        this.load()
      } catch (e) {
        uni.showToast({ title: e.message, icon: 'none' })
      }
    },
  },
}
</script>

<style scoped>
.page { padding: 24rpx; background: #f5f1ff; min-height: 100vh; }
.card { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; position: relative; }
.row { display: flex; justify-content: space-between; }
.period { font-weight: 600; }
.tag { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 8rpx; }
.tag.PENDING { background: #fff3e0; color: #ef6c00; }
.tag.OVERDUE { background: #ffebee; color: #c62828; }
.tag.PAID { background: #e8f5e9; color: #2e7d32; }
.amount { display: block; margin-top: 12rpx; font-size: 32rpx; color: #7b5cf2; }
.date { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.pay { position: absolute; right: 24rpx; bottom: 24rpx; background: #7b5cf2; color: #fff; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
