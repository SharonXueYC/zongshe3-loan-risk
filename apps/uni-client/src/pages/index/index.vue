<template>
  <view class="page">
    <view class="hero card">
      <text class="badge">ZONGSHE2 实际功能页</text>
      <text class="title">借贷系统前端（联调版）</text>
      <text class="subtitle">已接入后端接口：登录、产品列表、提交贷款申请、我的申请记录、信誉分评估、风控报告。</text>
    </view>

    <view class="card">
      <text class="section-title">1) 接口配置</text>
      <view class="field">
        <text class="label">后端地址</text>
        <input v-model="apiBase" class="input" placeholder="例如：http://localhost:8080" />
      </view>
      <view class="actions">
        <button class="btn secondary" @click="saveApiBase">保存地址</button>
      </view>
    </view>

    <view class="card">
      <text class="section-title">2) 用户登录</text>
      <view class="field">
        <text class="label">手机号</text>
        <input v-model="loginForm.phoneNumber" class="input" type="number" placeholder="输入手机号" />
      </view>
      <view class="field">
        <text class="label">密码</text>
        <input v-model="loginForm.password" class="input" password placeholder="输入密码" />
      </view>
      <view class="actions">
        <button class="btn primary" :disabled="loading.login" @click="doLogin">
          {{ loading.login ? '登录中...' : '登录' }}
        </button>
        <button class="btn secondary" @click="fillDemoAccount">填充演示账号</button>
      </view>
      <text v-if="auth.token" class="ok">登录成功，当前用户ID：{{ auth.userId || '未知' }}</text>
    </view>

    <view class="card">
      <text class="section-title">3) 信誉分与额度（需登录）</text>
      <view class="actions">
        <button class="btn primary" :disabled="loading.credit" @click="loadCreditEvaluation">
          {{ loading.credit ? '评估中...' : '刷新信誉分评估' }}
        </button>
      </view>
      <view v-if="creditInfo.creditScore" class="credit-box">
        <text class="item-title">信用分：{{ creditInfo.creditScore }} / {{ creditInfo.maxCreditScore || 850 }}</text>
        <text class="item-desc">等级：{{ creditInfo.creditLevel }} · 可借额度：{{ creditInfo.loanLimit }} 元</text>
        <text class="item-desc">贷款资格：{{ creditInfo.qualified ? '符合' : '不符合' }}</text>
      </view>
      <view v-else class="empty">点击按钮获取信誉分评估</view>
    </view>

    <view class="card">
      <text class="section-title">4) 产品列表（需登录）</text>
      <view class="actions">
        <button class="btn primary" :disabled="loading.products" @click="loadProducts">
          {{ loading.products ? '加载中...' : '刷新产品' }}
        </button>
      </view>
      <view v-if="products.length === 0" class="empty">暂无产品数据</view>
      <view v-else class="list">
        <view v-for="item in products" :key="item.id" class="list-item">
          <text class="item-title">{{ item.productName }}（{{ item.productType }}）</text>
          <text class="item-desc">额度：{{ item.minAmount }} - {{ item.maxAmount }}，利率：{{ item.interestRate }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <text class="section-title">5) 提交贷款申请（需登录）</text>
      <view class="field">
        <text class="label">贷款类型</text>
        <input v-model="loanForm.loanType" class="input" placeholder="如：个人消费贷款" />
      </view>
      <view class="field">
        <text class="label">贷款金额</text>
        <input v-model="loanForm.loanAmount" class="input" type="digit" placeholder="最少1000" />
      </view>
      <view class="field-row">
        <view class="field half">
          <text class="label">期限（月）</text>
          <input v-model="loanForm.loanTerm" class="input" type="number" placeholder="12" />
        </view>
        <view class="field half">
          <text class="label">还款方式</text>
          <input v-model="loanForm.repaymentMode" class="input" placeholder="equal / principal" />
        </view>
      </view>
      <view class="field">
        <text class="label">年化利率</text>
        <input v-model="loanForm.interestRate" class="input" type="digit" placeholder="8.5" />
      </view>
      <view class="field">
        <text class="label">申请说明</text>
        <textarea v-model="loanForm.description" class="textarea" placeholder="填写用途说明"></textarea>
      </view>
      <view class="actions">
        <button class="btn primary" :disabled="loading.submit" @click="submitLoan">
          {{ loading.submit ? '提交中...' : '提交申请' }}
        </button>
      </view>
    </view>

    <view class="card">
      <text class="section-title">6) 我的申请记录（需登录）</text>
      <view class="actions">
        <button class="btn primary" :disabled="loading.myApps" @click="loadMyApplications">
          {{ loading.myApps ? '加载中...' : '刷新我的申请' }}
        </button>
      </view>
      <view v-if="myApplications.length === 0" class="empty">暂无申请记录</view>
      <view v-else class="list">
        <view v-for="item in myApplications" :key="item.id" class="list-item" @click="loadRiskAssessment(item.id)">
          <text class="item-title">{{ item.applicationNo }} - {{ item.status }}</text>
          <text class="item-desc">金额：{{ item.loanAmount }}，期限：{{ item.loanTerm }}个月，类型：{{ item.loanType }}</text>
          <text v-if="item.riskScore != null" class="item-desc risk">
            风控：{{ item.riskPassed ? '通过' : '未通过' }} · 评分卡 {{ item.scoringCardPoints }}/{{ item.scoringCardMax }} · 风险分 {{ item.riskScore }}
          </text>
        </view>
      </view>
    </view>

    <view v-if="riskDetail.applicationId" class="card">
      <text class="section-title">风控评估详情</text>
      <text class="item-title">申请 #{{ riskDetail.applicationId }}</text>
      <text class="item-desc">综合风险分：{{ riskDetail.overallRiskScore }}，等级：{{ riskDetail.overallRiskLevel }}</text>
      <text class="item-desc">评分卡：{{ riskDetail.scoringCardPoints }}/{{ riskDetail.scoringCardMax }}（{{ riskDetail.cardVersion }}）</text>
      <text class="item-desc">结果：{{ riskDetail.passed ? '通过' : '未通过' }}{{ riskDetail.rejectReason ? ' - ' + riskDetail.rejectReason : '' }}</text>
    </view>

    <view class="card">
      <text class="section-title">状态输出</text>
      <text class="log">{{ statusMessage || '就绪' }}</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      apiBase: 'http://localhost:8080',
      loginForm: {
        phoneNumber: '',
        password: '',
      },
      loanForm: {
        loanType: '个人消费贷款',
        loanAmount: '5000',
        loanTerm: '12',
        repaymentMode: 'equal',
        interestRate: '8.5',
        description: '课程项目联调测试申请',
      },
      auth: {
        token: '',
        userId: '',
      },
      products: [],
      myApplications: [],
      creditInfo: {},
      riskDetail: {},
      statusMessage: '',
      loading: {
        login: false,
        products: false,
        submit: false,
        myApps: false,
        credit: false,
        risk: false,
      },
    }
  },
  onLoad() {
    const savedBase = uni.getStorageSync('apiBase')
    const token = uni.getStorageSync('token')
    const userId = uni.getStorageSync('userId')
    if (savedBase) this.apiBase = savedBase
    if (token) this.auth.token = token
    if (userId) this.auth.userId = userId
    if (this.auth.userId && this.auth.token) {
      this.loadCreditEvaluation()
    }
  },
  methods: {
    saveApiBase() {
      uni.setStorageSync('apiBase', this.apiBase)
      this.statusMessage = `已保存后端地址：${this.apiBase}`
      uni.showToast({ title: '已保存', icon: 'none' })
    },
    fillDemoAccount() {
      this.loginForm.phoneNumber = '13800138000'
      this.loginForm.password = '123456'
    },
    async request(path, options = {}) {
      const token = this.auth.token
      const headers = Object.assign(
        { 'Content-Type': 'application/json' },
        options.headers || {},
      )
      if (token) headers.Authorization = `Bearer ${token}`
      const url = `${this.apiBase}${path}`
      return new Promise((resolve, reject) => {
        uni.request({
          url,
          method: options.method || 'GET',
          data: options.data || undefined,
          header: headers,
          success: (res) => {
            if (res.statusCode >= 200 && res.statusCode < 300) {
              resolve(res.data || {})
            } else {
              reject(new Error((res.data && res.data.message) || `请求失败(${res.statusCode})`))
            }
          },
          fail: (err) => reject(new Error(err.errMsg || '网络请求失败')),
        })
      })
    },
    async doLogin() {
      if (!this.loginForm.phoneNumber || !this.loginForm.password) {
        uni.showToast({ title: '请填写手机号和密码', icon: 'none' })
        return
      }
      this.loading.login = true
      try {
        const data = await this.request('/api/users/login', {
          method: 'POST',
          data: {
            phoneNumber: this.loginForm.phoneNumber,
            password: this.loginForm.password,
          },
        })
        if (!data.success) {
          throw new Error(data.message || '登录失败')
        }
        this.auth.token = data.token || ''
        this.auth.userId = (data.userInfo && data.userInfo.userId) || ''
        uni.setStorageSync('token', this.auth.token)
        uni.setStorageSync('userId', this.auth.userId)
        this.statusMessage = `登录成功：${this.auth.userId}`
        uni.showToast({ title: '登录成功', icon: 'success' })
        await this.loadCreditEvaluation()
      } catch (e) {
        this.statusMessage = `登录失败：${e.message}`
        uni.showToast({ title: e.message, icon: 'none' })
      } finally {
        this.loading.login = false
      }
    },
    async loadCreditEvaluation() {
      if (!this.auth.userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      this.loading.credit = true
      try {
        const data = await this.request(`/api/credit/users/${this.auth.userId}/evaluation`)
        if (!data.success) throw new Error(data.message || '评估失败')
        this.creditInfo = data
        this.statusMessage = `信誉分：${data.creditScore}，额度：${data.loanLimit}`
      } catch (e) {
        this.statusMessage = `信誉分评估失败：${e.message}`
        uni.showToast({ title: e.message, icon: 'none' })
      } finally {
        this.loading.credit = false
      }
    },
    async loadRiskAssessment(applicationId) {
      this.loading.risk = true
      try {
        const res = await this.request(`/api/risk/applications/${applicationId}/assessment`)
        if (!res.success) throw new Error(res.message || '加载失败')
        this.riskDetail = res.data || {}
        this.riskDetail.applicationId = applicationId
        this.statusMessage = `风控评估已加载：申请 #${applicationId}`
      } catch (e) {
        this.statusMessage = `风控加载失败：${e.message}`
        uni.showToast({ title: e.message, icon: 'none' })
      } finally {
        this.loading.risk = false
      }
    },
    async loadProducts() {
      this.loading.products = true
      try {
        const data = await this.request('/api/products')
        this.products = data.data || []
        this.statusMessage = `产品加载成功，共 ${this.products.length} 条`
      } catch (e) {
        this.statusMessage = `产品加载失败：${e.message}`
        uni.showToast({ title: e.message, icon: 'none' })
      } finally {
        this.loading.products = false
      }
    },
    async submitLoan() {
      this.loading.submit = true
      try {
        const data = await this.request('/api/loan-applications/submit', {
          method: 'POST',
          data: {
            loanType: this.loanForm.loanType,
            loanAmount: Number(this.loanForm.loanAmount),
            loanTerm: Number(this.loanForm.loanTerm),
            repaymentMode: this.loanForm.repaymentMode,
            interestRate: Number(this.loanForm.interestRate),
            description: this.loanForm.description,
          },
        })
        if (!data.success) {
          throw new Error(data.message || '提交失败')
        }
        this.statusMessage = `申请提交成功：${data.applicationNo || '已提交'}`
        if (data.riskAssessment) {
          this.riskDetail = Object.assign({ applicationId: data.applicationId }, data.riskAssessment)
        }
        uni.showToast({ title: '提交成功', icon: 'success' })
        await this.loadMyApplications()
        await this.loadCreditEvaluation()
      } catch (e) {
        this.statusMessage = `申请提交失败：${e.message}`
        uni.showToast({ title: e.message, icon: 'none' })
      } finally {
        this.loading.submit = false
      }
    },
    async loadMyApplications() {
      if (!this.auth.userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      this.loading.myApps = true
      try {
        const data = await this.request(`/api/loan-applications/user/${this.auth.userId}`)
        this.myApplications = data.data || []
        this.statusMessage = `我的申请加载成功，共 ${this.myApplications.length} 条`
      } catch (e) {
        this.statusMessage = `我的申请加载失败：${e.message}`
        uni.showToast({ title: e.message, icon: 'none' })
      } finally {
        this.loading.myApps = false
      }
    },
  },
}
</script>

<style>
.page {
  min-height: 100vh;
  padding: 28rpx;
  background: linear-gradient(180deg, #f5f1ff 0%, #fcfbff 100%);
  color: #241b38;
}

.card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 12rpx 36rpx rgba(88, 58, 196, 0.12);
  margin-bottom: 24rpx;
}

.badge {
  display: inline-flex;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #efe8ff;
  color: #6f4ce6;
  font-size: 24rpx;
  margin-bottom: 18rpx;
}

.title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  line-height: 1.35;
}

