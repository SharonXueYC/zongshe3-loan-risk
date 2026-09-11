import request, { setToken } from '@/utils/request.js'

function getData(res) {
  return res?.data || res
}

function getToken(res) {
  const data = getData(res)

  return (
    data?.token ||
    data?.accessToken ||
    data?.access_token ||
    res?.token ||
    res?.accessToken ||
    ''
  )
}

export async function login(phoneNumber, password) {
  const res = await request({
    url: '/api/users/login',
    method: 'POST',
    data: {
      phoneNumber,
      password
    },
    auth: false
  })

  const token = getToken(res)

  if (!token) {
    throw new Error('后端未返回 token')
  }

  setToken(token)

  return res
}

export function register(data) {
  return request({
    url: '/api/users/register',
    method: 'POST',
    auth: false,
    data: {
      phoneNumber: data.phoneNumber,
      password: data.password,
      userName: data.userName,
      channelId: data.channelId || null
    }
  })
}
