import { request } from './client'

const ENABLED_STATUS_VALUES = new Set([
  '1', 'active', 'enabled', 'enable', 'true', 'on', '启用', '上架'
])

const DISABLED_STATUS_VALUES = new Set([
  '0', 'inactive', 'disabled', 'disable', 'false', 'off', '禁用', '下架'
])

/**
 * 将后端不同版本的产品状态统一为页面使用的 active / inactive。
 */
export function normalizeProductStatus(status) {
  if (status === true) return 'active'
  if (status === false) return 'inactive'
  if (status === null || status === undefined) return ''

  const value = String(status).trim().toLocaleLowerCase()
  if (ENABLED_STATUS_VALUES.has(value)) return 'active'
  if (DISABLED_STATUS_VALUES.has(value)) return 'inactive'
  return ''
}

function normalizeProduct(product) {
  if (!product || typeof product !== 'object') return product
  return {
    ...product,
    status: normalizeProductStatus(product.status)
  }
}

function normalizeProductPayload(payload) {
  const normalizedStatus = normalizeProductStatus(payload?.status)
  return {
    ...payload,
    ...(normalizedStatus ? { status: normalizedStatus } : {})
  }
}

function unwrapData(response, fallbackMessage) {
  if (!response || typeof response !== 'object') {
    throw new Error(fallbackMessage)
  }
  if (response.success === false) {
    throw new Error(response.message || fallbackMessage)
  }
  return response.data
}

function createListQuery(filters) {
  // 兼容旧调用 fetchProducts(search)，便于 Dashboard 继续复用同一实现。
  const values = typeof filters === 'string' ? { search: filters } : (filters || {})
  const params = new URLSearchParams()

  const search = String(values.search || '').trim()
  if (search) params.set('search', search)
  if (values.type && values.type !== 'all') params.set('type', values.type)
  if (values.status && values.status !== 'all') {
    const status = normalizeProductStatus(values.status)
    if (status) params.set('status', status)
  }

  const query = params.toString()
  return query ? `?${query}` : ''
}

export async function getProducts(filters = {}) {
  const response = await request(`/api/products${createListQuery(filters)}`)
  const products = unwrapData(response, '产品列表加载失败')
  if (!Array.isArray(products)) throw new Error('产品列表返回格式异常')
  return products.map(normalizeProduct)
}

export async function getProductById(id) {
  const response = await request(`/api/products/${encodeURIComponent(id)}`)
  return normalizeProduct(unwrapData(response, '产品详情加载失败'))
}

export async function createProduct(payload) {
  const response = await request('/api/products', {
    method: 'POST',
    body: JSON.stringify(normalizeProductPayload(payload))
  })
  return normalizeProduct(unwrapData(response, '产品创建失败'))
}

export async function updateProduct(id, payload) {
  const response = await request(`/api/products/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: JSON.stringify(normalizeProductPayload(payload))
  })
  return normalizeProduct(unwrapData(response, '产品更新失败'))
}

export async function deleteProduct(id) {
  const response = await request(`/api/products/${encodeURIComponent(id)}`, {
    method: 'DELETE'
  })
  if (response?.success === false) {
    throw new Error(response.message || '产品删除失败')
  }
  return response
}
