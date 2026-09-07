<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-left">
        <h1 class="app-title">闪借</h1>
      </div>
      <div class="nav-right">
        <div class="user-info">
          <span class="username">{{ adminName }}</span>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧菜单栏 -->
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <ul>
            <li class="nav-item" :class="{ active: currentPage === 'dashboard' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'dashboard'">
                <span class="nav-icon">📊</span>
                <span class="nav-text">控制台</span>
              </a>
            </li>
            <li class="nav-item" :class="{ active: currentPage === 'users' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'users'">
                <span class="nav-icon">👥</span>
                <span class="nav-text">用户管理</span>
              </a>
            </li>
            <li class="nav-item" :class="{ active: currentPage === 'loans' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'loans'">
                <span class="nav-icon">💼</span>
                <span class="nav-text">贷款管理</span>
              </a>
            </li>
            <li class="nav-item" :class="{ active: currentPage === 'products' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'products'">
                <span class="nav-icon">💰</span>
                <span class="nav-text">产品管理</span>
              </a>
            </li>
            <li class="nav-item" :class="{ active: currentPage === 'repayment' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'repayment'">
                <span class="nav-icon">📝</span>
                <span class="nav-text">还款计划</span>
              </a>
            </li>
            <li class="nav-item" :class="{ active: currentPage === 'contracts' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'contracts'">
                <span class="nav-icon">📄</span>
                <span class="nav-text">合同管理</span>
              </a>
            </li>
            <li class="nav-item" :class="{ active: currentPage === 'statistics' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'statistics'">
                <span class="nav-icon">📈</span>
                <span class="nav-text">数据统计</span>
              </a>
            </li>
            <li class="nav-item" :class="{ active: currentPage === 'settings' }">
              <a href="#" class="nav-link" @click.prevent="currentPage = 'settings'">
                <span class="nav-icon">⚙️</span>
                <span class="nav-text">系统设置</span>
              </a>
            </li>
          </ul>
        </nav>
      </aside>

      <!-- 右侧内容区 -->
      <main class="content">
        <!-- 控制台页面 -->
        <div v-if="currentPage === 'dashboard'">
          <div class="content-header">
            <h2>控制台</h2>
            <p class="welcome-message">欢迎来到闪借管理系统</p>
          </div>

          <!-- 数据卡片 -->
          <div class="data-cards">
            <div class="card">
              <div class="card-title">总用户数</div>
              <div class="card-value">{{ formatNumber(dashboardStats.totalUsers) }}</div>
            </div>
            <div class="card">
              <div class="card-title">总贷款金额</div>
              <div class="card-value">¥{{ formatMoney(dashboardStats.totalAmount) }}</div>
            </div>
            <div class="card">
              <div class="card-title">今日新增</div>
              <div class="card-value">{{ dashboardStats.todayNew || 0 }}</div>
            </div>
            <div class="card">
              <div class="card-title">逾期率</div>
              <div class="card-value">{{ dashboardStats.overdueRate || 0 }}%</div>
            </div>
          </div>

          <!-- 图表区域 -->
          <div class="charts">
            <div class="chart-container">
              <h3>贷款趋势</h3>
              <div ref="dashboardLineChart" class="chart-container-echarts"></div>
            </div>
            <div class="chart-container chart-container-pie">
              <h3>贷款状态分布</h3>
              <div class="pie-panel">
                <div ref="dashboardPieChart" class="chart-pie-ring"></div>
                <ul v-if="dashboardPieLegend.length" class="pie-legend-row">
                  <li v-for="item in dashboardPieLegend" :key="item.name">
                    <span class="pie-legend-dot" :style="{ backgroundColor: item.color }"></span>
                    <span>{{ item.name }} {{ item.value }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- 最近贷款列表 -->
          <div class="recent-loans">
            <h3>最近贷款</h3>
            <div class="loan-table">
              <table>
                <thead>
                  <tr>
                    <th>用户</th>
                    <th>金额</th>
                    <th>期限</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="loan in recentLoans" :key="loan.id">
                    <td>{{ loan.applicant }}</td>
                    <td>¥{{ Number(loan.amount).toLocaleString() }}</td>
                    <td>{{ loan.term }}个月</td>
                    <td><span :class="['status', loan.status]">{{ loan.statusText }}</span></td>
                    <td><button class="action-btn" @click="openLoanById(loan.id)">查看</button></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- 用户管理页面 -->
        <div v-if="currentPage === 'users'">
          <div class="content-header">
            <h2>用户管理</h2>
            <div class="user-actions">
              <div class="search-box">
                <input type="text" v-model="searchKeyword" placeholder="搜索用户..." class="search-input">
                <button class="search-btn">搜索</button>
              </div>
              <button class="add-user-btn" @click="openUserModal()">新增用户</button>
            </div>
          </div>

          <!-- 用户表格 -->
          <div class="user-table-container">
            <table class="user-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>姓名</th>
                  <th>手机号</th>
                  <th>邮箱</th>
                  <th>注册时间</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in filteredUsers" :key="user.id">
                  <td>{{ user.id }}</td>
                  <td>{{ user.name }}</td>
                  <td>{{ user.phone }}</td>
                  <td>{{ user.email }}</td>
                  <td>{{ user.regTime }}</td>
                  <td><span :class="['status', user.status]">{{ user.statusText }}</span></td>
                  <td>
                    <button class="action-btn edit-btn" @click="openUserModal(user)">修改</button>
                    <button class="action-btn delete-btn" @click="confirmDelete(user.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 产品管理页面 -->
        <div v-if="currentPage === 'products'">
          <div class="content-header">
            <h2>产品管理</h2>
            <div class="user-actions">
              <div class="search-box">
                <input type="text" v-model="productFilter.keyword" placeholder="搜索产品..." class="search-input">
                <button class="search-btn" @click="loadProducts">搜索</button>
              </div>
              <button class="add-user-btn" @click="openProductModal()">新增产品</button>
            </div>
          </div>

          <div class="user-table-container">
            <table class="user-table">
              <thead>
                <tr>
                  <th>产品编号</th>
                  <th>产品名称</th>
                  <th>类型</th>
                  <th>金额范围</th>
                  <th>期限</th>
                  <th>利率</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="product in products" :key="product.id">
                  <td>{{ product.productNo }}</td>
                  <td>{{ product.productName }}</td>
                  <td>{{ product.productType }}</td>
                  <td>{{ product.minAmount }}-{{ product.maxAmount }}</td>
                  <td>{{ product.minTerm }}-{{ product.maxTerm }}个月</td>
                  <td>{{ product.interestRate }}%</td>
                  <td><span :class="['status', product.status === 'active' ? 'approved' : 'rejected']">{{ product.status === 'active' ? '启用' : '禁用' }}</span></td>
                  <td>
                    <button class="action-btn view-btn" @click="openProductModal(product)">编辑</button>
                    <button class="action-btn reject-btn" @click="confirmDeleteProduct(product.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 贷款管理页面 -->
        <div v-if="currentPage === 'loans'">
          <div class="content-header">
            <h2>贷款管理</h2>
            <div class="loan-actions">
              <div class="filter-box">
                <select v-model="loanFilter.status" class="filter-select">
                  <option value="">全部状态</option>
                  <option value="pending">审核中</option>
                  <option value="approved">已批准</option>
                  <option value="rejected">已拒绝</option>
                  <option value="paid">已还清</option>
                </select>
                <input type="text" v-model="loanFilter.keyword" placeholder="搜索申请人..." class="search-input">
                <button class="search-btn">搜索</button>
              </div>
            </div>
          </div>

          <!-- 贷款表格 -->
          <div class="loan-table-container">
            <table class="loan-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>申请人</th>
                  <th>金额</th>
                  <th>期限</th>
                  <th>申请时间</th>
                  <th>状态</th>
                  <th>风控结果</th>
                  <th>风险分</th>
                  <th>评分卡</th>
                  <th>拒绝原因</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="loan in filteredLoans" :key="loan.id" :class="{ 'risk-failed-row': loan.riskPassed === false }">
                  <td>{{ loan.id }}</td>
                  <td>{{ loan.applicant }}</td>
                  <td>¥{{ loan.amount.toLocaleString() }}</td>
                  <td>{{ loan.term }}个月</td>
                  <td>{{ loan.applyTime }}</td>
                  <td><span :class="['status', loan.status]">{{ loan.statusText }}</span></td>
                  <td>
                    <span :class="['risk-badge', riskPassedClass(loan)]">{{ riskPassedText(loan) }}</span>
                  </td>
                  <td>
                    <span v-if="loan.riskScore != null">{{ loan.riskScore }}</span>
                    <span v-else class="text-muted">-</span>
                    <span v-if="loan.riskLevel != null" class="risk-level-tag">{{ riskLevelText(loan.riskLevel) }}</span>
                  </td>
                  <td>{{ formatScoringCard(loan) }}</td>
                  <td class="risk-reason-cell" :title="loan.rejectReason || ''">{{ loan.rejectReason || '-' }}</td>
                  <td class="loan-actions-cell">
                    <button class="action-btn view-btn" @click="openLoanDetail(loan)">查看</button>
                    <template v-if="loan.status === 'pending'">
                      <div v-if="loan.riskPassed === false" class="risk-approve-hint">
                        风控未通过，建议拒绝
                      </div>
                      <button class="action-btn approve-btn" @click="approveLoan(loan)">通过</button>
                      <button class="action-btn reject-btn" @click="rejectLoan(loan.id)">拒绝</button>
                    </template>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 合同管理页面 -->
        <div v-if="currentPage === 'contracts'">
          <div class="content-header">
            <h2>合同管理</h2>
            <div class="contract-actions">
              <div class="filter-box">
                <select v-model="contractFilter.status" class="filter-select">
                  <option value="">全部状态</option>
                  <option value="pending">待签署</option>
                  <option value="signed">已签署待放款</option>
                  <option value="active">已放款</option>
                  <option value="completed">已完成</option>
                </select>
                <input type="text" v-model="contractFilter.keyword" placeholder="搜索合同号或申请人..." class="search-input">
                <button class="search-btn">搜索</button>
              </div>
            </div>
          </div>

          <!-- 合同表格 -->
          <div class="contract-table-container">
            <table class="contract-table">
              <thead>
                <tr>
                  <th>合同编号</th>
                  <th>申请人</th>
                  <th>贷款金额</th>
                  <th>期限</th>
                  <th>签署日期</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="contract in filteredContracts" :key="contract.id">
                  <td>{{ contract.contractNo }}</td>
                  <td>{{ contract.applicant }}</td>
                  <td>¥{{ contract.amount.toLocaleString() }}</td>
                  <td>{{ contract.term }}个月</td>
                  <td>{{ contract.signDate }}</td>
                  <td><span :class="['status', contract.status]">{{ contract.statusText }}</span></td>
                  <td>
                    <button class="action-btn view-btn" @click="openContractDetail(contract)">查看</button>
                    <button v-if="contract.canGenerate" class="action-btn approve-btn" @click="handleGenerateContract(contract)">生成合同</button>
                    <button v-if="contract.canSign" class="action-btn approve-btn" @click="handleSignContract(contract)">签署</button>
                    <button v-if="contract.canDisburse" class="action-btn pay-btn" @click="handleDisburseContract(contract)">放款</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 还款计划页面 -->
        <div v-if="currentPage === 'repayment'">
          <div class="content-header">
            <h2>还款计划</h2>
            <div class="repayment-actions">
              <div class="filter-box">
                <select v-model="repaymentFilter.status" class="filter-select">
                  <option value="">全部状态</option>
                  <option value="pending">待还款</option>
                  <option value="paid">已还款</option>
                  <option value="overdue">逾期</option>
                </select>
                <input type="text" v-model="repaymentFilter.keyword" placeholder="搜索申请人..." class="search-input">
                <button class="search-btn">搜索</button>
              </div>
            </div>
          </div>

          <!-- 还款表格 -->
          <div class="repayment-table-container">
            <table class="repayment-table">
              <thead>
                <tr>
                  <th>贷款ID</th>
                  <th>申请人</th>
                  <th>期数</th>
                  <th>应还金额</th>
                  <th>本金</th>
                  <th>利息</th>
                  <th>应还日期</th>
                  <th>还款日期</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="repayment in filteredRepayments" :key="repayment.id" :class="{ overdue: repayment.status === 'overdue' }">
                  <td>{{ repayment.loanId }}</td>
                  <td>{{ repayment.applicant }}</td>
                  <td>{{ repayment.period }}/{{ repayment.totalPeriods }}</td>
                  <td>¥{{ repayment.amount.toLocaleString() }}</td>
                  <td>¥{{ repayment.principal.toLocaleString() }}</td>
                  <td>¥{{ repayment.interest.toLocaleString() }}</td>
                  <td>{{ repayment.dueDate }}</td>
                  <td>{{ repayment.paidDate || '-' }}</td>
                  <td><span :class="['status', repayment.status]">{{ repayment.statusText }}</span></td>
                  <td>
                    <button v-if="repayment.status === 'pending'" class="action-btn pay-btn" @click="markAsPaid(repayment.id)">确认还款</button>
                    <button class="action-btn view-btn" @click="openRepaymentDetail(repayment)">查看详情</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 数据统计页面 -->
        <div v-if="currentPage === 'statistics'">
          <div class="content-header">
            <h2>数据统计</h2>
            <div class="statistics-actions">
              <select v-model="statisticsPeriod" class="filter-select">
                <option value="week">本周</option>
                <option value="month">本月</option>
                <option value="quarter">本季度</option>
                <option value="year">本年</option>
              </select>
              <button class="refresh-btn" @click="refreshCharts()">🔄 刷新数据</button>
            </div>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon">💰</div>
              <div class="stat-info">
                <div class="stat-value">¥{{ totalLoanAmount.toLocaleString() }}</div>
                <div class="stat-label">累计放款</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📈</div>
              <div class="stat-info">
                <div class="stat-value">{{ loanCount }}</div>
                <div class="stat-label">贷款笔数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">💳</div>
              <div class="stat-info">
                <div class="stat-value">¥{{ totalRepaid.toLocaleString() }}</div>
                <div class="stat-label">累计还款</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">⚠️</div>
              <div class="stat-info">
                <div class="stat-value">{{ overdueRate }}%</div>
                <div class="stat-label">逾期率</div>
              </div>
            </div>
          </div>

          <!-- 图表区域 -->
          <div class="charts-grid">
            <div class="chart-wrapper">
              <h3>贷款趋势</h3>
              <div ref="lineChart" class="chart-container chart-container-sm"></div>
            </div>
            <div class="chart-wrapper">
              <h3>贷款状态分布</h3>
              <div ref="pieChart" class="chart-container chart-container-sm"></div>
            </div>
            <div class="chart-wrapper">
              <h3>用户等级分布</h3>
              <div ref="pieChart2" class="chart-container chart-container-sm"></div>
            </div>
            <div class="chart-wrapper chart-wrapper-full">
              <h3>月度还款分析</h3>
              <div ref="barChart" class="chart-container chart-container-bar"></div>
            </div>
          </div>
        </div>

        <!-- 系统设置页面 -->
        <div v-if="currentPage === 'settings'">
          <div class="content-header">
            <h2>{{ pageTitles[currentPage] }}</h2>
          </div>
          
          <!-- 设置标签页 -->
          <div class="settings-tabs">
            <button 
              v-for="tab in settingsTabs" 
              :key="tab.id"
              :class="['tab-btn', { active: currentSettingsTab === tab.id }]"
              @click="currentSettingsTab = tab.id"
            >
              {{ tab.name }}
            </button>
          </div>
          
          <!-- 基本设置 -->
          <div v-if="currentSettingsTab === 'basic'" class="settings-container">
            <div class="settings-section">
              <h3>系统信息</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>系统名称</label>
                    <input v-model="settings.basic.systemName" type="text" />
                  </div>
                  <div class="form-group">
                    <label>版本号</label>
                    <input v-model="settings.basic.version" type="text" readonly />
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>系统描述</label>
                    <textarea v-model="settings.basic.description" rows="3"></textarea>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="settings-section">
              <h3>联系方式</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>客服电话</label>
                    <input v-model="settings.basic.phone" type="text" />
                  </div>
                  <div class="form-group">
                    <label>邮箱地址</label>
                    <input v-model="settings.basic.email" type="email" />
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>公司地址</label>
                    <input v-model="settings.basic.address" type="text" />
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 贷款设置 -->
          <div v-if="currentSettingsTab === 'loan'" class="settings-container">
            <div class="settings-section">
              <h3>利率设置</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>最低年利率(%)</label>
                    <input v-model.number="settings.loan.minRate" type="number" step="0.1" />
                  </div>
                  <div class="form-group">
                    <label>最高年利率(%)</label>
                    <input v-model.number="settings.loan.maxRate" type="number" step="0.1" />
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>默认年利率(%)</label>
                    <input v-model.number="settings.loan.defaultRate" type="number" step="0.1" />
                  </div>
                </div>
              </div>
            </div>
            
            <div class="settings-section">
              <h3>额度设置</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>最低贷款金额(元)</label>
                    <input v-model.number="settings.loan.minAmount" type="number" />
                  </div>
                  <div class="form-group">
                    <label>最高贷款金额(元)</label>
                    <input v-model.number="settings.loan.maxAmount" type="number" />
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>最低期限(月)</label>
                    <input v-model.number="settings.loan.minTerm" type="number" />
                  </div>
                  <div class="form-group">
                    <label>最高期限(月)</label>
                    <input v-model.number="settings.loan.maxTerm" type="number" />
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 通知设置 -->
          <div v-if="currentSettingsTab === 'notification'" class="settings-container">
            <div class="settings-section">
              <h3>还款提醒</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>提前提醒天数</label>
                    <input v-model.number="settings.notification.remindDays" type="number" />
                  </div>
                  <div class="form-group">
                    <label>逾期提醒间隔(小时)</label>
                    <input v-model.number="settings.notification.overdueInterval" type="number" />
                  </div>
                </div>
              </div>
            </div>
            
            <div class="settings-section">
              <h3>通知方式</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group checkbox-group">
                    <label class="checkbox-label">
                      <input v-model="settings.notification.sms" type="checkbox" />
                      <span>短信通知</span>
                    </label>
                    <label class="checkbox-label">
                      <input v-model="settings.notification.email" type="checkbox" />
                      <span>邮件通知</span>
                    </label>
                    <label class="checkbox-label">
                      <input v-model="settings.notification.wechat" type="checkbox" />
                      <span>微信通知</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 安全设置 -->
          <div v-if="currentSettingsTab === 'security'" class="settings-container">
            <div class="settings-section">
              <h3>密码策略</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>最小密码长度</label>
                    <input v-model.number="settings.security.minPasswordLength" type="number" />
                  </div>
                  <div class="form-group">
                    <label>密码有效期(天)</label>
                    <input v-model.number="settings.security.passwordExpireDays" type="number" />
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group checkbox-group">
                    <label class="checkbox-label">
                      <input v-model="settings.security.requireUpper" type="checkbox" />
                      <span>要求大写字母</span>
                    </label>
                    <label class="checkbox-label">
                      <input v-model="settings.security.requireLower" type="checkbox" />
                      <span>要求小写字母</span>
                    </label>
                    <label class="checkbox-label">
                      <input v-model="settings.security.requireNumber" type="checkbox" />
                      <span>要求数字</span>
                    </label>
                    <label class="checkbox-label">
                      <input v-model="settings.security.requireSpecial" type="checkbox" />
                      <span>要求特殊字符</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="settings-section">
              <h3>登录安全</h3>
              <div class="settings-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>最大登录尝试次数</label>
                    <input v-model.number="settings.security.maxLoginAttempts" type="number" />
                  </div>
                  <div class="form-group">
                    <label>账号锁定时间(分钟)</label>
                    <input v-model.number="settings.security.lockoutMinutes" type="number" />
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group checkbox-group">
                    <label class="checkbox-label">
                      <input v-model="settings.security.enableCaptcha" type="checkbox" />
                      <span>登录启用验证码</span>
                    </label>
                    <label class="checkbox-label">
                      <input v-model="settings.security.enableTwoFactor" type="checkbox" />
                      <span>启用双因素认证</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 保存按钮 -->
          <div class="settings-actions">
            <button class="save-btn" @click="saveSettings">保存设置</button>
            <button class="cancel-btn" @click="resetSettings">重置</button>
          </div>
        </div>

        <!-- 用户编辑弹窗 -->
        <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
          <div class="modal-content" @click.stop>
            <div class="modal-header">
              <h3>{{ isEditing ? '修改用户' : '新增用户' }}</h3>
              <button class="close-btn" @click="closeUserModal">×</button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveUser">
                <div class="form-group">
                  <label>姓名</label>
                  <input type="text" v-model="formData.name" required>
                </div>
                <div class="form-group">
                  <label>手机号</label>
                  <input type="tel" v-model="formData.phone" required>
                </div>
                <div class="form-group">
                  <label>邮箱</label>
                  <input type="email" v-model="formData.email" required>
                </div>
                <div class="form-group">
                  <label>状态</label>
                  <select v-model="formData.status">
                    <option value="active">活跃</option>
                    <option value="inactive">禁用</option>
                  </select>
                </div>
                <div class="modal-footer">
                  <button type="button" class="cancel-btn" @click="closeUserModal">取消</button>
                  <button type="submit" class="save-btn">保存</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- 删除确认弹窗 -->
        <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
          <div class="modal-content delete-modal" @click.stop>
            <div class="modal-header">
              <h3>确认删除</h3>
              <button class="close-btn" @click="closeDeleteModal">×</button>
            </div>
            <div class="modal-body">
              <p>确定要删除该用户吗？此操作不可撤销。</p>
            </div>
            <div class="modal-footer">
              <button class="cancel-btn" @click="closeDeleteModal">取消</button>
              <button class="delete-confirm-btn" @click="deleteUser">删除</button>
            </div>
          </div>
        </div>

        <!-- 产品编辑弹窗 -->
        <div v-if="showProductModal" class="modal-overlay" @click="closeProductModal">
          <div class="modal-content" @click.stop>
            <div class="modal-header">
              <h3>{{ isEditingProduct ? '编辑产品' : '新增产品' }}</h3>
              <button class="close-btn" @click="closeProductModal">×</button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveProduct">
                <div class="form-group">
                  <label>产品编号</label>
                  <input type="text" v-model="productForm.productNo" required>
                </div>
                <div class="form-group">
                  <label>产品名称</label>
                  <input type="text" v-model="productForm.productName" required>
                </div>
                <div class="form-group">
                  <label>产品类型</label>
                  <input type="text" v-model="productForm.productType" required>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>最低金额</label>
                    <input type="number" v-model.number="productForm.minAmount" required>
                  </div>
                  <div class="form-group">
                    <label>最高金额</label>
                    <input type="number" v-model.number="productForm.maxAmount" required>
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>最短期限(月)</label>
                    <input type="number" v-model.number="productForm.minTerm" required>
                  </div>
                  <div class="form-group">
                    <label>最长期限(月)</label>
                    <input type="number" v-model.number="productForm.maxTerm" required>
                  </div>
                </div>
                <div class="form-group">
                  <label>年利率(%)</label>
                  <input type="number" step="0.01" v-model.number="productForm.interestRate" required>
                </div>
                <div class="form-group">
                  <label>状态</label>
                  <select v-model="productForm.status">
                    <option value="active">启用</option>
                    <option value="inactive">禁用</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>产品描述</label>
                  <textarea v-model="productForm.productDescription" rows="3"></textarea>
                </div>
                <div class="modal-footer">
                  <button type="button" class="cancel-btn" @click="closeProductModal">取消</button>
                  <button type="submit" class="save-btn">保存</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- 贷款详情弹窗 -->
        <div v-if="showLoanDetail" class="modal-overlay" @click="closeLoanDetail">
          <div class="modal-content loan-detail-modal" @click.stop>
            <div class="modal-header">
              <h3>贷款详情</h3>
              <button class="close-btn" @click="closeLoanDetail">×</button>
            </div>
            <div class="modal-body">
              <div class="loan-detail-content">
                <div class="detail-section">
                  <h4>基本信息</h4>
                  <div class="detail-row">
                    <span class="detail-label">贷款ID:</span>
                    <span class="detail-value">{{ currentLoan.id }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">申请人:</span>
                    <span class="detail-value">{{ currentLoan.applicant }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">贷款金额:</span>
                    <span class="detail-value">¥{{ currentLoan.amount.toLocaleString() }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">贷款期限:</span>
                    <span class="detail-value">{{ currentLoan.term }}个月</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">年利率:</span>
                    <span class="detail-value">{{ currentLoan.interestRate }}%</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">申请时间:</span>
                    <span class="detail-value">{{ currentLoan.applyTime }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">贷款状态:</span>
                    <span class="detail-value"><span :class="['status', currentLoan.status]">{{ currentLoan.statusText }}</span></span>
                  </div>
                </div>
                
                <div class="detail-section">
                  <h4>申请人资料</h4>
                  <div class="detail-row">
                    <span class="detail-label">身份证号:</span>
                    <span class="detail-value">{{ currentLoan.idCard }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">手机号:</span>
                    <span class="detail-value">{{ currentLoan.phone }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">邮箱:</span>
                    <span class="detail-value">{{ currentLoan.email }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">地址:</span>
                    <span class="detail-value">{{ currentLoan.address }}</span>
                  </div>
                </div>
                
                <div class="detail-section risk-detail-section">
                  <h4>风控评估</h4>
                  <div v-if="currentLoan.riskPassed === false" class="risk-detail-alert">
                    风控未通过，建议拒绝。管理员仍可人工审批，请谨慎操作。
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">申请单号:</span>
                    <span class="detail-value">{{ currentLoan.applicationNo || '-' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">风控结果:</span>
                    <span class="detail-value">
                      <span :class="['risk-badge', riskPassedClass(currentLoan)]">{{ riskPassedText(currentLoan) }}</span>
                    </span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">风险分:</span>
                    <span class="detail-value">
                      {{ currentLoan.riskScore != null ? currentLoan.riskScore : '-' }}
                      <span v-if="currentLoan.riskLevel != null" class="risk-level-tag">{{ riskLevelText(currentLoan.riskLevel) }}风险</span>
                    </span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">评分卡:</span>
                    <span class="detail-value">{{ formatScoringCard(currentLoan) }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">拒绝原因:</span>
                    <span class="detail-value reject-reason-text">{{ currentLoan.rejectReason || '-' }}</span>
                  </div>
                </div>

                <div class="detail-section">
                  <h4>贷款资料</h4>
                  <div class="document-list">
                    <div class="document-item">
                      <span class="document-name">身份证正反面</span>
                      <button class="view-doc-btn">查看</button>
                    </div>
                    <div class="document-item">
                      <span class="document-name">收入证明</span>
                      <button class="view-doc-btn">查看</button>
                    </div>
                    <div class="document-item">
                      <span class="document-name">银行流水</span>
                      <button class="view-doc-btn">查看</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <template v-if="currentLoan.status === 'pending'">
                <div v-if="currentLoan.riskPassed === false" class="risk-approve-hint modal-hint">
                  风控未通过，建议拒绝
                </div>
                <button class="approve-btn modal-action-btn" @click="approveLoan(currentLoan)">通过</button>
                <button class="reject-btn modal-action-btn" @click="rejectLoan(currentLoan.id)">拒绝</button>
              </template>
              <button class="cancel-btn" @click="closeLoanDetail">关闭</button>
            </div>
          </div>
        </div>

        <!-- 合同详情弹窗 -->
        <div v-if="showContractDetail" class="modal-overlay" @click="closeContractDetail">
          <div class="modal-content contract-detail-modal" @click.stop>
            <div class="modal-header">
              <h3>合同详情</h3>
              <button class="close-btn" @click="closeContractDetail">×</button>
            </div>
            <div class="modal-body">
              <div class="contract-detail-content">
                <div class="detail-section">
                  <h4>基本信息</h4>
                  <div class="detail-row">
                    <span class="detail-label">合同编号:</span>
                    <span class="detail-value">{{ currentContract.contractNo }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">关联贷款ID:</span>
                    <span class="detail-value">{{ currentContract.loanId }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">申请人:</span>
                    <span class="detail-value">{{ currentContract.applicant }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">贷款金额:</span>
                    <span class="detail-value">¥{{ currentContract.amount.toLocaleString() }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">贷款期限:</span>
                    <span class="detail-value">{{ currentContract.term }}个月</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">签署日期:</span>
                    <span class="detail-value">{{ currentContract.signDate }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">合同状态:</span>
                    <span class="detail-value"><span :class="['status', currentContract.status]">{{ currentContract.statusText }}</span></span>
                  </div>
                </div>
                
                <div class="detail-section">
                  <h4>合同内容摘要</h4>
                  <div class="contract-summary">
                    <p>根据《中华人民共和国合同法》及相关法律法规，甲方（借款人）与乙方（贷款方）本着平等、自愿、公平的原则，经协商一致，签订本合同。</p>
                    <p><strong>第一条：贷款金额</strong>：人民币{{ currentContract.amount.toLocaleString() }}元整（大写：{{ formatAmount(currentContract.amount) }}）</p>
                    <p><strong>第二条：贷款期限</strong>：自合同签署之日起{{ currentContract.term }}个月</p>
                    <p><strong>第三条：贷款利率</strong>：按照双方约定执行</p>
                    <p><strong>第四条：还款方式</strong>：等额本息还款</p>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="cancel-btn" @click="closeContractDetail">关闭</button>
              <button v-if="currentContract.canGenerate" class="save-btn" @click="handleGenerateContract(currentContract)">生成合同</button>
              <button v-if="currentContract.canSign" class="save-btn" @click="handleSignContract(currentContract)">确认签署</button>
              <button v-if="currentContract.canDisburse" class="save-btn" @click="handleDisburseContract(currentContract)">执行放款</button>
            </div>
          </div>
        </div>

        <!-- 还款详情弹窗 -->
        <div v-if="showRepaymentDetail" class="modal-overlay" @click="closeRepaymentDetail">
          <div class="modal-content repayment-detail-modal" @click.stop>
            <div class="modal-header">
              <h3>还款详情</h3>
              <button class="close-btn" @click="closeRepaymentDetail">×</button>
            </div>
            <div class="modal-body">
              <div class="repayment-detail-content">
                <div class="detail-section">
                  <h4>基本信息</h4>
                  <div class="detail-row">
                    <span class="detail-label">还款 ID:</span>
                    <span class="detail-value">{{ currentRepayment.id }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">贷款 ID:</span>
                    <span class="detail-value">{{ currentRepayment.loanId }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">申请人:</span>
                    <span class="detail-value">{{ currentRepayment.applicant }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">期数:</span>
                    <span class="detail-value">{{ currentRepayment.period }} / {{ currentRepayment.totalPeriods }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">还款状态:</span>
                    <span class="detail-value"><span :class="['status', currentRepayment.status]">{{ currentRepayment.statusText }}</span></span>
                  </div>
                </div>
                
                <div class="detail-section">
                  <h4>还款金额明细</h4>
                  <div class="detail-row">
                    <span class="detail-label">应还金额:</span>
                    <span class="detail-value">¥{{ currentRepayment.amount.toLocaleString() }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">本金:</span>
                    <span class="detail-value">¥{{ currentRepayment.principal.toLocaleString() }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">利息:</span>
                    <span class="detail-value">¥{{ currentRepayment.interest.toLocaleString() }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">应还日期:</span>
                    <span class="detail-value">{{ currentRepayment.dueDate }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">还款日期:</span>
                    <span class="detail-value">{{ currentRepayment.paidDate || '未还款' }}</span>
                  </div>
                </div>
                
                <div class="detail-section">
                  <h4>还款计划表</h4>
                  <div class="repayment-schedule">
                    <table class="schedule-table">
                      <thead>
                        <tr>
                          <th>期数</th>
                          <th>应还金额</th>
                          <th>本金</th>
                          <th>利息</th>
                          <th>应还日期</th>
                          <th>状态</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="i in currentRepayment.totalPeriods" :key="i">
                          <td>{{ i }}</td>
                          <td>¥{{ (currentRepayment.amount).toLocaleString() }}</td>
                          <td>¥{{ (currentRepayment.principal).toLocaleString() }}</td>
                          <td>¥{{ (currentRepayment.interest).toLocaleString() }}</td>
                          <td>{{ calculateDueDate(currentRepayment.dueDate, i - currentRepayment.period) }}</td>
                          <td><span :class="['status', i <= currentRepayment.period ? currentRepayment.status : 'pending']">{{ i <= currentRepayment.period ? currentRepayment.statusText : '待还款' }}</span></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="cancel-btn" @click="closeRepaymentDetail">关闭</button>
              <button v-if="currentRepayment.status === 'pending'" class="save-btn" @click="markAsPaid(currentRepayment.id)">确认还款</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  fetchUsers,
  createUser,
  updateUser,
  deleteUser as apiDeleteUser,
  fetchLoans,
  approveLoan as apiApproveLoan,
  rejectLoan as apiRejectLoan,
  fetchContracts,
  fetchRepayments,
  confirmRepayment,
  fetchDashboardStats,
  fetchRecentLoans,
  fetchChartData,
  fetchPortalSettings,
  savePortalSettings,
  resetPortalSettings,
  fetchProducts,
  createProduct,
  updateProduct,
  deleteProduct as apiDeleteProduct,
  generateContract,
  signContract as apiSignContract,
  disburseContract as apiDisburseContract,
  logout as apiLogout
} from '../api/admin'
import { clearAuth, getAdminUser } from '../api/client'

export default {
  name: 'Dashboard',
  data() {
    return {
      currentPage: 'dashboard',
      adminName: '管理员',
      dashboardStats: {
        totalUsers: 0,
        totalAmount: 0,
        todayNew: 0,
        overdueRate: 0
      },
      recentLoans: [],
      pageTitles: {
        loans: '贷款管理',
        products: '产品管理',
        repayment: '还款计划',
        contracts: '合同管理',
        statistics: '数据统计',
        settings: '系统设置'
      },
      searchKeyword: '',
      users: [],
      showUserModal: false,
      showDeleteModal: false,
      isEditing: false,
      formData: {
        id: '',
        name: '',
        phone: '',
        email: '',
        status: 'active'
      },
      deleteUserId: '',
      products: [],
      productFilter: { keyword: '' },
      showProductModal: false,
      isEditingProduct: false,
      productForm: {
        id: null,
        productNo: '',
        productName: '',
        productType: '',
        minAmount: 1000,
        maxAmount: 500000,
        minTerm: 3,
        maxTerm: 36,
        interestRate: 12,
        productDescription: '',
        status: 'active'
      },
    // 贷款相关数据
    loans: [],
    loanFilter: {
      status: '',
      keyword: ''
    },
    showLoanDetail: false,
    currentLoan: {},
    // 合同相关数据
    contracts: [],
    contractFilter: {
      status: '',
      keyword: ''
    },
    showContractDetail: false,
    currentContract: {},
    // 还款计划数据
    repayments: [],
    repaymentFilter: {
      status: '',
      keyword: ''
    },
    showRepaymentDetail: false,
    currentRepayment: {},
    // 统计数据
    statisticsPeriod: 'month',
    // 图表数据
    chartData: {
      lineChartData: [1200, 1320, 1010, 1340, 1900, 2300, 2200, 1820, 1910, 2340, 2900, 3300],
      pieChartData: [
        { value: 45, name: '已批准' },
        { value: 20, name: '审核中' },
        { value: 15, name: '已拒绝' },
        { value: 20, name: '已还清' }
      ],
      barChartData: [65000, 89000, 78000, 95000, 120000, 110000, 135000, 145000, 128000, 160000, 180000, 195000],
      barChartMonthCount: new Date().getMonth() + 1,
      pieChart2Data: [
        { value: 35, name: 'VIP用户' },
        { value: 45, name: '普通用户' },
        { value: 20, name: '新用户' }
      ]
    },
    // 图表实例
    lineChart: null,
    pieChart: null,
    barChart: null,
    pieChart2: null,
    dashboardLineChart: null,
    dashboardPieChart: null,
    // 设置页面标签
    settingsTabs: [
      { id: 'basic', name: '基本设置' },
      { id: 'loan', name: '贷款设置' },
      { id: 'notification', name: '通知设置' },
      { id: 'security', name: '安全设置' }
    ],
    currentSettingsTab: 'basic',
    // 设置数据
    settings: {
      basic: {
        systemName: '闪借贷款管理系统',
        version: '1.0.0',
        description: '专业的个人贷款管理平台',
        phone: '400-888-8888',
        email: 'support@shanjie.com',
        address: '北京市朝阳区金融街88号'
      },
      loan: {
        minRate: 4.5,
        maxRate: 18.0,
        defaultRate: 10.0,
        minAmount: 1000,
        maxAmount: 500000,
        minTerm: 1,
        maxTerm: 36
      },
      notification: {
        remindDays: 3,
        overdueInterval: 24,
        sms: true,
        email: true,
        wechat: false
      },
      security: {
        minPasswordLength: 8,
        passwordExpireDays: 90,
        requireUpper: true,
        requireLower: true,
        requireNumber: true,
        requireSpecial: false,
        maxLoginAttempts: 5,
        lockoutMinutes: 15,
        enableCaptcha: false,
        enableTwoFactor: false
      }
    }
    }
  },
  computed: {
    dashboardPieLegend() {
      const colors = ['#7b5cf2', '#fbbf24', '#f87171', '#34d399']
      return (this.chartData.pieChartData || [])
        .filter(item => Number(item.value) > 0)
        .map((item, index) => ({
          name: item.name,
          value: item.value,
          color: colors[index % colors.length]
        }))
    },
    filteredUsers() {
      if (!this.searchKeyword) {
        return this.users
      }
      const keyword = this.searchKeyword.toLowerCase()
      return this.users.filter(user => 
        user.name.toLowerCase().includes(keyword) ||
        user.phone.includes(keyword) ||
        user.email.toLowerCase().includes(keyword)
      )
    },
    filteredLoans() {
      let result = [...this.loans]
      
      // 按状态筛选
      if (this.loanFilter.status) {
        result = result.filter(loan => loan.status === this.loanFilter.status)
      }
      
      // 按关键词筛选
      if (this.loanFilter.keyword) {
        const keyword = this.loanFilter.keyword.toLowerCase()
        result = result.filter(loan => 
          loan.applicant.toLowerCase().includes(keyword)
        )
      }
      
      return result
    },
    filteredContracts() {
      let result = [...this.contracts]
      
      // 按状态筛选
      if (this.contractFilter.status) {
        result = result.filter(contract => contract.status === this.contractFilter.status)
      }
      
      // 按关键词筛选
      if (this.contractFilter.keyword) {
        const keyword = this.contractFilter.keyword.toLowerCase()
        result = result.filter(contract => 
          contract.applicant.toLowerCase().includes(keyword) ||
          contract.contractNo.toLowerCase().includes(keyword)
        )
      }
      
      return result
    },
    filteredRepayments() {
      let result = [...this.repayments]
      
      // 按状态筛选
      if (this.repaymentFilter.status) {
        result = result.filter(repayment => repayment.status === this.repaymentFilter.status)
      }
      
      // 按关键词筛选
      if (this.repaymentFilter.keyword) {
        const keyword = this.repaymentFilter.keyword.toLowerCase()
        result = result.filter(repayment => 
          repayment.applicant.toLowerCase().includes(keyword) ||
          repayment.loanId.toString().includes(keyword)
        )
      }
      
      return result
    },
    totalLoanAmount() {
      return this.loans.reduce((sum, loan) => sum + loan.amount, 0)
    },
    loanCount() {
      return this.loans.length
    },
    totalRepaid() {
      return this.repayments.filter(r => r.status === 'paid').reduce((sum, r) => sum + r.amount, 0)
    },
    overdueRate() {
      const total = this.repayments.length
      const overdue = this.repayments.filter(r => r.status === 'overdue').length
      return total > 0 ? ((overdue / total) * 100).toFixed(1) : '0.0'
    }
  },
  methods: {
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    formatMoney(value) {
      return Number(value || 0).toLocaleString()
    },
    async loadDashboardData() {
      try {
        const stats = await fetchDashboardStats()
        this.dashboardStats = {
          totalUsers: stats.totalUsers || 0,
          totalAmount: stats.totalAmount || 0,
          todayNew: stats.todayNew || 0,
          overdueRate: stats.overdueRate || 0
        }
        this.recentLoans = await fetchRecentLoans(5)
        await this.loadChartData()
        this.$nextTick(() => {
          if (this.currentPage === 'dashboard') {
            this.initDashboardCharts()
          }
        })
      } catch (error) {
        console.error('加载控制台数据失败', error)
      }
    },
    async loadUsers() {
      try {
        this.users = await fetchUsers(this.searchKeyword)
      } catch (error) {
        console.error('加载用户失败', error)
      }
    },
    async loadLoans() {
      try {
        this.loans = await fetchLoans(this.loanFilter.status, this.loanFilter.keyword)
      } catch (error) {
        console.error('加载贷款失败', error)
      }
    },
    async loadContracts() {
      try {
        this.contracts = await fetchContracts(this.contractFilter.status, this.contractFilter.keyword)
      } catch (error) {
        console.error('加载合同失败', error)
      }
    },
    async loadProducts() {
      try {
        this.products = await fetchProducts(this.productFilter.keyword)
      } catch (error) {
        console.error('加载产品失败', error)
      }
    },
    openProductModal(product = null) {
      if (product) {
        this.isEditingProduct = true
        this.productForm = { ...product }
      } else {
        this.isEditingProduct = false
        this.productForm = {
          id: null,
          productNo: '',
          productName: '',
          productType: '个人消费贷',
          minAmount: 1000,
          maxAmount: 500000,
          minTerm: 3,
          maxTerm: 36,
          interestRate: 12,
          productDescription: '',
          status: 'active'
        }
      }
      this.showProductModal = true
    },
    closeProductModal() {
      this.showProductModal = false
    },
    async saveProduct() {
      try {
        const payload = { ...this.productForm }
        if (this.isEditingProduct) {
          await updateProduct(this.productForm.id, payload)
        } else {
          await createProduct(payload)
        }
        await this.loadProducts()
        this.closeProductModal()
      } catch (error) {
        alert(error.message || '保存产品失败')
      }
    },
    async confirmDeleteProduct(id) {
      if (!confirm('确定要删除该产品吗？')) return
      try {
        await apiDeleteProduct(id)
        await this.loadProducts()
      } catch (error) {
        alert(error.message || '删除产品失败')
      }
    },
    async handleGenerateContract(contract) {
      try {
        const loanId = contract.loanId || contract.id
        await generateContract(loanId)
        await this.loadContracts()
        this.closeContractDetail()
        alert('合同已生成')
      } catch (error) {
        alert(error.message || '生成合同失败')
      }
    },
    async handleSignContract(contract) {
      try {
        await apiSignContract(contract.id)
        await this.loadContracts()
        if (this.showContractDetail) {
          this.currentContract = (await fetchContracts()).find(c => c.id === contract.id) || contract
        }
        alert('合同签署成功')
      } catch (error) {
        alert(error.message || '签署失败')
      }
    },
    async handleDisburseContract(contract) {
      if (!confirm(`确认向 ${contract.applicant} 放款 ¥${Number(contract.amount).toLocaleString()} 吗？`)) return
      try {
        await apiDisburseContract(contract.id)
        await Promise.all([this.loadContracts(), this.loadLoans(), this.loadDashboardData()])
        this.closeContractDetail()
        alert('放款成功')
      } catch (error) {
        alert(error.message || '放款失败')
      }
    },
    async loadRepayments() {
      try {
        this.repayments = await fetchRepayments()
      } catch (error) {
        console.error('加载还款计划失败', error)
      }
    },
    async loadChartData() {
      try {
        const data = await fetchChartData()
        if (data.lineChartData) this.chartData.lineChartData = data.lineChartData
        if (data.barChartData) this.chartData.barChartData = data.barChartData
        if (data.barChartMonthCount) this.chartData.barChartMonthCount = data.barChartMonthCount
        if (data.pieChartData) this.chartData.pieChartData = data.pieChartData
        if (data.pieChart2Data) this.chartData.pieChart2Data = data.pieChart2Data
      } catch (error) {
        console.error('加载图表数据失败', error)
      }
    },
    async loadSettings() {
      try {
        const data = await fetchPortalSettings()
        if (data) this.settings = data
      } catch (error) {
        console.error('加载设置失败', error)
      }
    },
    async loadAllData() {
      const admin = getAdminUser()
      if (admin && admin.name) {
        this.adminName = admin.name
      }
      await Promise.all([
        this.loadDashboardData(),
        this.loadUsers(),
        this.loadLoans(),
        this.loadContracts(),
        this.loadRepayments(),
        this.loadChartData(),
        this.loadProducts(),
        this.loadSettings()
      ])
    },
    openLoanById(loanId) {
      const loan = this.loans.find(item => item.id === loanId)
      if (loan) {
        this.openLoanDetail(loan)
        return
      }
      this.currentPage = 'loans'
      this.loadLoans().then(() => {
        const target = this.loans.find(item => item.id === loanId)
        if (target) this.openLoanDetail(target)
      })
    },
    async handleLogout() {
      await apiLogout()
      clearAuth()
      this.$emit('logout')
    },
    openUserModal(user = null) {
      if (user) {
        this.isEditing = true
        this.formData = { ...user }
      } else {
        this.isEditing = false
        this.formData = {
          id: '',
          name: '',
          phone: '',
          email: '',
          status: 'active'
        }
      }
      this.showUserModal = true
    },
    closeUserModal() {
      this.showUserModal = false
    },
    async saveUser() {
      try {
        const payload = {
          name: this.formData.name,
          phone: this.formData.phone,
          email: this.formData.email,
          status: this.formData.status
        }
        if (this.isEditing) {
          await updateUser(this.formData.id, payload)
        } else {
          await createUser(payload)
        }
        await this.loadUsers()
        this.closeUserModal()
      } catch (error) {
        alert(error.message || '保存用户失败')
      }
    },
    confirmDelete(userId) {
      this.deleteUserId = userId
      this.showDeleteModal = true
    },
    closeDeleteModal() {
      this.showDeleteModal = false
    },
    async deleteUser() {
      try {
        await apiDeleteUser(this.deleteUserId)
        await this.loadUsers()
        this.closeDeleteModal()
      } catch (error) {
        alert(error.message || '删除用户失败')
      }
    },
    // 贷款相关方法
    openLoanDetail(loan) {
      this.currentLoan = { ...loan }
      this.showLoanDetail = true
    },
    closeLoanDetail() {
      this.showLoanDetail = false
      this.currentLoan = {}
    },
    riskPassedText(loan) {
      if (loan.riskPassed === true) return '通过'
      if (loan.riskPassed === false) return '未通过'
      return '未评估'
    },
    riskPassedClass(loan) {
      if (loan.riskPassed === true) return 'risk-pass'
      if (loan.riskPassed === false) return 'risk-fail'
      return 'risk-unknown'
    },
    riskLevelText(level) {
      const map = { 1: '低', 2: '中', 3: '高', 4: '极高' }
      return map[level] || '-'
    },
    formatScoringCard(loan) {
      if (loan.scoringCardPoints == null) return '-'
      return `${loan.scoringCardPoints}/${loan.scoringCardMax || 100}`
    },
    async approveLoan(loan) {
      const target = typeof loan === 'object' ? loan : this.loans.find(item => item.id === loan)
      const loanId = target ? target.id : loan
      if (target && target.riskPassed === false) {
        const reason = target.rejectReason ? `\n拒绝原因：${target.rejectReason}` : ''
        const confirmed = window.confirm(
          `该申请风控未通过，建议拒绝。${reason}\n\n确定仍要通过审批吗？`
        )
        if (!confirmed) return
      }
      try {
        await apiApproveLoan(loanId)
        if (this.showLoanDetail && this.currentLoan.id === loanId) {
          this.closeLoanDetail()
        }
        await this.loadLoans()
        await this.loadDashboardData()
        await this.loadContracts()
      } catch (error) {
        alert(error.message || '审批失败')
      }
    },
    async rejectLoan(loanId) {
      try {
        await apiRejectLoan(loanId)
        if (this.showLoanDetail && this.currentLoan.id === loanId) {
          this.closeLoanDetail()
        }
        await this.loadLoans()
        await this.loadDashboardData()
      } catch (error) {
        alert(error.message || '拒绝失败')
      }
    },
    // 合同相关方法
    openContractDetail(contract) {
      this.currentContract = { ...contract }
      this.showContractDetail = true
    },
    closeContractDetail() {
      this.showContractDetail = false
      this.currentContract = {}
    },
    formatAmount(amount) {
      const units = ['元', '拾', '佰', '仟', '万', '拾', '佰', '仟', '亿']
      const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
      const str = amount.toString().split('').reverse()
      let result = ''
      for (let i = 0; i < str.length; i++) {
        result = digits[parseInt(str[i])] + units[i] + result
      }
      return result.replace(/零 (拾 | 佰 | 仟)/g, '零').replace (/零{2,}/g, '零').replace(/零 (万 | 亿)/g, '$1').replace(/零元$/, '元')
    },
    calculateDueDate(baseDate, offset) {
      if (!baseDate) return ''
      const date = new Date(baseDate)
      date.setMonth(date.getMonth() + offset)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    // 还款相关方法
    async markAsPaid(repaymentId) {
      try {
        await confirmRepayment(repaymentId)
        await this.loadRepayments()
        await this.loadChartData()
        if (this.lineChart) this.updateLineChart()
        if (this.pieChart) this.updatePieChart()
        if (this.barChart) this.updateBarChart()
        this.closeRepaymentDetail()
      } catch (error) {
        alert(error.message || '确认还款失败')
      }
    },
    openRepaymentDetail(repayment) {
      this.currentRepayment = { ...repayment }
      this.showRepaymentDetail = true
    },
    closeRepaymentDetail() {
      this.showRepaymentDetail = false
      this.currentRepayment = {}
    },
    // 图表相关方法
    disposeDashboardCharts() {
      if (this.dashboardLineChart) {
        this.dashboardLineChart.dispose()
        this.dashboardLineChart = null
      }
      if (this.dashboardPieChart) {
        this.dashboardPieChart.dispose()
        this.dashboardPieChart = null
      }
    },
    disposeStatisticsCharts() {
      if (this.lineChart) {
        this.lineChart.dispose()
        this.lineChart = null
      }
      if (this.pieChart) {
        this.pieChart.dispose()
        this.pieChart = null
      }
      if (this.barChart) {
        this.barChart.dispose()
        this.barChart = null
      }
      if (this.pieChart2) {
        this.pieChart2.dispose()
        this.pieChart2 = null
      }
    },
    initCharts() {
      this.disposeStatisticsCharts()
      this.$nextTick(() => {
        if (!this.$refs.lineChart) return
        this.lineChart = echarts.init(this.$refs.lineChart)
        this.pieChart = echarts.init(this.$refs.pieChart)
        this.barChart = echarts.init(this.$refs.barChart)
        this.pieChart2 = echarts.init(this.$refs.pieChart2)
        this.updateLineChart()
        this.updatePieChart()
        this.updateBarChart()
        this.updatePieChart2()
        this.$nextTick(() => this.handleResize())
      })
    },
    initDashboardCharts() {
      this.disposeDashboardCharts()
      this.$nextTick(() => {
        if (this.$refs.dashboardLineChart) {
          this.dashboardLineChart = echarts.init(this.$refs.dashboardLineChart)
        }
        if (this.$refs.dashboardPieChart) {
          this.dashboardPieChart = echarts.init(this.$refs.dashboardPieChart)
        }
        this.updateDashboardCharts()
      })
    },
    updateDashboardCharts() {
      const lineOption = {
        tooltip: { trigger: 'axis' },
        grid: { left: 56, right: 56, top: 28, bottom: 56, containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
          axisLabel: { fontSize: 10, interval: 0, margin: 10, color: '#666' },
          axisLine: { lineStyle: { color: '#e5e5e5' } },
          axisTick: { alignWithLabel: true }
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLabel: { fontSize: 10, color: '#666' },
          splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
        },
        series: [{
          name: '贷款笔数',
          type: 'line',
          smooth: true,
          data: this.chartData.lineChartData,
          lineStyle: { width: 3, color: '#d8b4fe' },
          itemStyle: { color: '#d8b4fe' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(216, 180, 254, 0.3)' },
                { offset: 1, color: 'rgba(216, 180, 254, 0.05)' }
              ]
            }
          }
        }]
      }
      if (this.dashboardLineChart) {
        this.dashboardLineChart.setOption(lineOption, true)
      }

      const pieData = (this.chartData.pieChartData || []).filter(item => Number(item.value) > 0)
      const pieOption = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { show: false },
        series: [{
          name: '贷款状态',
          type: 'pie',
          center: ['50%', '50%'],
          radius: ['42%', '68%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          labelLine: { show: false },
          emphasis: {
            label: { show: true, fontSize: 13, fontWeight: 'bold' }
          },
          data: pieData.length > 0 ? pieData : [{ value: 1, name: '暂无数据' }],
          color: ['#7b5cf2', '#fbbf24', '#f87171', '#34d399']
        }]
      }
      if (this.dashboardPieChart) {
        this.dashboardPieChart.setOption(pieOption, true)
      }
      this.$nextTick(() => {
        this.handleResize()
        setTimeout(() => this.handleResize(), 150)
      })
    },
    updateLineChart() {
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '贷款笔数',
            type: 'line',
            data: this.chartData.lineChartData,
            smooth: true,
            lineStyle: {
              width: 3,
              color: '#d8b4fe'
            },
            itemStyle: {
              color: '#d8b4fe'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(216, 180, 254, 0.3)' },
                  { offset: 1, color: 'rgba(216, 180, 254, 0.05)' }
                ]
              }
            }
          }
        ]
      }
      if (this.lineChart) {
        this.lineChart.setOption(option)
      }
    },
    updatePieChart() {
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '贷款状态',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 18,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: this.chartData.pieChartData,
            color: ['#d8b4fe', '#fbbf24', '#f87171', '#60a5fa']
          }
        ]
      }
      if (this.pieChart) {
        this.pieChart.setOption(option)
      }
    },
    updateBarChart() {
      const monthCount = this.chartData.barChartMonthCount || new Date().getMonth() + 1
      const monthLabels = Array.from({ length: monthCount }, (_, i) => `${i + 1}月`)
      const barData = (this.chartData.barChartData || [])
        .slice(0, monthCount)
        .map(v => Math.round(Number(v) || 0))
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: (params) => {
            const item = params[0]
            return `${item.name}<br/>还款金额：¥${Number(item.value).toLocaleString()}`
          }
        },
        grid: {
          left: 56,
          right: 32,
          top: 32,
          bottom: 48,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: monthLabels,
          axisLabel: { fontSize: 11, color: '#666' }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: (value) => (value >= 10000 ? `${(value / 10000).toFixed(1)}万` : value)
          },
          splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
        },
        series: [
          {
            name: '还款金额',
            type: 'bar',
            barMaxWidth: 48,
            data: barData,
            itemStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: '#d8b4fe' },
                  { offset: 1, color: '#a855f7' }
                ]
              },
              borderRadius: [6, 6, 0, 0]
            }
          }
        ]
      }
      if (this.barChart) {
        this.barChart.setOption(option, true)
      }
    },
    updatePieChart2() {
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}%'
        },
        series: [
          {
            name: '用户等级',
            type: 'pie',
            radius: '55%',
            center: ['50%', '50%'],
            data: this.chartData.pieChart2Data,
            color: ['#fbbf24', '#d8b4fe', '#60a5fa'],
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}: {d}%'
            }
          }
        ]
      }
      if (this.pieChart2) {
        this.pieChart2.setOption(option)
      }
    },
    handleResize() {
      if (this.lineChart) this.lineChart.resize()
      if (this.pieChart) this.pieChart.resize()
      if (this.barChart) this.barChart.resize()
      if (this.pieChart2) this.pieChart2.resize()
      if (this.dashboardLineChart) this.dashboardLineChart.resize()
      if (this.dashboardPieChart) this.dashboardPieChart.resize()
    },
    async refreshCharts() {
      await this.loadChartData()
      this.updateLineChart()
      this.updatePieChart()
      this.updateBarChart()
      this.updatePieChart2()
      this.updateDashboardCharts()
    },
    // 系统设置方法
    async saveSettings() {
      try {
        await savePortalSettings(this.settings)
        alert('设置已保存！')
      } catch (error) {
        alert(error.message || '保存设置失败')
      }
    },
    async resetSettings() {
      try {
        this.settings = await resetPortalSettings()
        alert('设置已重置！')
      } catch (error) {
        alert(error.message || '重置设置失败')
      }
    }
  },
  // 生命周期钩子
  mounted() {
    this.loadAllData()
    if (this.currentPage === 'statistics') {
      this.initCharts()
    }
    if (this.currentPage === 'dashboard') {
      this.$nextTick(() => this.initDashboardCharts())
    }
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize)
    if (this.lineChart) this.lineChart.dispose()
    if (this.pieChart) this.pieChart.dispose()
    if (this.barChart) this.barChart.dispose()
    if (this.pieChart2) this.pieChart2.dispose()
    if (this.dashboardLineChart) this.dashboardLineChart.dispose()
    if (this.dashboardPieChart) this.dashboardPieChart.dispose()
  },
  watch: {
    currentPage(newPage, oldPage) {
      if (oldPage === 'dashboard') {
        this.disposeDashboardCharts()
      }
      if (oldPage === 'statistics') {
        this.disposeStatisticsCharts()
      }
      if (newPage === 'statistics') {
        this.loadChartData().then(() => {
          setTimeout(() => this.initCharts(), 80)
        })
      }
      if (newPage === 'dashboard') {
        this.loadDashboardData().then(() => {
          setTimeout(() => this.initDashboardCharts(), 80)
        })
      }
      if (newPage === 'users') this.loadUsers()
      if (newPage === 'products') this.loadProducts()
      if (newPage === 'loans') this.loadLoans()
      if (newPage === 'contracts') this.loadContracts()
      if (newPage === 'repayment') this.loadRepayments()
    }
  }
}
</script>

<style scoped>
/* 主容器 */
.app-container {
  font-family: Arial, sans-serif;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.top-nav {
  background-color: #d8b4fe;
  color: white;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-left .app-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.nav-right .user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-right .username {
  font-weight: bold;
}

.nav-right .logout-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.nav-right .logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  background-color: #f5f5f5;
}

/* 左侧菜单栏 */
.sidebar {
  width: 220px;
  background: linear-gradient(to bottom, #d8b4fe, #c084fc);
  color: white;
  padding: 20px 0;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.1);
}

.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 5px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-left-color: white;
}

.nav-item.active .nav-link {
  background-color: rgba(255, 255, 255, 0.2);
  border-left-color: white;
  font-weight: bold;
}

.nav-icon {
  margin-right: 10px;
  font-size: 18px;
}

/* 右侧内容区 */
.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.content-header {
  margin-bottom: 20px;
}

.content-header h2 {
  margin: 0 0 5px 0;
  color: #333;
}

.welcome-message {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 数据卡片 */
.data-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.card-title {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.card-value {
  color: #d8b4fe;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.card-trend {
  font-size: 12px;
  font-weight: bold;
}

.card-trend.up {
  color: #4caf50;
}

.card-trend.down {
  color: #f44336;
}

/* 图表区域 */
.charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
  align-items: stretch;
}

.chart-container {
  background-color: white;
  border-radius: 8px;
  padding: 16px 20px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.chart-container h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
  flex-shrink: 0;
  text-align: center;
}

.chart-container-echarts {
  flex: none;
  height: 340px;
  width: 100%;
  box-sizing: border-box;
  margin: 0 auto;
}

.chart-container-pie .pie-panel {
  display: flex;
  flex-direction: column;
  height: 340px;
}

.chart-pie-ring {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.pie-legend-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 8px 16px;
  margin: 0;
  padding: 10px 8px 4px;
  list-style: none;
  font-size: 12px;
  color: #666;
  flex-shrink: 0;
}

.pie-legend-row li {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.pie-legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: end;
  gap: 10px;
  padding: 10px 0;
}

.chart-bar {
  flex: 1;
  background-color: #d8b4fe;
  border-radius: 4px 4px 0 0;
  min-height: 20px;
  transition: height 1s ease-in-out;
}

.pie-chart {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-slice {
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: #d8b4fe;
  clip-path: polygon(50% 50%, 50% 0%, 100% 0%, 100% 100%, 50% 100%);
  transform-origin: 50% 50%;
  transform: rotate(var(--rotate));
  opacity: 0.8;
}

.pie-slice:nth-child(2) {
  background-color: #c084fc;
}

.pie-slice:nth-child(3) {
  background-color: #a855f7;
}

.pie-slice:nth-child(4) {
  background-color: #9333ea;
}

/* 最近贷款列表 */
.recent-loans {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.recent-loans h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 16px;
}

.loan-table table {
  width: 100%;
  border-collapse: collapse;
}

.loan-table th,
.loan-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.loan-table th {
  background-color: #f9f9f9;
  font-weight: bold;
  color: #333;
}

.loan-table tr:hover {
  background-color: #f5f5f5;
}

.status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status.approved,
.status.disbursed,
.status.paid {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.status.pending {
  background-color: #fff3e0;
  color: #ef6c00;
}

.status.rejected {
  background-color: #ffebee;
  color: #c62828;
}

.action-btn {
  background-color: #d8b4fe;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.action-btn:hover {
  background-color: #c084fc;
}

/* 用户管理页面样式 */
.user-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 300px;
}

.search-btn {
  background-color: #d8b4fe;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #c084fc;
}

.add-user-btn {
  background-color: #d8b4fe;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.add-user-btn:hover {
  background-color: #c084fc;
}

.user-table-container {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.user-table th {
  background-color: #f9f9f9;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
}

.user-table tr:hover {
  background-color: #f5f5f5;
}

.edit-btn {
  margin-right: 8px;
}

.delete-btn {
  background-color: #f44336;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.status.active {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.status.inactive {
  background-color: #ffebee;
  color: #c62828;
}

/* 贷款管理页面样式 */
.loan-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 10px;
}

.filter-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 150px;
}

.loan-table-container {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
  margin-top: 20px;
}

.loan-table {
  width: 100%;
  border-collapse: collapse;
}

.loan-table th,
.loan-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.loan-table th {
  background-color: #f9f9f9;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
}

.loan-table tr:hover {
  background-color: #f5f5f5;
}

.view-btn {
  background-color: #d8b4fe;
  margin-right: 8px;
}

.view-btn:hover {
  background-color: #c084fc;
}

.approve-btn {
  background-color: #4caf50;
  margin-right: 8px;
}

.approve-btn:hover {
  background-color: #43a047;
}

.reject-btn {
  background-color: #f44336;
}

.reject-btn:hover {
  background-color: #d32f2f;
}

.risk-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
  white-space: nowrap;
}

.risk-badge.risk-pass {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.risk-badge.risk-fail {
  background-color: #ffebee;
  color: #c62828;
}

.risk-badge.risk-unknown {
  background-color: #f5f5f5;
  color: #757575;
}

.risk-level-tag {
  display: inline-block;
  margin-left: 4px;
  padding: 1px 6px;
  border-radius: 8px;
  font-size: 11px;
  background-color: #ede7f6;
  color: #5e35b1;
}

.risk-reason-cell {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
  font-size: 12px;
}

.risk-failed-row {
  background-color: #fff8f8;
}

.risk-failed-row:hover {
  background-color: #ffefef;
}

.loan-actions-cell {
  min-width: 120px;
}

.risk-approve-hint {
  font-size: 11px;
  color: #c62828;
  background-color: #ffebee;
  border: 1px solid #ffcdd2;
  border-radius: 4px;
  padding: 4px 6px;
  margin-bottom: 6px;
  line-height: 1.3;
}

.risk-detail-section {
  border-color: #e1bee7;
  background-color: #faf5ff;
}

.risk-detail-alert {
  background-color: #ffebee;
  color: #b71c1c;
  border: 1px solid #ffcdd2;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 12px;
  font-size: 13px;
}

.reject-reason-text {
  color: #c62828;
  word-break: break-all;
}

.text-muted {
  color: #999;
}

.modal-hint {
  flex: 1;
  margin-right: 12px;
  margin-bottom: 0;
}

.modal-action-btn {
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  font-size: 14px;
  margin-right: 8px;
}

.loan-table-container {
  overflow-x: auto;
}

.status.paid {
  background-color: #e3f2fd;
  color: #1565c0;
}

/* 贷款详情弹窗样式 */
.loan-detail-modal {
  max-width: 700px;
  max-height: 80vh;
}

.loan-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 16px;
  background-color: #f9f9f9;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 16px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 8px;
}

.detail-row {
  display: flex;
  margin-bottom: 8px;
  align-items: center;
}

.detail-label {
  width: 120px;
  font-weight: bold;
  color: #666;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #333;
}

.document-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.document-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #eee;
}

