<template>
  <view class="page">
    <button class="refresh" size="mini" @click="load">刷新</button>
    <view v-if="list.length === 0" class="empty">暂无申请记录</view>
    <view v-for="item in list" :key="item.id" class="card" @click="showRisk(item)">
      <view class="row">
        <text class="no">{{ item.applicationNo }}</text>
        <text :class="['tag', item.status]">{{ statusText(item.status) }}</text>
      </view>
      <text class="meta">¥{{ item.loanAmount }} · {{ item.loanTerm }}个月 · {{ item.loanType }}</text>
      <text v-if="item.riskScore != null" class="risk">
        评分卡 {{ item.scoringCardPoints }}/{{ item.scoringCardMax }} · 风险分 {{ item.riskScore }}
      </text>
    </view>
  </view>
</template>

<script>
import { request, getUserId, statusText } from '@/utils/api.js'

export default {
  data() {
    return { list: [] }
  },
  onShow() {
    this.load()
  },
  methods: {
    statusText,
    async load() {
      const uid = getUserId()
      if (!uid) return
      try {
        const res = await request(`/api/loan-applications/user/${uid}`)
        this.list = res.data || []
      } catch (e) {
        uni.showToast({ title: e.message, icon: 'none' })
      }
    },
    async showRisk(item) {
      try {
        const res = await request(`/api/risk/applications/${item.id}/assessment`)
        if (!res.success) throw new Error(res.message)
        const d = res.data || {}
        uni.showModal({
          title: '风控评估',
          content: `评分卡 ${d.scoringCardPoints}/${d.scoringCardMax}\n风险分 ${d.overallRiskScore}\n结果：${d.passed ? '通过' : '未通过'}`,
          showCancel: false,
        })
      } catch (e) {
        uni.showToast({ title: e.message, icon: 'none' })
      }
    },
  },
}
</script>

<style scoped>
.page { padding: 24rpx; background: #f5f1ff; min-height: 100vh; }
.refresh { margin-bottom: 16rpx; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
.card { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; }
.row { display: flex; justify-content: space-between; align-items: center; }
.no { font-weight: 600; font-size: 28rpx; }
.tag { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 8rpx; background: #eee; }
.tag.approved { background: #e8f5e9; color: #2e7d32; }
.tag.pending { background: #fff3e0; color: #ef6c00; }
.tag.rejected { background: #ffebee; color: #c62828; }
.meta { display: block; margin-top: 12rpx; color: #666; font-size: 26rpx; }
.risk { display: block; margin-top: 8rpx; color: #7b5cf2; font-size: 24rpx; }
</style>
