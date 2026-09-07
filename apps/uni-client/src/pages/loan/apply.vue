<template>
  <view class="page">
    <view class="card">
      <text class="title">提交贷款申请</text>
      <view class="field">
        <text class="label">贷款类型</text>
        <input v-model="form.loanType" class="input" />
      </view>
      <view class="field">
        <text class="label">金额（元）</text>
        <input v-model="form.loanAmount" type="digit" class="input" />
      </view>
      <view class="field-row">
        <view class="half">
          <text class="label">期限（月）</text>
          <input v-model="form.loanTerm" type="number" class="input" />
        </view>
        <view class="half">
          <text class="label">年利率 %</text>
          <input v-model="form.interestRate" type="digit" class="input" />
        </view>
      </view>
      <view class="field">
        <text class="label">用途说明</text>
        <textarea v-model="form.description" class="textarea" />
      </view>
      <button class="btn" :loading="loading" @click="submit">提交申请</button>
    </view>

    <view v-if="result.applicationNo" class="card result">
      <text class="ok">提交成功：{{ result.applicationNo }}</text>
      <text v-if="result.riskPassed != null" class="risk">
        风控：{{ result.riskPassed ? '通过' : '未通过' }}
        <text v-if="result.scoringCardPoints != null"> · 评分卡 {{ result.scoringCardPoints }}/{{ result.scoringCardMax }}</text>
      </text>
    </view>
  </view>
</template>

<script>
import { request } from '@/utils/api.js'

export default {
  data() {
    return {
      loading: false,
      form: {
        loanType: '个人消费贷',
        loanAmount: '10000',
        loanTerm: '12',
        interestRate: '12',
        repaymentMode: 'equal',
        description: '安卓客户端申请',
      },
      result: {},
    }
  },
  methods: {
    async submit() {
      this.loading = true
      try {
        const data = await request('/api/loan-applications/submit', {
          method: 'POST',
          data: {
            loanType: this.form.loanType,
            loanAmount: Number(this.form.loanAmount),
            loanTerm: Number(this.form.loanTerm),
            interestRate: Number(this.form.interestRate),
            repaymentMode: this.form.repaymentMode,
            description: this.form.description,
          },
        })
        if (!data.success) throw new Error(data.message || '提交失败')
        const ra = data.riskAssessment || {}
        this.result = {
          applicationNo: data.applicationNo,
          riskPassed: data.riskPassed ?? ra.passed,
          scoringCardPoints: ra.scoringCardPoints,
          scoringCardMax: ra.scoringCardMax,
        }
        uni.showToast({ title: '提交成功', icon: 'success' })
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
.page { padding: 32rpx; background: #f5f1ff; min-height: 100vh; }
.card { background: #fff; border-radius: 24rpx; padding: 32rpx; margin-bottom: 24rpx; }
.title { font-size: 34rpx; font-weight: 700; margin-bottom: 24rpx; display: block; }
.field { margin-bottom: 20rpx; }
.field-row { display: flex; gap: 16rpx; }
.half { flex: 1; }
.label { font-size: 24rpx; color: #666; margin-bottom: 8rpx; display: block; }
.input, .textarea { background: #f8f6ff; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; width: 100%; }
.textarea { min-height: 120rpx; }
.btn { margin-top: 16rpx; background: #7b5cf2; color: #fff; border-radius: 999rpx; }
.result .ok { color: #1f8a4c; display: block; font-size: 28rpx; }
.risk { display: block; margin-top: 12rpx; color: #6f4ce6; font-size: 26rpx; }
</style>
