/**
 * 用户会话与按账号隔离的本地存储
 */
(function (global) {
  const API_BASE_KEY = 'apiBase';

  function getApiBase() {
    if (global.ZongsheApi && typeof global.ZongsheApi.getApiBase === 'function') {
      return global.ZongsheApi.getApiBase();
    }
    const stored = global.localStorage.getItem(API_BASE_KEY);
    if (stored && stored.trim()) {
      return stored.trim().replace(/\/$/, '');
    }
    if (global.location && /^https?:$/i.test(global.location.protocol)) {
      return global.location.origin;
    }
    return 'http://localhost:8080';
  }

  function normalizeToken(raw) {
    const v = (raw || '').trim();
    if (!v || v === 'undefined' || v === 'null') return '';
    return v.startsWith('Bearer ') ? v.slice(7).trim() : v;
  }

  function getAuthToken() {
    const urlToken = new URLSearchParams(global.location.search).get('token');
    return normalizeToken(
      localStorage.getItem('token') ||
      sessionStorage.getItem('token') ||
      urlToken
    );
  }

  function getUserId() {
    return localStorage.getItem('userId') || '';
  }

  function userKey(prefix) {
    const userId = getUserId();
    return userId ? `${prefix}_${userId}` : prefix;
  }

  function parseUserIdFromToken(token) {
    try {
      const part = (token || '').split('.')[1];
      if (!part) return '';
      const json = JSON.parse(atob(part.replace(/-/g, '+').replace(/_/g, '/')));
      return json.username || json.sub || '';
    } catch (_) {
      return '';
    }
  }

  function syncSessionFromToken() {
    const token = getAuthToken();
    if (!token) return;
    const tokenUserId = parseUserIdFromToken(token);
    if (!tokenUserId) return;
    const storedUserId = getUserId();
    if (storedUserId && storedUserId !== tokenUserId) {
      onUserSwitch(tokenUserId);
    }
    localStorage.setItem('userId', tokenUserId);
  }

  function onUserSwitch(newUserId) {
    const oldUserId = localStorage.getItem('userId');
    if (oldUserId && newUserId && oldUserId !== newUserId) {
      localStorage.removeItem('lastLoanLimit');
      localStorage.removeItem('phoneNumber');
    }
    localStorage.setItem('currentUserId', newUserId || '');
  }

  function saveUserSession(data) {
    if (!data) return;
    const newUserId = data.userInfo && data.userInfo.userId;
    if (newUserId) {
      onUserSwitch(newUserId);
      localStorage.setItem('userId', newUserId);
    }
    if (data.token) {
      localStorage.setItem('token', data.token);
      sessionStorage.setItem('token', data.token);
    }
    if (data.userInfo && data.userInfo.phoneNumber) {
      localStorage.setItem('phoneNumber', data.userInfo.phoneNumber);
    }
  }

  function clearUserSession() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('phoneNumber');
    localStorage.removeItem('lastLoanLimit');
    sessionStorage.removeItem('token');
  }

  async function validateSession() {
    const token = getAuthToken();
    if (!token) return false;
    syncSessionFromToken();
    const userId = getUserId() || parseUserIdFromToken(token);
    if (!userId) return false;
    try {
      const res = await fetch(`${getApiBase()}/api/users/${encodeURIComponent(userId)}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.status === 401 || res.status === 403) return false;
      return res.ok;
    } catch (_) {
      return false;
    }
  }

  async function ensureSessionOrRedirect(loginPath) {
    const path = loginPath || 'login-prototype.html';
    const params = new URLSearchParams(global.location.search);
    if (params.get('logout') === '1') {
      clearUserSession();
      global.location.replace(path);
      return false;
    }
    const token = getAuthToken();
    if (!token) {
      global.location.replace(path);
      return false;
    }
    const valid = await validateSession();
    if (!valid) {
      clearUserSession();
      global.location.replace(path);
      return false;
    }
    return true;
  }

  async function apiRequest(path, options = {}) {
    const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
    const token = getAuthToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    const res = await fetch(`${getApiBase()}${path}`, {
      method: options.method || 'GET',
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message || `请求失败(${res.status})`);
    return data;
  }

  global.UserSession = {
    getApiBase,
    getAuthToken,
    getUserId,
    userKey,
    saveUserSession,
    clearUserSession,
    validateSession,
    ensureSessionOrRedirect,
    syncSessionFromToken,
    parseUserIdFromToken,
    apiRequest,
    INITIAL_LOAN_LIMIT: 5000
  };
})(window);
