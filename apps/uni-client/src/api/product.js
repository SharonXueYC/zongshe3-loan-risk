import request from '@/utils/api.js'

// 获取产品列表
export function getProductList(params = {}) {
  return request({
    url: '/api/products',
    method: 'GET',
    data: params
  })
}

// 获取产品详情
export function getProductDetail(id) {
  return request({
    url: `/api/products/${id}`,
    method: 'GET'
  })
}
