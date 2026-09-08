// src/utils/request.js

/**
 * 统一请求工具
 * 基地址指向 Gateway，自动携带 Bearer Token，统一错误码处理
 */

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

// 默认错误码映射
const ERROR_CODE_MAP = {
  401: '登录已过期，请重新登录',
  403: '无权限访问',
  404: '请求资源不存在',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂时不可用',
};

/**
 * 获取存储的 Token
 */
function getToken() {
  return localStorage.getItem('token') || sessionStorage.getItem('token') || '';
}

/**
 * 设置 Token
 */
export function setToken(token) {
  if (!token) {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
    return;
  }
  // 默认存储在 localStorage
  localStorage.setItem('token', token);
}

/**
 * 清除 Token
 */
export function clearToken() {
  localStorage.removeItem('token');
  sessionStorage.removeItem('token');
}

/**
 * 获取用户 ID
 */
export function getUserId() {
  return localStorage.getItem('userId') || sessionStorage.getItem('userId') || '';
}

/**
 * 设置用户 ID
 */
export function setUserId(userId) {
  if (!userId) {
    localStorage.removeItem('userId');
    sessionStorage.removeItem('userId');
    return;
  }
  localStorage.setItem('userId', userId);
}

/**
 * 统一请求函数
 * @param {string} path - 请求路径，会自动拼接 API_BASE
 * @param {object} options - 请求选项
 * @param {string} options.method - 请求方法 GET/POST/PUT/DELETE
 * @param {object} options.body - 请求体，会自动 JSON.stringify
 * @param {object} options.headers - 额外的请求头
 * @param {boolean} options.skipAuth - 是否跳过自动添加 Token
 * @returns {Promise<any>} 返回解析后的 JSON 数据
 */
export async function request(path, options = {}) {
  const {
    method = 'GET',
    body = null,
    headers = {},
    skipAuth = false,
    timeout = 30000,
  } = options;

  // 构建完整 URL
  const url = path.startsWith('http') ? path : `${API_BASE}${path}`;

  // 构建请求头
  const requestHeaders = {
    'Content-Type': 'application/json',
    ...headers,
  };

  // 自动添加 Bearer Token
  if (!skipAuth) {
    const token = getToken();
    if (token) {
      requestHeaders.Authorization = `Bearer ${token}`;
    }
  }

  // 构建请求配置
  const fetchOptions = {
    method,
    headers: requestHeaders,
    cache: 'no-cache',
  };

  if (body) {
    fetchOptions.body = JSON.stringify(body);
  }

  // 超时控制
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);
  fetchOptions.signal = controller.signal;

  try {
    const response = await fetch(url, fetchOptions);
    clearTimeout(timeoutId);

    // 尝试解析 JSON，如果响应为空则返回空对象
    let data;
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = {};
    }

    // 检查 HTTP 状态码
    if (!response.ok) {
      // 处理 401 未授权
      if (response.status === 401) {
        clearToken();
        setUserId('');
        // 触发全局登录过期事件
        window.dispatchEvent(new CustomEvent('auth:expired', { detail: { message: data.message || '登录已过期' } }));
      }

      // 使用自定义错误码映射或后端返回的消息
      const errorMessage = data.message || ERROR_CODE_MAP[response.status] || `请求失败 (${response.status})`;
      const error = new Error(errorMessage);
      error.status = response.status;
      error.code = data.code;
      error.data = data;
      throw error;
    }

    // 业务成功判断（如果后端有 success 字段）
    if (data.success === false) {
      const error = new Error(data.message || '业务处理失败');
      error.status = 200;
      error.code = data.code;
      error.data = data;
      throw error;
    }

    return data;
  } catch (error) {
    clearTimeout(timeoutId);

    // 处理 AbortError
    if (error.name === 'AbortError') {
      const timeoutError = new Error('请求超时，请稍后重试');
      timeoutError.status = 408;
      throw timeoutError;
    }

    // 处理网络错误
    if (error instanceof TypeError && error.message.includes('fetch')) {
      const networkError = new Error('网络连接失败，请检查网络设置');
      networkError.status = 0;
      throw networkError;
    }

    throw error;
  }
}

/**
 * GET 请求快捷方法
 */
export function get(path, options = {}) {
  return request(path, { ...options, method: 'GET' });
}

/**
 * POST 请求快捷方法
 */
export function post(path, body, options = {}) {
  return request(path, { ...options, method: 'POST', body });
}

/**
 * PUT 请求快捷方法
 */
export function put(path, body, options = {}) {
  return request(path, { ...options, method: 'PUT', body });
}

/**
 * DELETE 请求快捷方法
 */
export function del(path, options = {}) {
  return request(path, { ...options, method: 'DELETE' });
}

/**
 * 带 Token 的请求（自动使用最新的 Token）
 */
export function authRequest(path, options = {}) {
  return request(path, { ...options });
}

export default {
  request,
  get,
  post,
  put,
  delete: del,
  authRequest,
  setToken,
  clearToken,
  getToken,
  setUserId,
  getUserId,
};