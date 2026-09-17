<template>
  <section class="admin-home" aria-labelledby="workbench-title">
    <header class="welcome">
      <h1 id="workbench-title">管理员工作台</h1>
      <div class="welcome-line">
        <p>欢迎回来，{{ adminName }}</p>
        <span class="demo-label">演示数据</span>
      </div>
    </header>

    <section class="stats-grid" aria-label="数据统计（示例）">
      <article v-for="stat in stats" :key="stat.key" class="stat-card">
        <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <template v-if="stat.key === 'users'">
            <circle cx="12" cy="7" r="3" />
            <path d="M6 21v-2a6 6 0 0 1 12 0v2M5 5a2 2 0 0 0 0 4M19 5a2 2 0 0 1 0 4M2 18v-1a4 4 0 0 1 3-4M22 18v-1a4 4 0 0 0-3-4" />
          </template>
          <template v-else-if="stat.key === 'applications'">
            <rect x="5" y="2" width="14" height="20" rx="2" />
            <path d="M9 7h6M9 11h6M9 15h4" />
          </template>
          <template v-else-if="stat.key === 'pending'">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 6v6l4 2" />
          </template>
          <template v-else>
            <path d="m12 2 10 5-10 5L2 7l10-5ZM2 12l10 5 10-5M2 17l10 5 10-5" />
          </template>
        </svg>
        <h2>{{ stat.label }}</h2>
        <p class="stat-value">{{ formatNumber(stat.value) }}</p>
      </article>
    </section>

    <div class="overview-grid">
      <section class="panel applications-panel" aria-labelledby="applications-title">
        <header class="panel-heading">
          <h2 id="applications-title">最近贷款申请</h2>
          <p>近期申请概览 · 示例数据</p>
        </header>
        <div class="table-scroll" role="region" aria-label="最近贷款申请表格" tabindex="0">
          <table>
            <thead>
              <tr><th scope="col">申请人</th><th scope="col">申请金额</th><th scope="col">申请时间</th><th scope="col">状态</th></tr>
            </thead>
            <tbody>
              <tr v-for="application in applications" :key="application.id">
                <td>{{ application.applicant }}</td>
                <td class="amount">¥{{ formatNumber(application.amount) }}</td>
                <td><time :datetime="application.date">{{ application.date }}</time></td>
                <td><span class="status-badge" :class="application.status">{{ application.statusText }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel system-panel" aria-labelledby="system-title">
        <header class="panel-heading">
          <h2 id="system-title">系统状态</h2>
          <p>展示状态，非实时检测</p>
        </header>
        <ul class="service-list">
          <li v-for="service in services" :key="service.name">
            <span>{{ service.name }}</span>
            <span class="service-status"><span class="status-dot" aria-hidden="true"></span>{{ service.status }}</span>
          </li>
        </ul>
      </section>
    </div>
    <p class="demo-note">当前页面使用 Mock 数据，仅用于界面展示。</p>
  </section>
</template>

<script>
import { getAdminUser } from '../../api/client'

// 首页展示模型：后续可统一替换数据来源，本阶段不请求业务接口。
const MOCK_STATS = [
  { key: 'users', label: '用户总数', value: 1280 },
  { key: 'applications', label: '贷款申请数量', value: 356 },
  { key: 'pending', label: '待审核申请', value: 23 },
  { key: 'products', label: '产品数量', value: 8 }
]
const MOCK_APPLICATIONS = [
  { id: 'demo-1', applicant: '张三', amount: 50000, date: '2026-09-15', status: 'reviewing', statusText: '审核中' },
  { id: 'demo-2', applicant: '李四', amount: 30000, date: '2026-09-14', status: 'approved', statusText: '已通过' },
  { id: 'demo-3', applicant: '王五', amount: 80000, date: '2026-09-13', status: 'supplement', statusText: '待补充' }
]
const MOCK_SERVICES = [
  { name: '数据库', status: '正常' },
  { name: '风控服务', status: '正常' },
  { name: 'Gateway', status: '正常' }
]

export default {
  name: 'AdminHome',
  data() {
    return {
      adminName: '管理员',
      stats: MOCK_STATS.map(item => ({ ...item })),
      applications: MOCK_APPLICATIONS.map(item => ({ ...item })),
      services: MOCK_SERVICES.map(item => ({ ...item }))
    }
  },
  mounted() {
    try {
      const admin = getAdminUser()
      this.adminName = admin?.username || admin?.name || '管理员'
    } catch {
      // 缓存缺失或损坏时使用默认称呼，不影响展示，也不改动登录状态。
      this.adminName = '管理员'
    }
  },
  methods: {
    formatNumber(value) {
      return Number(value).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.admin-home {
  --home-accent: #1677ff;
  --home-text: #142239;
  --home-muted: #687991;
  --home-border: #e9eff7;
  box-sizing: border-box;
  min-height: 100%;
  padding: 40px 36px 28px;
  background: #f2f7fc;
  color: var(--home-text);
  font-family: Arial, 'Microsoft YaHei', sans-serif;
  line-height: 1.5;
}
.welcome { margin-bottom: 28px; }
.welcome h1 { margin: 0 0 8px; font-size: 30px; font-weight: 700; letter-spacing: -.5px; }
.welcome-line { display: flex; align-items: center; flex-wrap: wrap; gap: 16px; }
.welcome-line p { margin: 0; color: var(--home-muted); font-size: 16px; overflow-wrap: anywhere; }
.demo-label { padding: 3px 12px; border-radius: 20px; background: #e1efff; color: #0965d9; font-size: 12px; white-space: nowrap; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 18px; margin-bottom: 26px; }
.stat-card, .panel { border: 1px solid #edf2f8; border-radius: 12px; background: #fff; box-shadow: 0 4px 18px rgba(31, 73, 125, .035); }
.stat-card { padding: 24px; transition: transform .18s ease, box-shadow .18s ease; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(31, 73, 125, .1); }
.stat-icon { display: block; width: 28px; height: 28px; margin-bottom: 14px; color: var(--home-accent); }
.stat-card h2 { margin: 0 0 6px; color: var(--home-muted); font-size: 14px; font-weight: 400; }
.stat-value { margin: 0; font-size: 34px; font-weight: 700; line-height: 1.25; font-variant-numeric: tabular-nums; }
.overview-grid { display: grid; grid-template-columns: minmax(0, 2fr) minmax(250px, 1fr); gap: 18px; }
.panel { min-width: 0; padding: 24px; }
.panel-heading { margin-bottom: 18px; }
.panel-heading h2 { margin: 0 0 4px; font-size: 19px; font-weight: 700; }
.panel-heading p { margin: 0; color: var(--home-muted); font-size: 13px; }
.table-scroll { overflow-x: auto; border-radius: 6px; }
.table-scroll:focus-visible { outline: 2px solid var(--home-accent); outline-offset: 3px; }
table { width: 100%; min-width: 480px; border-collapse: collapse; text-align: left; font-size: 14px; }
th { padding: 12px 14px; background: #f2f6fb; color: #4a5e79; font-weight: 600; white-space: nowrap; }
td { padding: 16px 14px; border-bottom: 1px solid var(--home-border); white-space: nowrap; }
tbody tr:hover { background: #fafcff; }
.amount, time { font-variant-numeric: tabular-nums; }
.status-badge { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; line-height: 1.6; }
.reviewing { color: #0965d9; background: #e5f0ff; }
.approved { color: #08794a; background: #e0f5e9; }
.supplement { color: #985500; background: #fff0d2; }
.service-list { margin: 0; padding: 0; list-style: none; }
.service-list li { display: flex; justify-content: space-between; align-items: center; gap: 16px; padding: 20px 0; border-top: 1px solid var(--home-border); font-size: 14px; }
.service-status { display: inline-flex; align-items: center; gap: 10px; color: #23843f; white-space: nowrap; }
.status-dot { width: 9px; height: 9px; border-radius: 50%; background: #2ba64a; }
.demo-note { margin: 38px 0 0; color: var(--home-muted); font-size: 12px; text-align: center; }
@media (max-width: 1100px) {
  .admin-home { padding: 28px 24px; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .overview-grid { grid-template-columns: minmax(0, 1fr); }
}
@media (max-width: 560px) {
  .admin-home { padding: 24px 16px; }
  .welcome h1 { font-size: 25px; }
  .stats-grid { gap: 12px; }
  .stat-card { padding: 18px 14px; }
  .stat-value { font-size: 28px; }
  .panel { padding: 18px 14px; }
}
@media (max-width: 360px) {
  .stats-grid { grid-template-columns: minmax(0, 1fr); }
}
@media (prefers-reduced-motion: reduce) {
  .stat-card { transition: none; }
  .stat-card:hover { transform: none; }
}
</style>
