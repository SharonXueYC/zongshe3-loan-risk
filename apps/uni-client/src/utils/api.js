/**
 * 统一 API 请求（Android / H5 共用）
 * 部署时可在登录页设置服务器地址；默认同主机或开发机 8080
 */
const API_BASE_KEY = 'apiBase'

function getDefaultBase() {
  // #ifdef H5
  if (typeof window !== 'undefined' && window.location && /^https?:$/i.test(window.location.protocol)) {
    return window.location.origin
  }
  // #endif
  return 'http://10.0.2.2:8080' // Android 模拟器访问宿主机
}

export function getApiBase() {
  const saved = uni.getStorageSync(API_BASE_KEY)
  return (saved && String(saved).trim()) || getDefaultBase()
}

export function setApiBase(url) {
  uni.setStorageSync(API_BASE_KEY, String(url).replace(/\/$/, ''))
}

export function getToken() {
  return uni.getStorageSync('token') || ''
}

export function getUserId() {
  return uni.getStorageSync('userId') || ''
}

export function saveSession(token, userId) {
  uni.setStorageSync('token', token)
  uni.setStorageSync('userId', userId)
}

export function clearSession() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userId')
}

export function request(path, options = {}) {
  const token = getToken()
  const header = Object.assign({ 'Content-Type': 'application/json' }, options.header || {})
  if (token) header.Authorization = `Bearer ${token}`

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${getApiBase()}${path}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          const msg = (res.data && res.data.message) || `请求失败(${res.statusCode})`
          reject(new Error(msg))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络请求失败'))
      },
    })
  })
}

export const STATUS_MAP = {
  pending: '审核中',
  approved: '已通过',
  rejected: '已拒绝',
  disbursed: '已放款',
  paid: '已还清',
  settled: '已还清',
}

export function statusText(status) {
  if (!status) return '未知'
  return STATUS_MAP[String(status).toLowerCase()] || status
}