.subtitle {
  display: block;
  margin-top: 16rpx;
  color: #6f6886;
  font-size: 28rpx;
  line-height: 1.7;
}

.section-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  margin-bottom: 24rpx;
}

.field {
  margin-bottom: 18rpx;
}

.field-row {
  display: flex;
  gap: 16rpx;
}

.half {
  flex: 1;
}

.label {
  display: block;
  font-size: 26rpx;
  color: #6f6886;
  margin-bottom: 10rpx;
}

.input,
.textarea {
  width: 100%;
  background: #faf8ff;
  border: 2rpx solid #eee8ff;
  border-radius: 18rpx;
  font-size: 28rpx;
  padding: 18rpx 20rpx;
}

.textarea {
  min-height: 140rpx;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 8rpx;
}

button {
  margin: 0;
}

.btn.primary {
  background: linear-gradient(135deg, #7b5cf2 0%, #9b7bff 100%);
  color: #fff;
}

.btn.secondary {
  background: #f5f1ff;
  color: #6f4ce6;
}

.btn {
  border: none;
  border-radius: 999rpx;
  font-size: 28rpx;
  padding: 0 28rpx;
  height: 84rpx;
  line-height: 84rpx;
}

.ok {
  display: block;
  margin-top: 12rpx;
  color: #1f8a4c;
  font-size: 26rpx;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.list-item {
  background: #faf8ff;
  border: 2rpx solid #eee8ff;
  border-radius: 18rpx;
  padding: 18rpx;
}

.item-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}

.item-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #6f6886;
}

.empty {
  color: #8f88a4;
  font-size: 26rpx;
}

.credit-box {
  background: #faf8ff;
  border: 2rpx solid #eee8ff;
  border-radius: 18rpx;
  padding: 18rpx;
}

.risk {
  color: #6f4ce6;
}

.log {
  display: block;
  background: #f8f7fb;
  border-radius: 16rpx;
  padding: 18rpx;
  font-size: 24rpx;
  color: #4b4560;
  line-height: 1.6;
}
</style>
