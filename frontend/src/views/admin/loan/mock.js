// 仅用于管理员端展示；列表与详情共用同一数据源，不请求贷款业务接口。
export const MOCK_LOAN_APPLICATIONS = Object.freeze([
  { id: '1', applicationNo: 'LA202609150001', userId: 'U1001', userName: '张三', phone: '138****0001', amount: 50000, term: 12, status: 'PENDING', applyTime: '2026-09-15 10:30' },
  { id: '2', applicationNo: 'LA202609140002', userId: 'U1002', userName: '李四', phone: '139****0002', amount: 30000, term: 6, status: 'APPROVED', applyTime: '2026-09-14 14:20' },
  { id: '3', applicationNo: 'LA202609130003', userId: 'U1003', userName: '王五', phone: '137****0003', amount: 80000, term: 24, status: 'REJECTED', applyTime: '2026-09-13 09:15' }
].map(application => Object.freeze(application)))

export const LOAN_STATUS_LABELS = Object.freeze({
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
})

export function getMockLoanApplication(id) {
  return MOCK_LOAN_APPLICATIONS.find(application => application.id === String(id)) || null
}

export function loanStatusText(status) {
  return LOAN_STATUS_LABELS[status] || '未知状态'
}

export function formatLoanAmount(value) {
  return `¥${Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })}`
}
