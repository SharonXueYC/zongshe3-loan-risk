import { request } from './client'

const LOAN_STATUS_TEXT = {
  pending: '审核中',
  approved: '已通过',
  rejected: '已拒绝',
  disbursed: '已放款',
  paid: '已还清',
  settled: '已还清'
}

function normalizeLoanStatus(status) {
  if (!status) return 'pending'
  const lower = status.toLowerCase()
  if (lower === 'approved') return 'approved'
  if (lower === 'rejected') return 'rejected'
  if (lower === 'disbursed') return 'disbursed'
  if (lower === 'settled' || lower === 'paid') return 'paid'
  return 'pending'
}

function mapLoan(item) {
  const status = normalizeLoanStatus(item.status)
  return {
    id: item.id,
    applicationNo: item.applicationNo || '',
    applicant: item.applicantName,
    amount: Number(item.loanAmount),
    term: item.loanTerm,
    interestRate: 8.5,
    applyTime: item.applyTime ? String(item.applyTime).slice(0, 10) : '',
    status,
    statusText: item.statusText || LOAN_STATUS_TEXT[status] || status,
    idCard: item.idCardNumber,
    phone: item.phoneNumber,
    email: '',
    address: item.description || '',
    loanType: item.loanType || '',
    riskPassed: item.riskPassed ?? null,
    riskScore: item.riskScore ?? null,
    riskLevel: item.riskLevel ?? null,
    scoringCardPoints: item.scoringCardPoints ?? null,
    scoringCardMax: item.scoringCardMax ?? 100,
    rejectReason: item.rejectReason || ''
  }
}

export async function login(username, password) {
  return request('/api/admin/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
}

export async function checkAuth() {
  return request('/api/admin/check-auth')
}

export async function logout() {
  try {
    await request('/api/admin/logout', { method: 'POST' })
  } catch (_) {
    // ignore logout errors
  }
}

export async function fetchUsers(search = '') {
  const query = search ? `?search=${encodeURIComponent(search)}` : ''
  const res = await request(`/api/admin/users${query}`)
  return res.data || []
}

export async function createUser(payload) {
  const res = await request('/api/admin/users', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
  return res.data
}

export async function updateUser(id, payload) {
  const res = await request(`/api/admin/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
  return res.data
}

export async function deleteUser(id) {
  await request(`/api/admin/users/${id}`, { method: 'DELETE' })
}

export async function fetchLoans(status = '', keyword = '') {
  const params = new URLSearchParams()
  if (status) params.set('status', status)
  if (keyword) params.set('search', keyword)
  const query = params.toString() ? `?${params.toString()}` : ''
  const res = await request(`/api/loan-applications${query}`)
  return (res.data || []).map(mapLoan)
}

export async function approveLoan(id) {
  await request(`/api/loan-applications/${id}/approve`, {
    method: 'PUT',
    body: JSON.stringify({ remark: '管理员审批通过' })
  })
}

export async function rejectLoan(id) {
  await request(`/api/loan-applications/${id}/reject`, {
    method: 'PUT',
    body: JSON.stringify({ remark: '管理员审批拒绝' })
  })
}

export async function fetchContracts(status = '', keyword = '') {
  const params = new URLSearchParams()
  if (status) params.set('status', status)
  if (keyword) params.set('search', keyword)
  const query = params.toString() ? `?${params.toString()}` : ''
  const res = await request(`/api/admin/contracts${query}`)
  return res.data || []
}

export async function fetchRepayments() {
  const res = await request('/api/repayment/admin/plans')
  return res.data || []
}

export async function confirmRepayment(planId) {
  await request(`/api/repayment/admin/plans/${planId}/confirm`, { method: 'PUT' })
}

export async function fetchDashboardStats() {
  const res = await request('/api/statistics/dashboard')
  return res.data || {}
}

export async function fetchRecentLoans(limit = 5) {
  const res = await request(`/api/statistics/recent-loans?limit=${limit}`)
  return res.data || []
}

export async function fetchChartData() {
  const res = await request('/api/statistics/charts')
  return res.data || {}
}

export async function fetchPortalSettings() {
  const res = await request('/api/settings/portal')
  return res.data
}

export async function savePortalSettings(settings) {
  const res = await request('/api/settings/portal', {
    method: 'PUT',
    body: JSON.stringify(settings)
  })
  return res.data
}

export async function resetPortalSettings() {
  const res = await request('/api/settings/portal/reset', { method: 'POST' })
  return res.data
}

export async function fetchProducts(search = '') {
  const params = new URLSearchParams()
  if (search) params.set('search', search)
  const query = params.toString() ? `?${params.toString()}` : ''
  const res = await request(`/api/products${query}`)
  return res.data || []
}

export async function createProduct(payload) {
  const res = await request('/api/products', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
  return res.data
}

export async function updateProduct(id, payload) {
  const res = await request(`/api/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
  return res.data
}

export async function deleteProduct(id) {
  await request(`/api/products/${id}`, { method: 'DELETE' })
}

export async function generateContract(applicationId) {
  const res = await request(`/api/admin/contracts/from-application/${applicationId}`, {
    method: 'POST'
  })
  return res.data
}

export async function signContract(contractId) {
  const res = await request(`/api/admin/contracts/${contractId}/sign`, {
    method: 'POST'
  })
  return res.data
}

export async function disburseContract(contractId) {
  const res = await request(`/api/admin/contracts/${contractId}/disburse`, {
    method: 'POST'
  })
  return res.data
}
