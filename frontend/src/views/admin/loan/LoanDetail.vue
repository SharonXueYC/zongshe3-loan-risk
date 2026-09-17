<template>
  <section class="loan-page" aria-labelledby="loan-detail-title">
    <header class="page-header">
      <div>
        <h1 id="loan-detail-title">贷款申请详情</h1>
        <p>查看申请信息与风险报告预留区域，当前仅展示 Mock 数据。</p>
      </div>
      <router-link class="secondary-btn back-link" :to="{ name: 'admin-loan-applications' }">返回申请列表</router-link>
    </header>

    <template v-if="application">
      <section class="content-card detail-summary" aria-label="申请概览">
        <div>
          <span class="field-label">申请编号</span>
          <p class="summary-number">{{ application.applicationNo }}</p>
        </div>
        <span class="status-tag" :class="application.status.toLowerCase()">{{ loanStatusText(application.status) }}</span>
      </section>

      <div class="detail-grid">
        <section class="content-card" aria-labelledby="user-info-title">
          <header class="card-heading"><h2 id="user-info-title">用户信息</h2></header>
          <dl class="detail-fields">
            <div><dt>用户姓名</dt><dd>{{ application.userName }}</dd></div>
            <div><dt>用户编号</dt><dd>{{ application.userId }}</dd></div>
            <div><dt>联系电话（脱敏）</dt><dd>{{ application.phone }}</dd></div>
          </dl>
        </section>

        <section class="content-card" aria-labelledby="loan-info-title">
          <header class="card-heading"><h2 id="loan-info-title">申请信息</h2></header>
          <dl class="detail-fields">
            <div><dt>贷款金额</dt><dd class="amount-value">{{ formatLoanAmount(application.amount) }}</dd></div>
            <div><dt>贷款期限</dt><dd>{{ application.term }} 个月</dd></div>
            <div><dt>申请时间</dt><dd><time :datetime="application.applyTime.replace(' ', 'T')">{{ application.applyTime }}</time></dd></div>
            <div><dt>当前状态</dt><dd>{{ loanStatusText(application.status) }}</dd></div>
          </dl>
        </section>
      </div>

      <section class="content-card risk-card" aria-labelledby="risk-report-title">
        <header class="card-heading"><h2 id="risk-report-title">风险报告</h2></header>
        <div class="risk-placeholder">
          <p>风险报告区域已预留</p>
          <span>本阶段暂不生成或查询风险报告，后续在此展示评估结果。</span>
        </div>
      </section>
      <p class="demo-note">以上用户及申请信息为 Mock 数据，不代表真实用户或审批结果。</p>
    </template>

    <section v-else class="content-card empty-state" role="status">
      <h2>未找到该贷款申请</h2>
      <p>当前示例数据中不存在此申请，请返回列表选择申请。</p>
    </section>
  </section>
</template>

<script>
import { getMockLoanApplication, loanStatusText, formatLoanAmount } from './mock'

export default {
  name: 'AdminLoanDetail',
  computed: {
    application() {
      // 由 URL 中的 id 查找共用 Mock 数据，支持直接访问与路由参数切换。
      return getMockLoanApplication(this.$route.params.id)
    }
  },
  methods: { loanStatusText, formatLoanAmount }
}
</script>

<style scoped src="./loan.css"></style>
