const BASE_URL = 'http://localhost:8088'

function getToken() {
  let token =
    uni.getStorageSync('token') ||
    uni.getStorageSync('accessToken') ||
    ''

  if (token.startsWith('Bearer ')) {
    token = token.substring(7)
  }

  return token
}

export function setToken(token) {
  if (!token) return

  const value = token.startsWith('Bearer ')
    ? token.substring(7)
    : token

  uni.setStorageSync('token', value)
}

export function clearToken() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('accessToken')
  uni.removeStorageSync('userInfo')
}

export default function request(options = {}) {
  const {
    url,
    method = 'GET',
    data,
    header = {},
    auth = true
  } = options

  const token = getToken()

  const headers = {
    'Content-Type': 'application/json',
    ...header
  }

  if (auth && token) {
    headers.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: headers,
      timeout: 15000,

      success(res) {
        const { statusCode, data } = res

        if (statusCode === 401) {
          clearToken()
          reject(new Error('登录状态已失效，请重新登录'))
          return
        }

        if (statusCode < 200 || statusCode >= 300) {
          reject(
            new Error(
              data?.message ||
              data?.msg ||
              `请求失败(${statusCode})`
            )
          )
          return
        }

        if (data?.success === false) {
          reject(
            new Error(
              data.message || '请求失败'
            )
          )
          return
        }

        resolve(data)
      },

      fail(err) {
        console.error('请求失败：', err)

        reject(
          new Error(
            '无法连接 Gateway：http://localhost:8088'
          )
        )
      }
    })
  })
}
