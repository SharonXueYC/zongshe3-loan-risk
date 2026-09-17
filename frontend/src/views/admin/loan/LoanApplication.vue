<template>
  <section class="loan-page" aria-labelledby="loan-list-title">
    <header class="page-header">
      <div>
        <h1 id="loan-list-title">贷款申请管理</h1>
        <p>查看贷款申请与用户信息，当前页面仅展示 Mock 数据。</p>
      </div>
    </header>

    <form class="filter-card" aria-label="贷款申请筛选" @submit.prevent="applyFilters">
      <label class="filter-field">
        <span>用户姓名</span>
        <input v-model="filters.name" type="search" placeholder="请输入用户姓名" maxlength="100">
      </label>
      <label class="filter-field">
        <span>申请状态</span>
        <select v-model="filters.status">
          <option value="">全部</option>
          <option v-for="(label, status) in statusLabels" :key="status" :value="status">{{ label }}（{{ status }}）</option>
        </select>
      </label>
      <div class="filter-actions">
        <button class="primary-btn" type="submit">搜索</button>
        <button class="secondary-btn" type="button" @click="resetFilters">重置</button>
      </div>
    </form>

    <section class="content-card" aria-labelledby="applications-title">
      <header class="card-heading">
        <h2 id="applications-title">申请列表</h2>
        <span class="result-count" role="status">共 {{ filteredApplications.length }} 条申请</span>
      </header>
      <div class="table-scroll" tabindex="0" role="region" aria-label="贷款申请表格">
        <table>
          <thead>
            <tr>
              <th scope="col">申请编号</th>
              <th scope="col">用户姓名</th>
              <th scope="col">申请金额</th>
              <th scope="col">贷款期限</th>
              <th scope="col">申请状态</th>
              <th scope="col">申请时间</th>
              <th scope="col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="application in filteredApplications" :key="application.id">
              <td class="application-number">{{ application.applicationNo }}</td>
              <td>{{ application.userName }}</td>
              <td>{{ formatLoanAmount(application.amount) }}</td>
              <td>{{ application.term }} 个月</td>
              <td><span class="status-tag" :class="application.status.toLowerCase()">{{ loanStatusText(application.status) }}</span></td>
              <td><time :datetime="application.applyTime.replace(' ', 'T')">{{ application.applyTime }}</time></td>
              <td>
                <router-link class="text-link" :to="{ name: 'admin-loan-detail', params: { id: application.id } }"
                  :aria-label="`查看${application.userName}的申请详情`">查看详情</router-link>
              </td>
            </tr>
            <tr v-if="filteredApplications.length === 0">
              <td colspan="7" class="empty-state">没有符合筛选条件的贷款申请，请调整筛选条件。</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
    <p class="demo-note">以上申请均为示例数据，未连接真实贷款业务接口。</p>
  </section>
</template>

<script>
import { MOCK_LOAN_APPLICATIONS, LOAN_STATUS_LABELS, loanStatusText, formatLoanAmount } from './mock'

function emptyFilters() {
  return { name: '', status: '' }
}

export default {
  name: 'AdminLoanApplication',
  data() {
    return {
      applications: MOCK_LOAN_APPLICATIONS,
      statusLabels: LOAN_STATUS_LABELS,
      filters: emptyFilters(),
      appliedFilters: emptyFilters()
    }
  },
  computed: {
    filteredApplications() {
      const name = this.appliedFilters.name.trim().toLocaleLowerCase()
      const status = this.appliedFilters.status
      return this.applications.filter(application =>
        (!name || application.userName.toLocaleLowerCase().includes(name)) &&
        (!status || application.status === status)
      )
    }
  },
  methods: {
    loanStatusText,
    formatLoanAmount,
    applyFilters() {
      this.appliedFilters = { ...this.filters, name: this.filters.name.trim() }
    },
    resetFilters() {
      this.filters = emptyFilters()
      this.appliedFilters = emptyFilters()
    }
  }
}
</script>

<style scoped src="./loan.css"></style>
