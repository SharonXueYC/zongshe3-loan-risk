<template>
  <section class="loan-page" aria-labelledby="loan-detail-title">
    <header class="page-header">
      <div>
        <h1 id="loan-detail-title">贷款申请详情</h1>
        <p>查看申请信息与风险评估报告，当前仅展示 Mock 数据。</p>
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

      <section class="risk-report" aria-labelledby="risk-report-title">
        <header class="risk-report-header">
          <h2 id="risk-report-title">风险评估报告</h2>
          <p>以下评分、因素与建议为 Mock 数据，不代表真实风控结果。</p>
        </header>
        <template v-if="riskReport">
          <div class="risk-overview">
            <section class="content-card risk-score-card" aria-labelledby="risk-score-title">
              <h3 id="risk-score-title" class="risk-card-title">综合风险评分</h3>
              <div class="risk-score"><strong>{{ riskReport.score }}</strong><span>/ 100 分</span></div>
              <span class="risk-level" :class="riskReport.level.toLowerCase()">{{ riskLevelText(riskReport.level) }}</span>
              <p class="risk-scale-note">评分越高，风险越低。<br>80–100：低风险 · 60–79：中风险 · 低于60：高风险</p>
            </section>

            <section class="content-card risk-dimensions-card" aria-labelledby="risk-dimensions-title">
              <h3 id="risk-dimensions-title" class="risk-card-title">风险维度</h3>
              <div class="risk-dimension-list">
                <div v-for="dimension in riskDimensions" :key="dimension.key" class="risk-dimension">
                  <div class="risk-dimension-heading"><span>{{ dimension.label }}</span><strong>{{ dimension.score }} <small>/ 100</small></strong></div>
                  <progress :value="dimension.score" max="100" :aria-label="dimension.label">{{ dimension.score }} / 100</progress>
                </div>
              </div>
            </section>
          </div>

          <div class="risk-analysis">
            <section class="content-card risk-factors-card" aria-labelledby="risk-factors-title">
              <h3 id="risk-factors-title" class="risk-card-title">风险因素</h3>
              <div class="risk-factor-groups">
                <div class="risk-factor-group positive">
                  <h4>正向因素</h4>
                  <ul><li v-for="factor in riskReport.factors.positive" :key="factor">{{ factor }}</li></ul>
                </div>
                <div class="risk-factor-group negative">
                  <h4>风险因素</h4>
                  <ul><li v-for="factor in riskReport.factors.negative" :key="factor">{{ factor }}</li></ul>
                </div>
              </div>
            </section>

            <section class="content-card risk-decision-card" aria-labelledby="risk-decision-title">
              <h3 id="risk-decision-title" class="risk-card-title">审批建议</h3>
              <dl class="risk-decision-fields">
                <div><dt>建议结果</dt><dd>{{ riskReport.decision.result }}</dd></div>
                <div><dt>建议额度</dt><dd class="risk-decision-amount">{{ formatLoanAmount(riskReport.decision.amount) }}</dd></div>
              </dl>
              <p class="risk-decision-note">仅为示例建议，不执行审批或改变申请状态。</p>
            </section>
          </div>
        </template>
        <div v-else class="content-card risk-no-report" role="status">当前申请暂无示例风险报告。</div>
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
import { getMockRiskReport, riskLevelText } from './riskMock'

export default {
  name: 'AdminLoanDetail',
  computed: {
    application() {
      // 由 URL 中的 id 查找共用 Mock 数据，支持直接访问与路由参数切换。
      return getMockLoanApplication(this.$route.params.id)
    },
    riskReport() {
      return this.application ? getMockRiskReport(this.application.id) : null
    },
    riskDimensions() {
      if (!this.riskReport) return []
      return [
        { key: 'credit', label: '信用评分', score: this.riskReport.credit },
        { key: 'behavior', label: '行为评分', score: this.riskReport.behavior },
        { key: 'external', label: '外部数据评分', score: this.riskReport.external }
      ]
    }
  },
  methods: { loanStatusText, formatLoanAmount, riskLevelText }
}
</script>

<style scoped src="./loan.css"></style>
<style scoped src="./risk.css"></style>