.document-name {
  color: #333;
}

.view-doc-btn {
  background-color: #d8b4fe;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.view-doc-btn:hover {
  background-color: #c084fc;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.delete-modal {
  max-width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.close-btn:hover {
  background-color: #f5f5f5;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: bold;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #d8b4fe;
  box-shadow: 0 0 0 2px rgba(216, 180, 254, 0.2);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  background-color: #f9f9f9;
  border-radius: 0 0 8px 8px;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.save-btn {
  background-color: #d8b4fe;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.save-btn:hover {
  background-color: #c084fc;
}

.delete-confirm-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.delete-confirm-btn:hover {
  background-color: #d32f2f;
}

/* 占位内容样式 */
.placeholder-content {
  background-color: white;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.placeholder-content p {
  color: #666;
  font-size: 16px;
}

/* 合同管理页面样式 */
.contract-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 10px;
}

.contract-table-container {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
  margin-top: 20px;
}

.contract-table {
  width: 100%;
  border-collapse: collapse;
}

.contract-table th,
.contract-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.contract-table th {
  background-color: #f9f9f9;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
}

.contract-table tr:hover {
  background-color: #f5f5f5;
}

.status.completed {
  background-color: #e8f5e8;
  color: #2e7d32;
}

/* 合同详情弹窗样式 */
.contract-detail-modal {
  max-width: 700px;
  max-height: 80vh;
}

.contract-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.contract-summary {
  background-color: white;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.contract-summary p {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
}

.contract-summary p:last-child {
  margin-bottom: 0;
}

/* 还款计划页面样式 */
.repayment-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 10px;
}

.repayment-table-container {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
  margin-top: 20px;
}

.repayment-table {
  width: 100%;
  border-collapse: collapse;
}

.repayment-table th,
.repayment-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.repayment-table th {
  background-color: #f9f9f9;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
}

.repayment-table tr:hover {
  background-color: #f5f5f5;
}

.repayment-table tr.overdue {
  background-color: #fff5f5;
}

.repayment-table tr.overdue td {
  color: #c62828;
}

.status.overdue {
  background-color: #ffebee;
  color: #c62828;
}

.status.pending {
  background-color: #fff3e0;
  color: #ef6c00;
}

.status.paid {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.pay-btn {
  background-color: #4caf50;
  margin-right: 8px;
}

.pay-btn:hover {
  background-color: #43a047;
}

/* 数据统计页面样式 */
.statistics-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 15px;
  margin-top: 10px;
}

.refresh-btn {
  background-color: #d8b4fe;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.refresh-btn:hover {
  background-color: #c084fc;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(135deg, #d8b4fe 0%, #c084fc 100%);
  border-radius: 12px;
  padding: 24px;
  color: white;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 15px rgba(216, 180, 254, 0.3);
}

.stat-icon {
  font-size: 40px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.chart-wrapper {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  min-width: 0;
}

.chart-wrapper h3 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 16px;
  text-align: center;
}

.chart-wrapper-full {
  grid-column: 1 / -1;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart-container-sm {
  height: 280px;
}

.chart-container-bar {
  height: 380px;
}

/* 还款详情弹窗样式 */
.repayment-detail-modal {
  max-width: 800px;
  max-height: 80vh;
}

.repayment-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.repayment-schedule {
  margin-top: 10px;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.schedule-table th,
.schedule-table td {
  padding: 8px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.schedule-table th {
  background-color: #f9f9f9;
  font-weight: bold;
  color: #333;
}

.schedule-table tr:hover {
  background-color: #f5f5f5;
}

/* 系统设置页面样式 */
.settings-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.tab-btn {
  background-color: #f5f5f5;
  color: #666;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-btn:hover {
  background-color: #e8e8e8;
}

.tab-btn.active {
  background-color: #d8b4fe;
  color: white;
}

.settings-container {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.settings-section {
  margin-bottom: 30px;
}

.settings-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #d8b4fe;
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.settings-form .form-group {
  flex: 1;
  margin-bottom: 0;
}

.settings-form .form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 600;
  font-size: 14px;
}

.settings-form .form-group input,
.settings-form .form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.settings-form .form-group input:focus,
.settings-form .form-group textarea:focus {
  outline: none;
  border-color: #d8b4fe;
  box-shadow: 0 0 0 2px rgba(216, 180, 254, 0.2);
}

.settings-form .form-group input[readonly] {
  background-color: #f5f5f5;
  color: #999;
}

.settings-form .form-group textarea {
  resize: vertical;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
  cursor: pointer;
}

.settings-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.settings-actions .save-btn {
  background-color: #d8b4fe;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.settings-actions .save-btn:hover {
  background-color: #c084fc;
}

.settings-actions .cancel-btn {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.settings-actions .cancel-btn:hover {
  background-color: #e8e8e8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 60px;
  }
  
  .nav-text {
    display: none;
  }
  
  .nav-icon {
    margin-right: 0;
  }
  
  .data-cards {
    grid-template-columns: 1fr;
  }
  
  .charts {
    grid-template-columns: 1fr;
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-wrapper-full {
    grid-column: 1;
  }
  .chart-container {
    min-width: auto;
  }
  
  .user-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-input {
    width: 100%;
  }
  
  .search-box {
    width: 100%;
  }
  
  .user-table {
    font-size: 14px;
  }
  
  .user-table th,
  .user-table td {
    padding: 8px;
  }
  
  .modal-content {
    width: 95%;
  }
}
</style>