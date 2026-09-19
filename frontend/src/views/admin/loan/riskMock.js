// 只读展示数据，不请求风险接口，也不影响贷款申请状态。
export function getRiskLevel(score) {
  if (typeof score !== 'number' || !Number.isFinite(score) || score < 0 || score > 100) return 'UNKNOWN'
  if (score >= 80) return 'LOW'
  if (score >= 60) return 'MEDIUM'
  return 'HIGH'
}

export function riskLevelText(level) {
  return { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险' }[level] || '未知等级'
}

function createRiskReport({ score, credit, behavior, external, factors, decision }) {
  return Object.freeze({
    score,
    level: getRiskLevel(score),
    credit,
    behavior,
    external,
    factors: Object.freeze({
      positive: Object.freeze(factors.positive),
      negative: Object.freeze(factors.negative)
    }),
    decision: Object.freeze(decision)
  })
}

export const riskReport = createRiskReport({
  score: 72,
  credit: 78,
  behavior: 70,
  external: 68,
  factors: {
    positive: ['历史还款记录良好', '账户使用行为较稳定'],
    negative: ['近期贷款查询次数较多', '外部数据覆盖度有限']
  },
  decision: { result: '建议人工复核，适当降低授信额度', amount: 40000 }
})

const MOCK_RISK_REPORTS = Object.freeze({
  '1': riskReport,
  '2': createRiskReport({
    score: 88,
    credit: 92,
    behavior: 86,
    external: 84,
    factors: {
      positive: ['历史信用记录良好', '收入与还款能力较稳定', '多源信息一致性较高'],
      negative: ['仍需关注后续负债变化']
    },
    decision: { result: '建议通过，按申请额度授信', amount: 30000 }
  }),
  '3': createRiskReport({
    score: 49,
    credit: 45,
    behavior: 56,
    external: 46,
    factors: {
      positive: ['基础身份信息较完整'],
      negative: ['历史还款存在逾期记录', '近期负债水平较高', '部分外部信息存在不一致']
    },
    decision: { result: '建议拒绝本次申请，暂不授信', amount: 0 }
  })
})

export function getMockRiskReport(applicationId) {
  // 不存在的申请不回退到其他人的报告。
  return Object.hasOwn(MOCK_RISK_REPORTS, String(applicationId))
    ? MOCK_RISK_REPORTS[String(applicationId)]
    : null
}
